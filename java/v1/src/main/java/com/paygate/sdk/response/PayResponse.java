package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for the {@code /pay} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayResponse extends ApiResponse {

    /**
     * URL the buyer should be redirected to for payment completion
     */
    @JsonProperty("redirectUrl")
    private String redirectUrl;

    /**
     * Platform-assigned unique payment request identifier
     */
    @JsonProperty("paymentRequestId")
    private String paymentRequestId;

    /**
     * @return URL to redirect the buyer for payment completion
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * @return platform-assigned payment request ID
     */
    public String getPaymentRequestId() {
        return paymentRequestId;
    }

    public void setPaymentRequestId(String paymentRequestId) {
        this.paymentRequestId = paymentRequestId;
    }
}
