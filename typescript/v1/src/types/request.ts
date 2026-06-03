/**
 * Request parameters for the `/pay` endpoint.
 * Required: amount, paymentMethodCode, terminalType, settlementCurrency.
 *
 * @author alphah
 * @since 1.0.0
 */
export interface PayRequest {
  /** Payment amount, e.g. "100.00" */
  amount: string;
  /** Payment method code, e.g. "ALIPAY_CN" */
  paymentMethodCode: string;
  /** Terminal type: "WEB", "APP", "WAP" */
  terminalType: string;
  /** Settlement currency, e.g. "USD" */
  settlementCurrency: string;
  /** Transaction currency (optional) */
  currency?: string;
  /** Order description (optional) */
  orderDescription?: string;
  /** ISO 3166-1 buyer country code (optional) */
  buyerCountry?: string;
  /** Merchant's reference buyer ID (optional) */
  referenceBuyerId?: string;
  /** Buyer phone number (optional) */
  buyerPhoneNo?: string;
  /** Payment method detail object as JSON string (optional) */
  paymentMethod?: string;
  /** Available payment methods as JSON string (optional) */
  availablePaymentMethod?: string;
  /** Saved payment methods as JSON string (optional) */
  savedPaymentMethods?: string;
  /** OS type: "ANDROID" or "IOS" (optional) */
  osType?: string;
}

/**
 * Request parameters for the `/capture` endpoint.
 */
export interface CaptureRequest {
  /** Platform payment request ID to capture */
  paymentRequestId: string;
}

/**
 * Request parameters for the `/cancel` endpoint.
 * Default reason is "MERCHANT_MANUAL".
 */
export interface CancelRequest {
  /** Platform payment request ID to cancel */
  paymentRequestId: string;
  /** Cancellation reason, defaults to "MERCHANT_MANUAL" */
  reason?: string;
}

/**
 * Request parameters for the `/inquiry-payment` endpoint.
 */
export interface InquiryPaymentRequest {
  /** Platform payment request ID to query */
  paymentRequestId: string;
}

/**
 * Request parameters for the `/inquiry-refund` endpoint.
 * Supply at least one of refundTransactionId or paymentRequestId.
 */
export interface InquiryRefundRequest {
  /** Refund transaction ID (optional, if paymentRequestId is supplied) */
  refundTransactionId?: string;
  /** Payment request ID (optional, if refundTransactionId is supplied) */
  paymentRequestId?: string;
}

/**
 * Request parameters for the `/refund` endpoint.
 * Default reason is "ISV退款".
 */
export interface RefundRequest {
  /** Platform payment request ID to refund */
  paymentRequestId: string;
  /** Refund amount, e.g. "50.00" */
  refundAmount: string;
  /** Refund reason, defaults to "ISV退款" */
  reason?: string;
}
