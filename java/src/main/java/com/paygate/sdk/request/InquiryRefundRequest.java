package com.paygate.sdk.request;

/**
 * Request parameters for the {@code /inquiry-refund} endpoint.
 * Supply at least one of {@code refundTransactionId} or {@code paymentRequestId}.
 *
 * @author alphah
 * @since 1.0.0
 */
public class InquiryRefundRequest {

    /** Refund transaction ID (supply this OR paymentRequestId) */
    private final String refundTransactionId;
    /** Payment request ID (supply this OR refundTransactionId) */
    private final String paymentRequestId;

    private InquiryRefundRequest(Builder builder) {
        this.refundTransactionId = builder.refundTransactionId;
        this.paymentRequestId = builder.paymentRequestId;
    }

    /** @return refund transaction ID (optional, if paymentRequestId is supplied) */
    public String getRefundTransactionId() { return refundTransactionId; }
    /** @return platform payment request ID (optional, if refundTransactionId is supplied) */
    public String getPaymentRequestId() { return paymentRequestId; }

    /** @return a new {@link Builder} */
    public static Builder builder() { return new Builder(); }

    /** Builder for {@link InquiryRefundRequest}. */
    public static class Builder {
        private String refundTransactionId;
        private String paymentRequestId;

        /** @param refundTransactionId refund transaction ID */
        public Builder refundTransactionId(String refundTransactionId) { this.refundTransactionId = refundTransactionId; return this; }
        /** @param paymentRequestId payment request ID */
        public Builder paymentRequestId(String paymentRequestId) { this.paymentRequestId = paymentRequestId; return this; }

        /** @return the configured {@link InquiryRefundRequest} */
        public InquiryRefundRequest build() {
            if (refundTransactionId == null && paymentRequestId == null) {
                throw new IllegalArgumentException("refundTransactionId or paymentRequestId is required");
            }
            return new InquiryRefundRequest(this);
        }
    }
}
