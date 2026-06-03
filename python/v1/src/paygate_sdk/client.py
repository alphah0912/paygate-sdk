"""
Main entry point for the PayGate Python SDK.

@author alphah
@since 1.0.0
"""

from __future__ import annotations

import json
import time
import uuid
from typing import Any, Dict, Optional, Type, TypeVar

import httpx

from .environment import Environment
from .error_code import ErrorCode
from .exception import PaygateException
from .signature import sign
from .types import (
    CancelRequest,
    CancelResponse,
    CaptureRequest,
    CaptureResponse,
    InquiryPaymentRequest,
    InquiryPaymentResponse,
    InquiryRefundRequest,
    InquiryRefundResponse,
    PayRequest,
    PayResponse,
    RefundRequest,
    RefundResponse,
)

T = TypeVar("T")

_MAX_RETRIES = 3


class PaygateClient:
    """Main entry point for the PayGate SDK."""

    def __init__(
        self,
        api_key: str,
        api_secret: str,
        environment: Environment = Environment.SANDBOX,
        _base_url: Optional[str] = None,  # for testing
    ):
        if not api_key:
            raise ValueError("api_key is required")
        if not api_secret:
            raise ValueError("api_secret is required")
        self._api_key = api_key
        self._api_secret = api_secret
        self._base_url = _base_url or environment.base_url
        self._base_path = environment.base_path if not _base_url else ""
        self._client = httpx.Client(timeout=30.0)

    # ── API methods ────────────────────────────────────────

    def pay(self, request: PayRequest) -> PayResponse:
        return self._execute("/pay", request, PayResponse)

    def capture(self, request: CaptureRequest) -> CaptureResponse:
        return self._execute("/capture", request, CaptureResponse)

    def cancel(self, request: CancelRequest) -> CancelResponse:
        if not request.reason:
            request.reason = "MERCHANT_MANUAL"
        return self._execute("/cancel", request, CancelResponse)

    def inquiry_payment(self, request: InquiryPaymentRequest) -> InquiryPaymentResponse:
        return self._execute("/inquiry-payment", request, InquiryPaymentResponse)

    def inquiry_refund(self, request: InquiryRefundRequest) -> InquiryRefundResponse:
        return self._execute("/inquiry-refund", request, InquiryRefundResponse)

    def refund(self, request: RefundRequest) -> RefundResponse:
        if not request.reason:
            request.reason = "ISV退款"
        return self._execute("/refund", request, RefundResponse)

    # ── Internal ───────────────────────────────────────────

    def _execute(
        self, path: str, request: Any, response_cls: Type[T], attempt: int = 0
    ) -> T:
        body = json.dumps(request.to_dict(), ensure_ascii=False)
        timestamp = str(int(time.time() * 1000))
        signing_path = self._base_path + path
        signature = sign(self._api_secret, "POST", signing_path, timestamp, body)

        try:
            resp = self._client.post(
                self._base_url + path,
                content=body,
                headers={
                    "Content-Type": "application/json",
                    "X-Api-Key": self._api_key,
                    "X-Signature": signature,
                    "X-Timestamp": timestamp,
                    "X-Request-Id": uuid.uuid4().hex[:8],
                },
            )
        except httpx.RequestError as e:
            raise PaygateException(
                ErrorCode.NETWORK_ERROR, f"Network request failed: {e}"
            )

        retryable = resp.status_code in (429, 502, 503, 504)
        if retryable and attempt < _MAX_RETRIES:
            time.sleep(2 ** (attempt + 1))
            return self._execute(path, request, response_cls, attempt + 1)

        if resp.status_code >= 500:
            raise PaygateException(
                ErrorCode.SERVER_ERROR,
                f"Server returned {resp.status_code}: {resp.text}",
                resp.status_code,
            )

        try:
            data: Dict[str, Any] = resp.json()
        except Exception:
            raise PaygateException(
                ErrorCode.UNKNOWN_ERROR, f"Failed to parse response: {resp.text}"
            )

        # Map camelCase JSON keys to snake_case dataclass fields
        data = _camel_to_snake_keys(data)

        result = response_cls(**{k: v for k, v in data.items() if k in response_cls.__dataclass_fields__})  # type: ignore[call-arg]

        if not result.is_success:
            raise PaygateException(result.code, result.message)

        return result


def _camel_to_snake_keys(d: Dict[str, Any]) -> Dict[str, Any]:
    """Convert camelCase dict keys to snake_case for dataclass instantiation."""
    import re

    result: Dict[str, Any] = {}
    for k, v in d.items():
        snake = re.sub(r"([A-Z])", r"_\1", k[0].lower() + k[1:]).lower()
        result[snake] = v
    return result
