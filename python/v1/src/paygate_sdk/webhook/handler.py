"""
Webhook handler — validates incoming notifications from the PayGate platform.

Two webhook types, both using HMAC-SHA256:
- ISV Webhook: headers X-Isv-Signature + X-Isv-Timestamp.
- NotificationService Webhook: headers X-Webhook-Signature + X-Webhook-Timestamp.

Sign string: POST\n{notifyUrl}\n{timestamp}\n{body}

@author alphah
@since 1.0.0
"""

from __future__ import annotations

import json
from typing import Any, Dict, List, Mapping, Optional, Union

import re

from ..error_code import ErrorCode
from ..exception import PaygateException
from ..signature import verify
from .types import (
    WebhookEvent,
    PaymentCompletedWebhookEvent,
    PaymentResultWebhookEvent,
    RefundCompletedWebhookEvent,
    SignSuccessWebhookEvent,
)

_ISV_SIGNATURE = "X-Isv-Signature"
_ISV_TIMESTAMP = "X-Isv-Timestamp"
_WEBHOOK_SIGNATURE = "X-Webhook-Signature"
_WEBHOOK_TIMESTAMP = "X-Webhook-Timestamp"


_HEADER_VALUE = Union[str, List[str], None]


class WebhookHandler:
    """Handles incoming webhook notifications."""

    def __init__(self, isv_webhook_secret: str, merchant_webhook_secret: Optional[str]):
        self._secret = isv_webhook_secret
        self._merchant_secret = merchant_webhook_secret

    def handle(
        self,
        headers: Mapping[str, _HEADER_VALUE],
        body: str,
        notify_url: str,
    ) -> WebhookEvent:
        """
        Process an incoming webhook request.

        Args:
            headers: request headers.
            body: raw JSON body.
            notify_url: full URL the webhook was sent to.
        Returns:
            Typed webhook event.
        Raises:
            PaygateException: if verification or parsing fails.
        """
        lower = _normalize_headers(headers)

        if _ISV_SIGNATURE.lower() in lower:
            return self._handle_isv(lower, body, notify_url)
        if _WEBHOOK_SIGNATURE.lower() in lower:
            return self._handle_notification(lower, body, notify_url)
        raise PaygateException(
            ErrorCode.INVALID_SIGNATURE, "No webhook signature header found"
        )

    def _handle_isv(self, h: Dict[str, str], body: str, url: str) -> WebhookEvent:
        sig = h.get(_ISV_SIGNATURE.lower())
        ts = h.get(_ISV_TIMESTAMP.lower())
        if not sig or not ts:
            raise PaygateException(
                ErrorCode.INVALID_SIGNATURE, "Missing ISV webhook signature headers"
            )
        if not verify(self._secret, sig, "POST", url, ts, body):
            raise PaygateException(
                ErrorCode.INVALID_SIGNATURE, "ISV webhook signature mismatch"
            )
        try:
            data = _camel_to_snake_keys(json.loads(body))
            return PaymentResultWebhookEvent(**{k: v for k, v in data.items() if k in PaymentResultWebhookEvent.__dataclass_fields__})
        except Exception as e:
            raise PaygateException(
                ErrorCode.UNKNOWN_ERROR, f"Failed to parse ISV webhook payload: {e}"
            )

    def _handle_notification(self, h: Dict[str, str], body: str, url: str) -> WebhookEvent:
        sig = h.get(_WEBHOOK_SIGNATURE.lower())
        ts = h.get(_WEBHOOK_TIMESTAMP.lower())

        # Platform sends signature only when a secret is configured.
        # If sig present, secret must be configured and must pass verification.
        if sig and ts:
            if not self._merchant_secret:
                raise PaygateException(
                    ErrorCode.INVALID_SIGNATURE,
                    "Webhook signature received but no merchant secret configured",
                )
            if not verify(self._merchant_secret, sig, "POST", url, ts, body):
                raise PaygateException(
                    ErrorCode.INVALID_SIGNATURE, "Webhook signature mismatch"
                )

        try:
            data: Dict[str, Any] = json.loads(body)
        except Exception as e:
            raise PaygateException(
                ErrorCode.UNKNOWN_ERROR, f"Failed to parse webhook payload: {e}"
            )

        typ = data.get("type")
        if not typ:
            raise PaygateException(
                ErrorCode.INVALID_PARAMETER, "Webhook payload missing type field"
            )

        type_map = {
            "sign.success": SignSuccessWebhookEvent,
            "payment.completed": PaymentCompletedWebhookEvent,
            "refund.completed": RefundCompletedWebhookEvent,
        }
        cls = type_map.get(typ)
        if not cls:
            raise PaygateException(
                ErrorCode.INVALID_PARAMETER, f"Unknown webhook event type: {typ}"
            )
        data = _camel_to_snake_keys(data)
        return cls(**{k: v for k, v in data.items() if k in cls.__dataclass_fields__})


def _camel_to_snake_keys(d: Dict[str, Any]) -> Dict[str, Any]:
    """Convert camelCase dict keys to snake_case."""
    result: Dict[str, Any] = {}
    for k, v in d.items():
        snake = re.sub(r"([A-Z])", r"_\1", k[0].lower() + k[1:]).lower()
        result[snake] = v
    return result


def _normalize_headers(headers: Mapping[str, _HEADER_VALUE]) -> Dict[str, str]:
    """Normalize headers to lowercase keys with single string values."""
    result: Dict[str, str] = {}
    for k, v in headers.items():
        value = v[0] if isinstance(v, list) else v
        if value is not None:
            result[k.lower()] = value
    return result
