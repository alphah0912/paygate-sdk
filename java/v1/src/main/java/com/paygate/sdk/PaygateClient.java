package com.paygate.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paygate.sdk.request.CancelRequest;
import com.paygate.sdk.request.CaptureRequest;
import com.paygate.sdk.request.InquiryPaymentRequest;
import com.paygate.sdk.request.InquiryRefundRequest;
import com.paygate.sdk.request.PayRequest;
import com.paygate.sdk.request.RefundRequest;
import com.paygate.sdk.response.ApiResponse;
import com.paygate.sdk.response.CancelResponse;
import com.paygate.sdk.response.CaptureResponse;
import com.paygate.sdk.response.InquiryPaymentResponse;
import com.paygate.sdk.response.InquiryRefundResponse;
import com.paygate.sdk.response.PayResponse;
import com.paygate.sdk.response.RefundResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Main entry point for the PayGate SDK.
 *
 * <p>Construct via {@link #builder()} and call one of the six API methods.
 * Each method automatically handles HMAC-SHA256 signing, timestamp generation,
 * and response parsing. On rate-limit (HTTP 429) or server errors (502/503/504)
 * the client retries with exponential backoff up to 3 times.
 *
 * <p>Zero external HTTP dependencies — uses {@link HttpURLConnection} (JDK 1.8+).
 * Every request carries a unique {@code X-Request-Id} header for log correlation.
 *
 * <pre>{@code
 * PaygateClient client = PaygateClient.builder()
 *     .apiKey("mk_test_xxx")
 *     .apiSecret("your_api_secret")
 *     .environment(Environment.SANDBOX)
 *     .build();
 *
 * PayResponse resp = client.pay(PayRequest.builder()
 *     .amount("100.00")
 *     .paymentMethodCode("ALIPAY_CN")
 *     .terminalType("WEB")
 *     .settlementCurrency("USD")
 *     .build());
 * }</pre>
 *
 * @author alphah
 * @since 1.0.0
 */
public class PaygateClient {

    /** HTTP method used for all API requests */
    private static final String POST = "POST";

    /** Content-Type header value */
    private static final String APPLICATION_JSON = "application/json";

    /** Read timeout in milliseconds */
    private static final int READ_TIMEOUT_MS = 30000;

    /** Connect timeout in milliseconds */
    private static final int CONNECT_TIMEOUT_MS = 10000;

    /** Maximum number of retries for transient errors (429, 502, 503, 504) */
    private static final int MAX_RETRIES = 3;

    /** Shared JSON mapper, tolerant of unknown response fields */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** Merchant API key, prefixed with {@code mk_live_} or {@code mk_test_} */
    private final String apiKey;

    /** Merchant API secret for HMAC-SHA256 signing, never exposed in responses */
    private final String apiSecret;

    /** Resolved API base URL from the configured {@link Environment} */
    private final String baseUrl;

    /** API path prefix used for HMAC signing (e.g. /api/gateway/v1) */
    private final String basePath;

    private PaygateClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
        this.baseUrl = builder.environment.getBaseUrl();
        this.basePath = builder.environment.getBasePath();
    }

    /**
     * Package-private constructor for testing with a custom base URL.
     * Path prefix defaults to empty string (test server has no path prefix).
     */
    PaygateClient(String apiKey, String apiSecret, String baseUrl) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseUrl = baseUrl;
        this.basePath = "";
    }

    // ── API methods ────────────────────────────────────────

    public PayResponse pay(PayRequest request) {
        return execute("/pay", request, PayResponse.class);
    }

    public CaptureResponse capture(CaptureRequest request) {
        return execute("/capture", request, CaptureResponse.class);
    }

    public CancelResponse cancel(CancelRequest request) {
        return execute("/cancel", request, CancelResponse.class);
    }

    public InquiryPaymentResponse inquiryPayment(InquiryPaymentRequest request) {
        return execute("/inquiry-payment", request, InquiryPaymentResponse.class);
    }

    public InquiryRefundResponse inquiryRefund(InquiryRefundRequest request) {
        return execute("/inquiry-refund", request, InquiryRefundResponse.class);
    }

    public RefundResponse refund(RefundRequest request) {
        return execute("/refund", request, RefundResponse.class);
    }

    // ── Internal ───────────────────────────────────────────

    private <T> T execute(String path, Object request, Class<T> responseClass) {
        return executeWithRetry(path, request, responseClass, 0);
    }

    private <T> T executeWithRetry(String path, Object request, Class<T> responseClass, int attempt) {
        String body;
        try {
            body = MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "Failed to serialize request", e);
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String signingPath = basePath + path;
        String signature = SignatureUtil.sign(apiSecret, POST, signingPath, timestamp, body);
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(POST);
            conn.setRequestProperty("Content-Type", APPLICATION_JSON);
            conn.setRequestProperty("X-Api-Key", apiKey);
            conn.setRequestProperty("X-Signature", signature);
            conn.setRequestProperty("X-Timestamp", timestamp);
            conn.setRequestProperty("X-Request-Id", requestId);
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int statusCode = conn.getResponseCode();

            // Retry on 429 (rate limit) and server transient errors
            if (attempt < MAX_RETRIES && isRetryable(statusCode)) {
                conn.disconnect();
                long delay = (long) Math.pow(2, attempt + 1) * 1000;
                try { Thread.sleep(delay); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new PaygateException(ErrorCode.RATE_LIMITED.getCode(), "Retry interrupted");
                }
                return executeWithRetry(path, request, responseClass, attempt + 1);
            }

            String responseBody = readResponseBody(conn);

            if (statusCode >= 500) {
                throw new PaygateException(ErrorCode.SERVER_ERROR.getCode(),
                        "Server returned " + statusCode + ": " + responseBody, statusCode);
            }

            T response = MAPPER.readValue(responseBody, responseClass);

            if (response instanceof ApiResponse) {
                ApiResponse ar = (ApiResponse) response;
                if (!ar.isSuccess()) {
                    throw new PaygateException(String.valueOf(ar.getCode()), ar.getMessage());
                }
            }

            return response;
        } catch (JsonProcessingException e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(),
                    "Failed to parse response", e);
        } catch (IOException e) {
            throw new PaygateException(ErrorCode.NETWORK_ERROR.getCode(),
                    "Network request failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /** Retry 429, 502, 503, 504 — transient errors worth retrying. */
    private static boolean isRetryable(int statusCode) {
        return statusCode == 429 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    /**
     * Reads the response body from either the input stream (2xx) or error stream (4xx/5xx).
     */
    private String readResponseBody(HttpURLConnection conn) throws IOException {
        InputStream stream;
        if (conn.getResponseCode() >= 400) {
            stream = conn.getErrorStream();
        } else {
            stream = conn.getInputStream();
        }
        if (stream == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = stream.read(buf)) != -1) {
            baos.write(buf, 0, n);
        }
        stream.close();
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    // ── Builder ────────────────────────────────────────────

    /** @return a new {@link Builder} instance */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link PaygateClient}.
     *
     * <p>If {@code apiKey} starts with {@code mk_test_}, the environment is automatically
     * forced to {@link Environment#SANDBOX} for safety.
     */
    public static class Builder {
        private String apiKey;
        private String apiSecret;
        private Environment environment = Environment.SANDBOX;

        /** @param apiKey merchant API key, starting with {@code mk_live_} or {@code mk_test_} */
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }

        /** @param apiSecret merchant API secret for HMAC signing */
        public Builder apiSecret(String apiSecret) { this.apiSecret = apiSecret; return this; }

        /** @param environment target environment, defaults to {@link Environment#SANDBOX} */
        public Builder environment(Environment environment) { this.environment = environment; return this; }

        /** @return a configured {@link PaygateClient} */
        public PaygateClient build() {
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            if (apiSecret == null || apiSecret.isEmpty()) {
                throw new IllegalArgumentException("apiSecret is required");
            }
            if (apiKey.startsWith("mk_test_")) {
                this.environment = Environment.SANDBOX;
            }
            return new PaygateClient(this);
        }
    }
}
