/**
 * Base response fields present in every API response.
 * A `code` of "0" indicates success; any other value is an error.
 */
export interface ApiResponse {
  /** Response code, "0" means success */
  code: string;
  /** Human-readable response message */
  message: string;
}

/**
 * Response for the `/pay` endpoint.
 */
export interface PayResponse extends ApiResponse {
  /** URL to redirect the buyer for payment completion */
  redirectUrl: string;
  /** Platform-assigned unique payment request identifier */
  paymentRequestId: string;
}

/**
 * Response for the `/capture` endpoint.
 */
export interface CaptureResponse extends ApiResponse {
  /** Platform-assigned capture transaction identifier */
  captureId: string;
  /** Capture status, e.g. "SUCCESS" */
  status: string;
}

/**
 * Response for the `/cancel` endpoint.
 */
export interface CancelResponse extends ApiResponse {
  /** Cancellation status */
  status: string;
}

/**
 * Response for the `/inquiry-payment` endpoint.
 */
export interface InquiryPaymentResponse extends ApiResponse {
  /** Trade-level status */
  tradeStatus: string;
  /** Payment identifier */
  paymentId: string;
  /** Payment status, e.g. "SUCCESS" */
  paymentStatus: string;
}

/**
 * Response for the `/inquiry-refund` endpoint.
 */
export interface InquiryRefundResponse extends ApiResponse {
  /** Trade-level status */
  tradeStatus: string;
  /** Refund transaction identifier */
  refundId: string;
  /** Refund status, e.g. "SUCCESS" */
  refundStatus: string;
}

/**
 * Response for the `/refund` endpoint.
 */
export interface RefundResponse extends ApiResponse {
  /** Platform-assigned refund transaction identifier */
  refundTransactionId: string;
  /** Refund status, e.g. "SUCCESS" */
  refundStatus: string;
}
