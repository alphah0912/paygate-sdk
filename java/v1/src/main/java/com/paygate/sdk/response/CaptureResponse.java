package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response for the {@code /capture} endpoint.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptureResponse extends ApiResponse {

    /**
     * Platform-assigned capture transaction identifier
     */
    private String captureId;

    /**
     * Capture status, e.g. {@code "SUCCESS"}
     */
    private String status;

    /**
     * @return capture transaction ID
     */
    public String getCaptureId() {
        return captureId;
    }

    public void setCaptureId(String captureId) {
        this.captureId = captureId;
    }

    /**
     * @return capture status, e.g. {@code "SUCCESS"}
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
