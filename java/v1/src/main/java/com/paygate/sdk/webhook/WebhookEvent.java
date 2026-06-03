package com.paygate.sdk.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Marker interface for all webhook event types.
 * Obtain via {@link WebhookHandler#handle}.
 *
 * @author alphah
 * @since 1.0.0
 */
public interface WebhookEvent {

    /** @return the event type identifier, e.g. {@code "payment.result"} */
    String getType();

    /**
     * ISV webhook: payment result notification pushed by the platform.
     * Signed with HMAC-SHA256; verified by {@link WebhookHandler}.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PaymentResult implements WebhookEvent {
        /** Platform payment request ID */
        private String paymentRequestId;
        /** Payment status, e.g. {@code "SUCCESS"}, {@code "FAIL"} */
        private String status;
        /** Payment amount */
        private String amount;
        /** Transaction currency */
        private String currency;
        /** Trade timestamp */
        private String tradeTime;
        /** Status description (Chinese) */
        private String message;
        /** Extended payment result information, may be {@code null} */
        private PaymentResultInfo paymentResultInfo;

        @Override
        public String getType() { return "payment.result"; }

        /** @return platform payment request ID */
        public String getPaymentRequestId() { return paymentRequestId; }
        public void setPaymentRequestId(String paymentRequestId) { this.paymentRequestId = paymentRequestId; }

        /** @return payment status, e.g. {@code "SUCCESS"}, {@code "FAIL"} */
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        /** @return payment amount */
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }

        /** @return transaction currency */
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        /** @return trade timestamp */
        public String getTradeTime() { return tradeTime; }
        public void setTradeTime(String tradeTime) { this.tradeTime = tradeTime; }

        /** @return status description (Chinese) */
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        /** @return extended payment result info, may be {@code null} */
        public PaymentResultInfo getPaymentResultInfo() { return paymentResultInfo; }
        public void setPaymentResultInfo(PaymentResultInfo paymentResultInfo) { this.paymentResultInfo = paymentResultInfo; }
    }

    /** Nested payment result details. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PaymentResultInfo {
        /** 3DS authentication result string, may be {@code null} */
        private String threeDSResult;

        /** @return 3DS authentication result */
        public String getThreeDSResult() { return threeDSResult; }
        public void setThreeDSResult(String threeDSResult) { this.threeDSResult = threeDSResult; }
    }

    /**
     * NotificationService event: merchant sign-up completed.
     * Verified by matching {@code X-Webhook-Secret} header.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class SignSuccess implements WebhookEvent {
        /** Merchant reference identifier */
        private String referenceMerchantId;
        /** Status description */
        private String message;
        /** Event timestamp */
        private String timestamp;

        @Override
        public String getType() { return "sign.success"; }

        /** @return merchant reference ID */
        public String getReferenceMerchantId() { return referenceMerchantId; }
        public void setReferenceMerchantId(String referenceMerchantId) { this.referenceMerchantId = referenceMerchantId; }

        /** @return status description */
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        /** @return event timestamp */
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /** NotificationService event: payment completed. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class PaymentCompleted implements WebhookEvent {
        /** Platform trade number */
        private String platformTradeNo;
        /** Payment status */
        private String status;
        /** Event timestamp */
        private String timestamp;

        @Override
        public String getType() { return "payment.completed"; }

        /** @return platform trade number */
        public String getPlatformTradeNo() { return platformTradeNo; }
        public void setPlatformTradeNo(String platformTradeNo) { this.platformTradeNo = platformTradeNo; }

        /** @return payment status */
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        /** @return event timestamp */
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    /** NotificationService event: refund completed. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    class RefundCompleted implements WebhookEvent {
        /** Refund trade number */
        private String refundTradeNo;
        /** Refund status */
        private String status;
        /** Event timestamp */
        private String timestamp;

        @Override
        public String getType() { return "refund.completed"; }

        /** @return refund trade number */
        public String getRefundTradeNo() { return refundTradeNo; }
        public void setRefundTradeNo(String refundTradeNo) { this.refundTradeNo = refundTradeNo; }

        /** @return refund status */
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        /** @return event timestamp */
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
