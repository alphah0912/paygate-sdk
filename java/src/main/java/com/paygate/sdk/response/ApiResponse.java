package com.paygate.sdk.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Base class for all PayGate API responses.
 * Contains the common {@code code} and {@code message} fields present in every response.
 *
 * <p>A {@code code} of {@code "0"} indicates success; any other value is an error.
 *
 * @author alphah
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ApiResponse {

    /** Response code from the API, {@code "0"} indicates success */
    private String code;

    /** Human-readable message accompanying the response code */
    private String message;

    /** @return response code, {@code "0"} means success */
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    /** @return human-readable response message */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    /** @return {@code true} if the response code is {@code "0"} */
    public boolean isSuccess() {
        return "0".equals(code);
    }
}
