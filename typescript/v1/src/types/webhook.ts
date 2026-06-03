/**
 * ISV webhook: payment result notification pushed by the platform.
 * Signed with HMAC-SHA256, verified by WebhookHandler.
 */
export interface PaymentResultWebhookEvent {
  /** Event type identifier */
  type: 'payment.result';
  /** Platform payment request ID */
  paymentRequestId: string;
  /** Payment status, e.g. "SUCCESS", "FAIL" */
  status: string;
  /** Payment amount */
  amount: string;
  /** Transaction currency */
  currency: string;
  /** Trade timestamp */
  tradeTime: string;
  /** Status description (Chinese) */
  message: string;
  /** Extended payment result information, may be null */
  paymentResultInfo?: PaymentResultInfo;
}

/** Nested payment result details. */
export interface PaymentResultInfo {
  /** 3DS authentication result */
  threeDSResult?: string;
}

/**
 * NotificationService event: merchant sign-up completed.
 */
export interface SignSuccessWebhookEvent {
  type: 'sign.success';
  /** Merchant reference identifier */
  referenceMerchantId: string;
  /** Status description */
  message: string;
  /** Event timestamp */
  timestamp: string;
}

/**
 * NotificationService event: payment completed.
 */
export interface PaymentCompletedWebhookEvent {
  type: 'payment.completed';
  /** Platform trade number */
  platformTradeNo: string;
  /** Payment status */
  status: string;
  /** Event timestamp */
  timestamp: string;
}

/**
 * NotificationService event: refund completed.
 */
export interface RefundCompletedWebhookEvent {
  type: 'refund.completed';
  /** Refund trade number */
  refundTradeNo: string;
  /** Refund status */
  status: string;
  /** Event timestamp */
  timestamp: string;
}

/** Union type of all supported webhook events. */
export type WebhookEvent =
  | PaymentResultWebhookEvent
  | SignSuccessWebhookEvent
  | PaymentCompletedWebhookEvent
  | RefundCompletedWebhookEvent;
