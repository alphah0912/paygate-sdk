package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response for the {@code /inquiry-payment} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InquiryPaymentResponse extends ApiResponse {

    /** Trade-level status */
    private String tradeStatus;

    /** Payment identifier */
    private String paymentId;

    /** Payment status, e.g. {@code "SUCCESS"} */
    private String paymentStatus;

    /** @return trade-level status */
    public String getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }

    /** @return payment ID */
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    /** @return payment status */
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
