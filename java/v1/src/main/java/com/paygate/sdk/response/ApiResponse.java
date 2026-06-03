package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Base class for all PayGate API responses.
 * Contains the common {@code code} and {@code message} fields present in every response.
 *
 * <p>A {@code code} of {@code "200"} indicates success; any other value is an error.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ApiResponse {

    /**
     * Response code from the API, "200" indicates success
     */
    private int code;

    /**
     * Human-readable message accompanying the response code
     */
    private String message;

    /**
     * @return response code, "200" means success
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return human-readable response message
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return true if the response code indicates success
     */
    public boolean isSuccess() {
        return code == 200;
    }
}
