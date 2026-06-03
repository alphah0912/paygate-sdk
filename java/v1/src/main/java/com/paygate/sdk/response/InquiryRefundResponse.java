package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response for the {@code /inquiry-refund} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InquiryRefundResponse extends ApiResponse {

    /**
     * Trade-level status
     */
    private String tradeStatus;

    /**
     * Refund transaction identifier
     */
    private String refundId;

    /**
     * Refund status, e.g. {@code "SUCCESS"}
     */
    private String refundStatus;

    /**
     * @return trade-level status
     */
    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    /**
     * @return refund transaction ID
     */
    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
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
