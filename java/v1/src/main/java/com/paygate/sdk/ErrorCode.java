package com.paygate.sdk;

/**
 * Unified error codes returned by the PayGate API.
 * Each SDK language maps to the same codes defined in {@code spec/error-codes.yaml}.
 *
 * @author alphah
 * @since 1.0.0
 */
public enum ErrorCode {

    INVALID_API_KEY("40001", "Invalid API key"),
    INVALID_SIGNATURE("40002", "Signature verification failed"),
    REQUEST_EXPIRED("40003", "Request timestamp expired"),
    INVALID_PARAMETER("40004", "Invalid parameter"),
    PAYMENT_NOT_FOUND("40401", "Payment not found"),
    REFUND_NOT_FOUND("40402", "Refund not found"),
    INVALID_PAYMENT_STATUS("40901", "Invalid payment status for this operation"),
    DUPLICATE_REQUEST("40902", "Duplicate request"),
    NETWORK_ERROR("50001", "Network request failed"),
    SERVER_ERROR("50002", "Server returned error"),
    RATE_LIMITED("50003", "Rate limited"),
    UNKNOWN_ERROR("59999", "Unknown error");

    /** Numeric error code string, e.g. {@code "40001"} */
    private final String code;

    /** Human-readable default message, may be overridden by the API response */
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** @return the numeric error code string, e.g. {@code "40001"} */
    public String getCode() {
        return code;
    }

    /** @return the human-readable default message */
    public String getDefaultMessage() {
        return defaultMessage;
    }

    /**
     * Resolves a raw error code string to its enum constant.
     *
     * @param code numeric error code from the API response
     * @return matching {@link ErrorCode}, or {@link #UNKNOWN_ERROR} if unrecognized
     */
    public static ErrorCode fromCode(String code) {
        for (ErrorCode e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return UNKNOWN_ERROR;
    }
}
