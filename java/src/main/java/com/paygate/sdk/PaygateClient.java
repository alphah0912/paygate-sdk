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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Main entry point for the PayGate SDK.
 *
 * <p>Construct via {@link #builder()} and call one of the six API methods.
 * Each method automatically handles HMAC-SHA256 signing, timestamp generation,
 * and response parsing. On rate-limit (HTTP 429) the client retries with
 * exponential backoff up to 3 times.
 *
 * <p>Zero external dependencies — uses {@link HttpURLConnection} (JDK 1.8+) for HTTP.
 *
 * <pre>{@code
 * PaygateClient client = PaygateClient.builder()
 *     .apiKey("mk_test_xxx")
 *     .apiSecret("your_api_secret")
 *     .environment(Environment.SANDBOX)
 *     .build();
 *
 * PayResponse resp = client.pay(
 *     PayRequest.builder()
 *         .amount("100.00")
 *         .paymentMethodCode("ALIPAY_CN")
 *         .terminalType("WEB")
 *         .settlementCurrency("USD")
 *         .build()
 * );
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

    /** Maximum number of retries when rate-limited (HTTP 429) */
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

    private PaygateClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
        this.baseUrl = builder.environment.getBaseUrl();
    }

    /**
     * Package-private constructor for testing with a custom base URL.
     */
    PaygateClient(String apiKey, String apiSecret, String baseUrl) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.baseUrl = baseUrl;
    }

    /**
     * Initiates a payment.
     *
     * @param request payment parameters
     * @return response with {@code redirectUrl} and {@code paymentRequestId}
     * @throws PaygateException on API error, network failure, or invalid signature
     */
    public PayResponse pay(PayRequest request) {
        return execute("/pay", request, PayResponse.class);
    }

    /**
     * Captures a previously authorized payment.
     *
     * @param request capture parameters
     * @return response with {@code captureId} and {@code status}
     * @throws PaygateException on API error or network failure
     */
    public CaptureResponse capture(CaptureRequest request) {
        return execute("/capture", request, CaptureResponse.class);
    }

    /**
     * Cancels a payment.
     *
     * @param request cancel parameters
     * @return response with {@code status}
     * @throws PaygateException on API error or network failure
     */
    public CancelResponse cancel(CancelRequest request) {
        return execute("/cancel", request, CancelResponse.class);
    }

    /**
     * Queries the status of a payment.
     *
     * @param request inquiry parameters
     * @return response with {@code tradeStatus}, {@code paymentId}, {@code paymentStatus}
     * @throws PaygateException on API error or network failure
     */
    public InquiryPaymentResponse inquiryPayment(InquiryPaymentRequest request) {
        return execute("/inquiry-payment", request, InquiryPaymentResponse.class);
    }

    /**
     * Queries the status of a refund.
     *
     * @param request inquiry parameters (supply either refundTransactionId or paymentRequestId)
     * @return response with {@code tradeStatus}, {@code refundId}, {@code refundStatus}
     * @throws PaygateException on API error or network failure
     */
    public InquiryRefundResponse inquiryRefund(InquiryRefundRequest request) {
        return execute("/inquiry-refund", request, InquiryRefundResponse.class);
    }

    /**
     * Initiates a refund.
     *
     * @param request refund parameters
     * @return response with {@code refundTransactionId} and {@code refundStatus}
     * @throws PaygateException on API error or network failure
     */
    public RefundResponse refund(RefundRequest request) {
        return execute("/refund", request, RefundResponse.class);
    }

    private <T> T execute(String path, Object request, Class<T> responseClass) {
        String body;
        try {
            body = MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "Failed to serialize request", e);
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = SignatureUtil.sign(apiSecret, POST, path, timestamp, body);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(POST);
            conn.setRequestProperty("Content-Type", APPLICATION_JSON);
            conn.setRequestProperty("X-Api-Key", apiKey);
            conn.setRequestProperty("X-Signature", signature);
            conn.setRequestProperty("X-Timestamp", timestamp);
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int statusCode = conn.getResponseCode();

            if (statusCode == 429 && MAX_RETRIES > 0) {
                conn.disconnect();
                return retryWithBackoff(path, request, responseClass, 1);
            }

            String responseBody = readResponseBody(conn);

            if (statusCode >= 500) {
                throw new PaygateException(ErrorCode.SERVER_ERROR.getCode(),
                        "Server returned " + statusCode + ": " + responseBody, statusCode);
            }

            T response;
            try {
                response = MAPPER.readValue(responseBody, responseClass);
            } catch (JsonProcessingException e) {
                throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(),
                        "Failed to parse response: " + responseBody, e);
            }

            if (response instanceof ApiResponse) {
                ApiResponse ar = (ApiResponse) response;
                if (!ar.isSuccess()) {
                    throw new PaygateException(ar.getCode(), ar.getMessage());
                }
            }

            return response;
        } catch (IOException e) {
            throw new PaygateException(ErrorCode.NETWORK_ERROR.getCode(),
                    "Network request failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
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
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[4096];
        int n;
        while ((n = stream.read(buf)) != -1) {
            sb.append(new String(buf, 0, n, StandardCharsets.UTF_8));
        }
        stream.close();
        return sb.toString();
    }

    private <T> T retryWithBackoff(String path, Object request, Class<T> responseClass, int attempt) {
        try {
            Thread.sleep((long) Math.pow(2, attempt) * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaygateException(ErrorCode.RATE_LIMITED.getCode(), "Retry interrupted");
        }
        return execute(path, request, responseClass);
    }

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
        /** @see #apiKey(String) */
        private String apiKey;
        /** @see #apiSecret(String) */
        private String apiSecret;
        /** Defaults to {@link Environment#SANDBOX} for safety */
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
