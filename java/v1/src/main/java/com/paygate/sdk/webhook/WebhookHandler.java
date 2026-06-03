package com.paygate.sdk.webhook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paygate.sdk.ErrorCode;
import com.paygate.sdk.PaygateException;
import com.paygate.sdk.SignatureUtil;

import java.util.List;
import java.util.Map;

/**
 * Handles incoming webhook notifications from the PayGate platform.
 *
 * <p>Two webhook types, both using HMAC-SHA256:
 * <ul>
 *   <li><b>ISV Webhook</b> — headers {@code X-Isv-Signature} + {@code X-Isv-Timestamp},
 *       verified with platform ISV secret.</li>
 *   <li><b>NotificationService Webhook</b> — headers {@code X-Webhook-Signature} + {@code X-Webhook-Timestamp},
 *       verified with the merchant's own webhook secret.</li>
 * </ul>
 *
 * <p>Sign string format (aligned with platform): {@code POST\n{notifyUrl}\n{timestamp}\n{body}}
 *
 * <pre>{@code
 * WebhookHandler handler = new WebhookHandler("webhook_secret");
 * WebhookEvent event = handler.handle(headers, body, "https://merchant.com/webhook");
 * }</pre>
 *
 * @author alphah
 * @since 1.0.0
 */
public class WebhookHandler {

    /**
     * Header name for ISV webhook HMAC signature
     */
    private static final String ISV_SIGNATURE_HEADER = "X-Isv-Signature";
    /**
     * Header name for ISV webhook timestamp
     */
    private static final String ISV_TIMESTAMP_HEADER = "X-Isv-Timestamp";
    /**
     * Header name for NotificationService webhook HMAC signature
     */
    private static final String WEBHOOK_SIGNATURE_HEADER = "X-Webhook-Signature";
    /**
     * Header name for NotificationService webhook timestamp
     */
    private static final String WEBHOOK_TIMESTAMP_HEADER = "X-Webhook-Timestamp";
    /**
     * Shared JSON mapper, tolerant of unknown response fields
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Platform ISV webhook secret for HMAC-SHA256 verification
     */
    private final String webhookSecret;
    /**
     * Merchant's own webhook secret for NotificationService HMAC verification
     */
    private final String merchantWebhookSecret;

    /**
     * @param webhookSecret      platform ISV webhook secret for HMAC verification
     * @param merchantWebhookSecret merchant's own webhook secret for NotificationService verification
     */
    public WebhookHandler(String webhookSecret, String merchantWebhookSecret) {
        this.webhookSecret = webhookSecret;
        this.merchantWebhookSecret = merchantWebhookSecret;
    }

    /**
     * Processes an incoming webhook request. Detects the type from request headers,
     * verifies the signature, and returns a typed event.
     *
     * @param headers   request headers (multi-valued)
     * @param body      raw JSON request body
     * @param notifyUrl the full URL the webhook was sent to (e.g. https://merchant.com/webhook),
     *                  used for ISV signature verification with the same sign string format as the platform
     * @return the parsed event
     * @throws PaygateException if signature verification fails or the payload is invalid
     */
    public WebhookEvent handle(Map<String, List<String>> headers, String body, String notifyUrl) {
        if (headers.containsKey(ISV_SIGNATURE_HEADER)) {
            return handleIsvWebhook(headers, body, notifyUrl);
        }
        if (headers.containsKey(WEBHOOK_SIGNATURE_HEADER)) {
            return handleNotificationWebhook(headers, body, notifyUrl);
        }
        throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "No webhook signature header found");
    }

    private WebhookEvent handleIsvWebhook(Map<String, List<String>> headers, String body, String notifyUrl) {
        String sigHeader = first(headers, ISV_SIGNATURE_HEADER);
        String tsHeader = first(headers, ISV_TIMESTAMP_HEADER);

        if (sigHeader == null || tsHeader == null) {
            throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "Missing ISV webhook signature headers");
        }

        // Platform signs with "POST\n{notifyUrl}\n{timestamp}\n{body}" (full URL, not just path)
        boolean valid = SignatureUtil.verify(webhookSecret, sigHeader, "POST", notifyUrl, tsHeader, body);
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
    private WebhookEvent handleNotificationWebhook(Map<String, List<String>> headers, String body, String notifyUrl) {
        String sigHeader = first(headers, WEBHOOK_SIGNATURE_HEADER);
        String tsHeader = first(headers, WEBHOOK_TIMESTAMP_HEADER);

        // Platform sends signature only when a secret is configured.
        // If sig present, secret must be configured and must pass verification.
        if (sigHeader != null && tsHeader != null) {
            if (merchantWebhookSecret == null) {
                throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(),
                        "Webhook signature received but no merchant secret configured");
            }
            boolean valid = SignatureUtil.verify(merchantWebhookSecret, sigHeader, "POST", notifyUrl, tsHeader, body);
            if (!valid) {
                throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "Webhook signature mismatch");
            }
        }

        try {
            Map<String, Object> map = MAPPER.readValue(body, Map.class);
            String type = (String) map.get("type");
            if (type == null) {
                throw new PaygateException(ErrorCode.INVALID_PARAMETER.getCode(), "Webhook payload missing type field");
            }
            switch (type) {
                case "sign.success":
                    return MAPPER.convertValue(map, WebhookEvent.SignSuccess.class);
                case "payment.completed":
                    return MAPPER.convertValue(map, WebhookEvent.PaymentCompleted.class);
                case "refund.completed":
                    return MAPPER.convertValue(map, WebhookEvent.RefundCompleted.class);
                default:
                    throw new PaygateException(ErrorCode.INVALID_PARAMETER.getCode(), "Unknown webhook event type: " + type);
            }
        } catch (PaygateException e) {
            throw e;
        } catch (Exception e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "Failed to parse webhook payload", e);
        }
    }

    /**
     * Extracts the first value of a header key, or {@code null}.
     */
    private static String first(Map<String, List<String>> headers, String key) {
        List<String> values = headers.get(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
}
