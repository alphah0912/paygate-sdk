"""
Request and response types for the 6 PayGate API endpoints.
Using dataclasses with optional fields defaulting to None.

@author alphah
@since 1.0.0
"""

from dataclasses import asdict, dataclass, field
from typing import Any, Dict, Optional


def _to_dict(obj) -> Dict[str, Any]:
    """Serialize a dataclass to dict, omitting None fields."""
    return {k: v for k, v in asdict(obj).items() if v is not None}


# ── Requests ──────────────────────────────────────────────


@dataclass
class PayRequest:
    """Required: amount, paymentMethodCode, terminalType, settlementCurrency."""

    amount: str = ""
    payment_method_code: str = ""
    terminal_type: str = ""
    settlement_currency: str = ""
    currency: Optional[str] = None
    order_description: Optional[str] = None
    buyer_country: Optional[str] = None
    reference_buyer_id: Optional[str] = None
    buyer_phone_no: Optional[str] = None
    payment_method: Optional[str] = None
    available_payment_method: Optional[str] = None
    saved_payment_methods: Optional[str] = None
    os_type: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        d = _to_dict(self)
        # Map Python field names back to API JSON names
        rename = {
            "payment_method_code": "paymentMethodCode",
            "terminal_type": "terminalType",
            "settlement_currency": "settlementCurrency",
            "order_description": "orderDescription",
            "buyer_country": "buyerCountry",
            "reference_buyer_id": "referenceBuyerId",
            "buyer_phone_no": "buyerPhoneNo",
            "payment_method": "paymentMethod",
            "available_payment_method": "availablePaymentMethod",
            "saved_payment_methods": "savedPaymentMethods",
            "os_type": "osType",
        }
        result = {}
        for k, v in d.items():
            result[rename.get(k, k)] = v
        return result


@dataclass
class CaptureRequest:
    payment_request_id: str = ""

    def to_dict(self) -> Dict[str, Any]:
        return {"paymentRequestId": self.payment_request_id}


@dataclass
class CancelRequest:
    payment_request_id: str = ""
    reason: str = "MERCHANT_MANUAL"

    def to_dict(self) -> Dict[str, Any]:
        return {"paymentRequestId": self.payment_request_id, "reason": self.reason}


@dataclass
class InquiryPaymentRequest:
    payment_request_id: str = ""

    def to_dict(self) -> Dict[str, Any]:
        return {"paymentRequestId": self.payment_request_id}


@dataclass
class InquiryRefundRequest:
    refund_transaction_id: Optional[str] = None
    payment_request_id: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        d: Dict[str, Any] = {}
        if self.refund_transaction_id:
            d["refundTransactionId"] = self.refund_transaction_id
        if self.payment_request_id:
            d["paymentRequestId"] = self.payment_request_id
        return d


@dataclass
class RefundRequest:
    payment_request_id: str = ""
    refund_amount: str = ""
    reason: str = "ISV退款"

    def to_dict(self) -> Dict[str, Any]:
        return {
            "paymentRequestId": self.payment_request_id,
            "refundAmount": self.refund_amount,
            "reason": self.reason,
        }


# ── Responses ──────────────────────────────────────────────


@dataclass
class ApiResponse:
    code: str = ""
    message: str = ""

    @property
    def is_success(self) -> bool:
        return self.code == "200"


@dataclass
class PayResponse(ApiResponse):
    redirect_url: str = ""
    payment_request_id: str = ""


@dataclass
class CaptureResponse(ApiResponse):
    capture_id: str = ""
    status: str = ""


@dataclass
class CancelResponse(ApiResponse):
    status: str = ""


@dataclass
class InquiryPaymentResponse(ApiResponse):
    trade_status: str = ""
    payment_id: str = ""
    payment_status: str = ""


@dataclass
class InquiryRefundResponse(ApiResponse):
    trade_status: str = ""
    refund_id: str = ""
    refund_status: str = ""


@dataclass
class RefundResponse(ApiResponse):
    refund_transaction_id: str = ""
    refund_status: str = ""
