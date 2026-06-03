package com.paygate.sdk;

/**
 * Exception thrown when the PayGate API returns an error,
 * or when a client-side error (network, serialization) occurs.
 *
 * <p>Always carries an {@code errorCode} that maps to {@link ErrorCode}.
 *
 * @author alphah
 * @since 1.0.0
 */
public class PaygateException extends RuntimeException {

    /** Numeric error code string, maps to {@link ErrorCode} */
    private final String errorCode;

    /** HTTP status code from the server response, or 0 if N/A */
    private final int httpStatus;

    /**
     * @param errorCode numeric error code from the API or SDK
     * @param message   human-readable description
     */
    public PaygateException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = 0;
    }

    /**
     * @param errorCode  numeric error code
     * @param message    human-readable description
     * @param httpStatus HTTP status code returned by the server, or 0
     */
    public PaygateException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * @param errorCode numeric error code
     * @param message   human-readable description
     * @param cause     underlying exception
     */
    public PaygateException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = 0;
    }

    /** @return numeric error code string */
    public String getErrorCode() {
        return errorCode;
    }

    /** @return HTTP status code from the server, or 0 if not applicable */
    public int getHttpStatus() {
        return httpStatus;
    }

    /** @return the {@link ErrorCode} enum constant matching this error */
    public ErrorCode getErrorCodeEnum() {
        return ErrorCode.fromCode(errorCode);
    }
}
