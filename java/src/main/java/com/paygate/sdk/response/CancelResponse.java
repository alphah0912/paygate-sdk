package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response for the {@code /cancel} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CancelResponse extends ApiResponse {

    /** Cancellation status */
    private String status;

    /** @return cancellation status */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
