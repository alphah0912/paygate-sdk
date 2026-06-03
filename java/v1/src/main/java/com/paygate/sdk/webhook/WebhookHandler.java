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
 * <p>Both ISV and NotificationService webhooks share the same secret
 * (aligned with platform: webhook.getSecret()).
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

    private static final String ISV_SIGNATURE_HEADER = "X-Isv-Signature";
    private static final String ISV_TIMESTAMP_HEADER = "X-Isv-Timestamp";
    private static final String WEBHOOK_SIGNATURE_HEADER = "X-Webhook-Signature";
    private static final String WEBHOOK_TIMESTAMP_HEADER = "X-Webhook-Timestamp";
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String webhookSecret;

    public WebhookHandler(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

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
        if (webhookSecret == null) {
            throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "Webhook secret not configured");
        }
        if (!SignatureUtil.verify(webhookSecret, sigHeader, "POST", notifyUrl, tsHeader, body)) {
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

        if (sigHeader != null && tsHeader != null) {
            if (webhookSecret == null) {
                throw new PaygateException(ErrorCode.INVALID_SIGNATURE.getCode(), "Webhook signature received but no secret configured");
            }
            if (!SignatureUtil.verify(webhookSecret, sigHeader, "POST", notifyUrl, tsHeader, body)) {
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
                case "sign.success": return MAPPER.convertValue(map, WebhookEvent.SignSuccess.class);
                case "payment.completed": return MAPPER.convertValue(map, WebhookEvent.PaymentCompleted.class);
                case "refund.completed": return MAPPER.convertValue(map, WebhookEvent.RefundCompleted.class);
                default:
                    throw new PaygateException(ErrorCode.INVALID_PARAMETER.getCode(), "Unknown webhook event type: " + type);
            }
        } catch (PaygateException e) {
            throw e;
        } catch (Exception e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "Failed to parse webhook payload", e);
        }
    }

    private static String first(Map<String, List<String>> headers, String key) {
        List<String> values = headers.get(key);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }
}
