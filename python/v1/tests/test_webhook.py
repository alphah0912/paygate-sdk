import json

import pytest
from paygate_sdk.signature import sign
from paygate_sdk.webhook.handler import WebhookHandler
from paygate_sdk.webhook.types import (
    PaymentResultWebhookEvent,
    SignSuccessWebhookEvent,
    PaymentCompletedWebhookEvent,
    RefundCompletedWebhookEvent,
)
from paygate_sdk.exception import PaygateException

ISV_SECRET = "isv_webhook_secret"
MERCHANT_SECRET = "merchant_webhook_secret"
NOTIFY_URL = "https://merchant.example.com/webhook"


class TestWebhookHandler:
    def setup_method(self):
        self.handler = WebhookHandler(ISV_SECRET, MERCHANT_SECRET)

    def test_reject_no_headers(self):
        with pytest.raises(PaygateException, match="No webhook signature header"):
            self.handler.handle({}, "{}", NOTIFY_URL)

    def test_isv_valid_signature(self):
        body = json.dumps({
            "paymentRequestId": "REQ123",
            "status": "SUCCESS",
            "amount": "100.00",
            "currency": "USD",
            "tradeTime": "2024-01-01T00:00:00Z",
            "message": "支付成功",
        })
        ts = "1700000000000"
        sig = sign(ISV_SECRET, "POST", NOTIFY_URL, ts, body)

        event = self.handler.handle(
            {"X-Isv-Signature": sig, "X-Isv-Timestamp": ts}, body, NOTIFY_URL
        )
        assert isinstance(event, PaymentResultWebhookEvent)
        assert event.payment_request_id == "REQ123"
        assert event.status == "SUCCESS"

    def test_isv_invalid_signature(self):
        with pytest.raises(PaygateException, match="signature mismatch"):
            self.handler.handle(
                {"X-Isv-Signature": "sha256=invalid", "X-Isv-Timestamp": "1700000000000"},
                '{"paymentRequestId":"REQ123"}',
                NOTIFY_URL,
            )

    def test_notification_sign_success(self):
        body = json.dumps({
            "type": "sign.success",
            "referenceMerchantId": "M001",
            "message": "签约成功",
            "timestamp": "1700000000000",
        })
        ts = "1700000000000"
        sig = sign(MERCHANT_SECRET, "POST", NOTIFY_URL, ts, body)

        event = self.handler.handle(
            {"X-Webhook-Signature": sig, "X-Webhook-Timestamp": ts}, body, NOTIFY_URL
        )
        assert isinstance(event, SignSuccessWebhookEvent)
        assert event.reference_merchant_id == "M001"

    def test_notification_invalid_signature(self):
        with pytest.raises(PaygateException, match="signature mismatch"):
            self.handler.handle(
                {"X-Webhook-Signature": "sha256=invalid", "X-Webhook-Timestamp": "1700000000000"},
                '{"type":"sign.success"}',
                NOTIFY_URL,
            )

    def test_payment_completed(self):
        body = json.dumps({
            "type": "payment.completed",
            "platformTradeNo": "T001",
            "status": "SUCCESS",
            "timestamp": "1700000000000",
        })
        ts = "1700000000000"
        sig = sign(MERCHANT_SECRET, "POST", NOTIFY_URL, ts, body)
        event = self.handler.handle(
            {"X-Webhook-Signature": sig, "X-Webhook-Timestamp": ts}, body, NOTIFY_URL
        )
        assert isinstance(event, PaymentCompletedWebhookEvent)
        assert event.platform_trade_no == "T001"

    def test_refund_completed(self):
        body = json.dumps({
            "type": "refund.completed",
            "refundTradeNo": "R001",
            "status": "SUCCESS",
            "timestamp": "1700000000000",
        })
        ts = "1700000000000"
        sig = sign(MERCHANT_SECRET, "POST", NOTIFY_URL, ts, body)
        event = self.handler.handle(
            {"X-Webhook-Signature": sig, "X-Webhook-Timestamp": ts}, body, NOTIFY_URL
        )
        assert isinstance(event, RefundCompletedWebhookEvent)
        assert event.refund_trade_no == "R001"

    def test_unknown_type(self):
        body = json.dumps({"type": "unknown.event", "timestamp": "1700000000000"})
        ts = "1700000000000"
        sig = sign(MERCHANT_SECRET, "POST", NOTIFY_URL, ts, body)
        with pytest.raises(PaygateException, match="Unknown webhook event type"):
            self.handler.handle(
                {"X-Webhook-Signature": sig, "X-Webhook-Timestamp": ts}, body, NOTIFY_URL
            )
