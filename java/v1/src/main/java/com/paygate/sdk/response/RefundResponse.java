package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response for the {@code /refund} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundResponse extends ApiResponse {

    /**
     * Platform-assigned refund transaction identifier
     */
    private String refundTransactionId;

    /**
     * Refund status, e.g. {@code "SUCCESS"}
     */
    private String refundStatus;

    /**
     * @return refund transaction ID
     */
    public String getRefundTransactionId() {
        return refundTransactionId;
    }

    public void setRefundTransactionId(String refundTransactionId) {
        this.refundTransactionId = refundTransactionId;
    }

    /**
     * @return refund status
     */
    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }
}
