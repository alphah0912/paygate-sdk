package com.paygate.sdk.request;

/**
 * Request parameters for the {@code /inquiry-payment} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
public class InquiryPaymentRequest {

    /** Platform payment request ID to query (required) */
    private final String paymentRequestId;

    private InquiryPaymentRequest(Builder builder) {
        this.paymentRequestId = builder.paymentRequestId;
    }

    /** @return the platform payment request ID */
    public String getPaymentRequestId() { return paymentRequestId; }

    /** @return a new {@link Builder} */
    public static Builder builder() { return new Builder(); }

    /** Builder for {@link InquiryPaymentRequest}. */
    public static class Builder {
        private String paymentRequestId;

        /** @param paymentRequestId platform payment request ID (required) */
        public Builder paymentRequestId(String paymentRequestId) { this.paymentRequestId = paymentRequestId; return this; }

        /** @return the configured {@link InquiryPaymentRequest} */
        public InquiryPaymentRequest build() {
            if (paymentRequestId == null) throw new IllegalArgumentException("paymentRequestId is required");
            return new InquiryPaymentRequest(this);
        }
    }
}
