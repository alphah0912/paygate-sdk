package com.paygate.sdk.request;

/**
 * Request parameters for the {@code /refund} endpoint.
 * Default reason is {@code "ISV退款"}.
 *
 * @author alphah
 * @since 1.0.0
 */
public class RefundRequest {

    /**
     * Platform payment request ID to refund (required)
     */
    private final String paymentRequestId;
    /**
     * Refund amount as a string, e.g. {@code "50.00"} (required)
     */
    private final String refundAmount;
    /**
     * Refund reason, defaults to {@code "ISV退款"}
     */
    private final String reason;

    private RefundRequest(Builder builder) {
        this.paymentRequestId = builder.paymentRequestId;
        this.refundAmount = builder.refundAmount;
        this.reason = builder.reason;
    }

    /**
     * @return the platform payment request ID
     */
    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    /**
     * @return refund amount as a string
     */
    public String getRefundAmount() {
        return refundAmount;
    }

    /**
     * @return refund reason, defaults to {@code "ISV退款"}
     */
    public String getReason() {
        return reason;
    }

    /**
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link RefundRequest}.
     */
    public static class Builder {
        private String paymentRequestId;
        private String refundAmount;
        /**
         * Platform-defined refund reason, defaults to "ISV退款" (server convention)
         */
        private String reason = "ISV退款";

        /**
         * @param paymentRequestId platform payment request ID (required)
         */
        public Builder paymentRequestId(String paymentRequestId) {
            this.paymentRequestId = paymentRequestId;
            return this;
        }

        /**
         * @param refundAmount refund amount (required)
         */
        public Builder refundAmount(String refundAmount) {
            this.refundAmount = refundAmount;
            return this;
        }

        /**
         * @param reason refund reason, defaults to {@code "ISV退款"}
         */
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        /**
         * @return the configured {@link RefundRequest}
         */
        public RefundRequest build() {
            if (paymentRequestId == null) throw new IllegalArgumentException("paymentRequestId is required");
            if (refundAmount == null) throw new IllegalArgumentException("refundAmount is required");
            return new RefundRequest(this);
        }
    }
}
