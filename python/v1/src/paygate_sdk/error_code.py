"""
Unified error codes returned by the PayGate API.

@author alphah
@since 1.0.0
"""

from enum import Enum


class ErrorCode(str, Enum):
    """Each SDK language maps to the same codes defined in spec/error-codes.yaml."""

    INVALID_API_KEY = "40001"
    INVALID_SIGNATURE = "40002"
    REQUEST_EXPIRED = "40003"
    INVALID_PARAMETER = "40004"
    PAYMENT_NOT_FOUND = "40401"
    REFUND_NOT_FOUND = "40402"
    INVALID_PAYMENT_STATUS = "40901"
    DUPLICATE_REQUEST = "40902"
    NETWORK_ERROR = "50001"
    SERVER_ERROR = "50002"
    RATE_LIMITED = "50003"
    UNKNOWN_ERROR = "59999"


# Default human-readable messages.
_DEFAULT_MESSAGES = {
    "40001": "Invalid API key",
    "40002": "Signature verification failed",
    "40003": "Request timestamp expired",
    "40004": "Invalid parameter",
    "40401": "Payment not found",
    "40402": "Refund not found",
    "40901": "Invalid payment status for this operation",
    "40902": "Duplicate request",
    "50001": "Network request failed",
    "50002": "Server returned error",
    "50003": "Rate limited",
    "59999": "Unknown error",
}


def error_code_from_code(code: str) -> ErrorCode:
    """Resolve a raw error code string to its enum constant."""
    try:
        return ErrorCode(code)
    except ValueError:
        return ErrorCode.UNKNOWN_ERROR


def error_code_message(code: str) -> str:
    """Return the default human-readable message for an error code."""
    return _DEFAULT_MESSAGES.get(code, "Unknown error")
