"""
Exception thrown on PayGate API errors or client-side failures.

@author alphah
@since 1.0.0
"""

from .error_code import ErrorCode, error_code_from_code


class PaygateException(Exception):
    """Always carries an error_code that maps to ErrorCode."""

    def __init__(self, error_code: str, message: str, http_status: int = 0):
        super().__init__(message)
        # Numeric error code string.
        self.error_code = error_code
        # HTTP status code from the server, or 0 if not applicable.
        self.http_status = http_status

    @property
    def error_code_enum(self) -> ErrorCode:
        """The ErrorCode enum constant matching this error."""
        return error_code_from_code(self.error_code)
