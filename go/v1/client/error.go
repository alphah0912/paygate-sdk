package client

import "fmt"

// ErrorCode is a unified PayGate error code.
type ErrorCode string

const (
	ErrInvalidAPIKey        ErrorCode = "40001"
	ErrInvalidSignature     ErrorCode = "40002"
	ErrRequestExpired       ErrorCode = "40003"
	ErrInvalidParameter     ErrorCode = "40004"
	ErrPaymentNotFound      ErrorCode = "40401"
	ErrRefundNotFound       ErrorCode = "40402"
	ErrInvalidPaymentStatus ErrorCode = "40901"
	ErrDuplicateRequest     ErrorCode = "40902"
	ErrNetworkError         ErrorCode = "50001"
	ErrServerError          ErrorCode = "50002"
	ErrRateLimited          ErrorCode = "50003"
	ErrUnknownError         ErrorCode = "59999"
)

var defaultMessages = map[ErrorCode]string{
	ErrInvalidAPIKey:        "Invalid API key",
	ErrInvalidSignature:     "Signature verification failed",
	ErrRequestExpired:       "Request timestamp expired",
	ErrInvalidParameter:     "Invalid parameter",
	ErrPaymentNotFound:      "Payment not found",
	ErrRefundNotFound:       "Refund not found",
	ErrInvalidPaymentStatus: "Invalid payment status for this operation",
	ErrDuplicateRequest:     "Duplicate request",
	ErrNetworkError:         "Network request failed",
	ErrServerError:          "Server returned error",
	ErrRateLimited:          "Rate limited",
	ErrUnknownError:         "Unknown error",
}

// DefaultMessage returns the default human-readable message.
func (c ErrorCode) DefaultMessage() string {
	if msg, ok := defaultMessages[c]; ok {
		return msg
	}
	return "Unknown error"
}

// PaygateError is an error with an error code and optional HTTP status.
type PaygateError struct {
	Code       ErrorCode
	Message    string
	HTTPStatus int
}

func (e *PaygateError) Error() string {
	if e.HTTPStatus > 0 {
		return fmt.Sprintf("[%s] %s (HTTP %d)", e.Code, e.Message, e.HTTPStatus)
	}
	return fmt.Sprintf("[%s] %s", e.Code, e.Message)
}

// NewPaygateError creates a PaygateError.
func NewPaygateError(code ErrorCode, msg string) *PaygateError {
	return &PaygateError{Code: code, Message: msg}
}
