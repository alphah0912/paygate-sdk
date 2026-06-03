package com.paygate.sdk;

import com.paygate.sdk.webhook.WebhookEvent;
import com.paygate.sdk.webhook.WebhookHandler;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebhookHandlerTest {

    private static final String WEBHOOK_SECRET = "webhook_secret";
    private static final String NOTIFY_URL = "https://merchant.example.com/webhook";

    private final WebhookHandler handler = new WebhookHandler(WEBHOOK_SECRET);

    @Test
    void shouldRejectRequestWithoutWebhookHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        assertThatThrownBy(() -> handler.handle(headers, "{}", NOTIFY_URL))
                .isInstanceOf(PaygateException.class)
                .hasMessageContaining("No webhook signature header");
    }

    @Test
    void shouldVerifyIsvWebhookWithValidSignature() {
        String body = "{\"paymentRequestId\":\"REQ123\",\"status\":\"SUCCESS\",\"amount\":\"100.00\",\"currency\":\"USD\",\"tradeTime\":\"2024-01-01T00:00:00Z\",\"message\":\"支付成功\"}";
        String timestamp = "1700000000000";
        String sig = SignatureUtil.sign(WEBHOOK_SECRET, "POST", NOTIFY_URL, timestamp, body);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Isv-Signature", Collections.singletonList(sig));
        headers.put("X-Isv-Timestamp", Collections.singletonList(timestamp));

        WebhookEvent event = handler.handle(headers, body, NOTIFY_URL);

        assertThat(event).isInstanceOf(WebhookEvent.PaymentResult.class);
        WebhookEvent.PaymentResult result = (WebhookEvent.PaymentResult) event;
        assertThat(result.getPaymentRequestId()).isEqualTo("REQ123");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldRejectIsvWebhookWithInvalidSignature() {
        String body = "{\"paymentRequestId\":\"REQ123\"}";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Isv-Signature", Collections.singletonList("sha256=invalid"));
        headers.put("X-Isv-Timestamp", Collections.singletonList("1700000000000"));

        assertThatThrownBy(() -> handler.handle(headers, body, NOTIFY_URL))
                .isInstanceOf(PaygateException.class)
                .hasMessageContaining("signature mismatch");
    }

    @Test
    void shouldHandleNotificationSignSuccessEvent() {
        String body = "{\"type\":\"sign.success\",\"referenceMerchantId\":\"M001\",\"message\":\"签约成功\",\"timestamp\":\"1700000000000\"}";
        String timestamp = "1700000000000";
        String sig = SignatureUtil.sign(WEBHOOK_SECRET, "POST", NOTIFY_URL, timestamp, body);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", Collections.singletonList(sig));
        headers.put("X-Webhook-Timestamp", Collections.singletonList(timestamp));

        WebhookEvent event = handler.handle(headers, body, NOTIFY_URL);

        assertThat(event).isInstanceOf(WebhookEvent.SignSuccess.class);
        WebhookEvent.SignSuccess ss = (WebhookEvent.SignSuccess) event;
        assertThat(ss.getReferenceMerchantId()).isEqualTo("M001");
    }

    @Test
    void shouldRejectNotificationWithInvalidSignature() {
        String body = "{\"type\":\"sign.success\",\"referenceMerchantId\":\"M001\",\"timestamp\":\"1700000000000\"}";

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", Collections.singletonList("sha256=invalid"));
        headers.put("X-Webhook-Timestamp", Collections.singletonList("1700000000000"));

        assertThatThrownBy(() -> handler.handle(headers, body, NOTIFY_URL))
                .isInstanceOf(PaygateException.class)
                .hasMessageContaining("signature mismatch");
    }

    @Test
    void shouldHandlePaymentCompletedEvent() {
        String body = "{\"type\":\"payment.completed\",\"platformTradeNo\":\"T001\",\"status\":\"SUCCESS\",\"timestamp\":\"1700000000000\"}";
        String timestamp = "1700000000000";
        String sig = SignatureUtil.sign(WEBHOOK_SECRET, "POST", NOTIFY_URL, timestamp, body);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", Collections.singletonList(sig));
        headers.put("X-Webhook-Timestamp", Collections.singletonList(timestamp));

        WebhookEvent event = handler.handle(headers, body, NOTIFY_URL);

        assertThat(event).isInstanceOf(WebhookEvent.PaymentCompleted.class);
        WebhookEvent.PaymentCompleted pc = (WebhookEvent.PaymentCompleted) event;
        assertThat(pc.getPlatformTradeNo()).isEqualTo("T001");
    }

    @Test
    void shouldHandleRefundCompletedEvent() {
        String body = "{\"type\":\"refund.completed\",\"refundTradeNo\":\"R001\",\"status\":\"SUCCESS\",\"timestamp\":\"1700000000000\"}";
        String timestamp = "1700000000000";
        String sig = SignatureUtil.sign(WEBHOOK_SECRET, "POST", NOTIFY_URL, timestamp, body);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", Collections.singletonList(sig));
        headers.put("X-Webhook-Timestamp", Collections.singletonList(timestamp));

        WebhookEvent event = handler.handle(headers, body, NOTIFY_URL);

        assertThat(event).isInstanceOf(WebhookEvent.RefundCompleted.class);
        WebhookEvent.RefundCompleted rc = (WebhookEvent.RefundCompleted) event;
        assertThat(rc.getRefundTradeNo()).isEqualTo("R001");
    }

    @Test
    void shouldRejectUnknownNotificationType() {
        String body = "{\"type\":\"unknown.event\",\"timestamp\":\"1700000000000\"}";
        String timestamp = "1700000000000";
        String sig = SignatureUtil.sign(WEBHOOK_SECRET, "POST", NOTIFY_URL, timestamp, body);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("X-Webhook-Signature", Collections.singletonList(sig));
        headers.put("X-Webhook-Timestamp", Collections.singletonList(timestamp));

        assertThatThrownBy(() -> handler.handle(headers, body, NOTIFY_URL))
                .isInstanceOf(PaygateException.class)
                .hasMessageContaining("Unknown webhook event type");
    }
}
