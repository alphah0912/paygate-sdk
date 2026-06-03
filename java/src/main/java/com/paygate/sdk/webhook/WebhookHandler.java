package com.paygate.sdk.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paygate.sdk.ErrorCode;
import com.paygate.sdk.PaygateException;
import com.paygate.sdk.SignatureUtil;

import java.util.List;
import java.util.Map;

/**
 * Handles incoming webhook notifications from the PayGate platform.
 *
 * <p>Two webhook types are supported:
 * <ul>
 *   <li><b>ISV Webhook</b> — HMAC-SHA256 signed (header {@code X-Isv-Signature}).
 *       Verified against the configured {@code isvWebhookSecret}.</li>
 *   <li><b>NotificationService Webhook</b> — plaintext secret matching
 *       (header {@code X-Webhook-Secret}).</li>
 * </ul>
 *
 * <pre>{@code
 * WebhookHandler handler = new WebhookHandler("isv_secret", "notify_secret");
 * WebhookEvent event = handler.handle(headers, body, "/webhook");
 * if (event instanceof WebhookEvent.PaymentResult pr) {
 *     System.out.println(pr.getStatus());
 * }
 * }</pre>
 *
 * @author alphah
 * @since 1.0.0
 */
public class WebhookHandler {

    /** Header name for ISV webhook HMAC signature */
    private static final String ISV_SIGNATURE_HEADER = "X-Isv-Signature";
    /** Header name for ISV webhook timestamp */
    private static final String ISV_TIMESTAMP_HEADER = "X-Isv-Timestamp";
    /** Header name for NotificationService plaintext secret */
    private static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";
    /** Shared JSON mapper for deserializing webhook payloads */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Platform ISV webhook secret for HMAC-SHA256 verification */
    private final String isvWebhookSecret;
    /** Notification service secret for plaintext comparison */
    private final String notificationWebhookSecret;

    /**
     * @param isvWebhookSecret            platform ISV webhook secret for HMAC verification
     * @param notificationWebhookSecret   notification service secret for plaintext comparison
     */
    public WebhookHandler(String isvWebhookSecret, String notificationWebhookSecret) {
        this.isvWebhookSecret = isvWebhookSecret;
        this.notificationWebhookSecret = notificationWebhookSecret;
    }

    /**
     * Processes an incoming webhook request. Detects the type from request headers,
     * verifies the signature, and returns a typed event.
     *
     * @param headers request headers (multi-valued)
     * @param body    raw JSON request body
     * @param path    request path, used for ISV signature verification
     * @return the parsed event
     * @throws PaygateException if signature verification fails or the payload is invalid
     */
    public WebhookEvent handle(Map<String, List<String>> headers, String body, String path) {
        if (headers.containsKey(ISV_SIGNATURE_HEADER)) {
            return handleIsvWebhook(headers, body, path);
        }
        if (headers.containsKey(WEBHOOK_SECRET_HEADER)) {
            return handleNotificationWebhook(headers, body);
        }
        throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "No webhook signature header found");
    }

    private WebhookEvent handleIsvWebhook(Map<String, List<String>> headers, String body, String path) {
        String sigHeader = first(headers, ISV_SIGNATURE_HEADER);
        String tsHeader = first(headers, ISV_TIMESTAMP_HEADER);

        if (sigHeader == null || tsHeader == null) {
            throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(),
                    "Missing ISV webhook signature headers");
        }

        boolean valid = SignatureUtil.verify(isvWebhookSecret, sigHeader, "POST", path, tsHeader, body);
        if (!valid) {
            throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "ISV webhook signature mismatch");
        }

        try {
            return MAPPER.readValue(body, WebhookEvent.PaymentResult.class);
        } catch (Exception e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "Failed to parse ISV webhook payload", e);
        }
    }

    @SuppressWarnings("unchecked")
    private WebhookEvent handleNotificationWebhook(Map<String, List<String>> headers, String body) {
        String secretHeader = first(headers, WEBHOOK_SECRET_HEADER);

        if (!notificationWebhookSecret.equals(secretHeader)) {
            throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "Webhook secret mismatch");
        }

        try {
            Map<String, Object> map = MAPPER.readValue(body, Map.class);
            String type = (String) map.get("type");
            if (type == null) {
                throw new PaygateException(ErrorCode.INVALID_PARAMETER.getCode(),
                        "Notification webhook missing type field");
            }
            switch (type) {
                case "sign.success":
                    return MAPPER.convertValue(map, WebhookEvent.SignSuccess.class);
                case "payment.completed":
                    return MAPPER.convertValue(map, WebhookEvent.PaymentCompleted.class);
                case "refund.completed":
                    return MAPPER.convertValue(map, WebhookEvent.RefundCompleted.class);
                default:
                    throw new PaygateException(ErrorCode.INVALID_PARAMETER.getCode(),
                            "Unknown webhook event type: " + type);
            }
        } catch (PaygateException e) {
            throw e;
        } catch (Exception e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "Failed to parse webhook payload", e);
        }
    }

    /** Extracts the first value of a header key, or {@code null}. */
    private static String first(Map<String, List<String>> headers, String key) {
        List<String> values = headers.get(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
}
