"""
Webhook event type definitions.

@author alphah
@since 1.0.0
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any, Dict, Optional, Union


@dataclass
class PaymentResultInfo:
    three_ds_result: Optional[str] = None


@dataclass
class PaymentResultWebhookEvent:
    payment_request_id: str = ""
    status: str = ""
    amount: str = ""
    currency: str = ""
    trade_time: str = ""
    message: str = ""
    payment_result_info: Optional[Dict[str, Any]] = None
    type: str = "payment.result"


@dataclass
class SignSuccessWebhookEvent:
    reference_merchant_id: str = ""
    message: str = ""
    timestamp: str = ""
    type: str = "sign.success"


@dataclass
class PaymentCompletedWebhookEvent:
    platform_trade_no: str = ""
    status: str = ""
    timestamp: str = ""
    type: str = "payment.completed"


@dataclass
class RefundCompletedWebhookEvent:
    refund_trade_no: str = ""
    status: str = ""
    timestamp: str = ""
    type: str = "refund.completed"


WebhookEvent = Union[
    PaymentResultWebhookEvent,
    SignSuccessWebhookEvent,
    PaymentCompletedWebhookEvent,
    RefundCompletedWebhookEvent,
]
