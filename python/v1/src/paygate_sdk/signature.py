"""
HMAC-SHA256 signing and verification for PayGate API requests.

Signing string format (newline-separated):
  METHOD
  PATH
  TIMESTAMP
  BODY

Output format: sha256=<base64-hmac>

@author alphah
@since 1.0.0
"""

import base64
import hashlib
import hmac

from .error_code import ErrorCode
from .exception import PaygateException

_SIGNATURE_PREFIX = "sha256="


def sign(api_secret: str, method: str, path: str, timestamp: str, body: str) -> str:
    """
    Compute HMAC-SHA256 signature for a PayGate API request.

    Args:
        api_secret: merchant API secret.
        method: HTTP method, uppercase (e.g. POST).
        path: request path with prefix (e.g. /api/gateway/v1/pay).
        timestamp: Unix millisecond timestamp string.
        body: JSON request body.
    Returns:
        Signature in sha256=<base64> format.
    """
    sign_str = f"{method}\n{path}\n{timestamp}\n{body}"
    try:
        mac = hmac.new(
            api_secret.encode("utf-8"),
            sign_str.encode("utf-8"),
            hashlib.sha256,
        )
        return _SIGNATURE_PREFIX + base64.b64encode(mac.digest()).decode("utf-8")
    except Exception as e:
        raise PaygateException(
            ErrorCode.UNKNOWN_ERROR, f"HMAC signature failed: {e}"
        )


def verify(
    secret: str,
    expected_signature: str,
    method: str,
    path: str,
    timestamp: str,
    body: str,
) -> bool:
    """
    Verify a signature against the expected value.

    Args:
        secret: merchant API secret.
        expected_signature: the signature header value to verify.
        method: HTTP method.
        path: request path.
        timestamp: timestamp used in the original signature.
        body: raw JSON body.
    Returns:
        True if the signature matches.
    """
    actual = sign(secret, method, path, timestamp, body)
    return hmac.compare_digest(actual, expected_signature)
