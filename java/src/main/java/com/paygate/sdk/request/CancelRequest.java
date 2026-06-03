package com.paygate.sdk.request;

/**
 * Request parameters for the {@code /cancel} endpoint.
 * Default reason is {@code "MERCHANT_MANUAL"}.
 *
 * @author alphah
 * @since 1.0.0
 */
public class CancelRequest {

    /** Platform payment request ID to cancel (required) */
    private final String paymentRequestId;
    /** Cancellation reason, defaults to {@code "MERCHANT_MANUAL"} */
    private final String reason;

    private CancelRequest(Builder builder) {
        this.paymentRequestId = builder.paymentRequestId;
        this.reason = builder.reason;
    }

    /** @return the platform payment request ID */
    public String getPaymentRequestId() { return paymentRequestId; }
    /** @return cancellation reason, defaults to {@code "MERCHANT_MANUAL"} */
    public String getReason() { return reason; }

    /** @return a new {@link Builder} */
    public static Builder builder() { return new Builder(); }

    /** Builder for {@link CancelRequest}. */
    public static class Builder {
        private String paymentRequestId;
        private String reason = "MERCHANT_MANUAL";

        /** @param paymentRequestId platform payment request ID (required) */
        public Builder paymentRequestId(String paymentRequestId) { this.paymentRequestId = paymentRequestId; return this; }
        /** @param reason cancellation reason, defaults to {@code "MERCHANT_MANUAL"} */
        public Builder reason(String reason) { this.reason = reason; return this; }

        /** @return the configured {@link CancelRequest} */
        public CancelRequest build() {
            if (paymentRequestId == null) throw new IllegalArgumentException("paymentRequestId is required");
            return new CancelRequest(this);
        }
    }
}
