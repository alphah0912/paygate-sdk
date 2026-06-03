import { randomUUID } from 'crypto';
import { Environment, getBasePath } from './environment';
import { sign } from './signature';
import { PaygateException } from './paygate-exception';
import { ErrorCode } from './error-code';
import {
  PayRequest,
  CaptureRequest,
  CancelRequest,
  InquiryPaymentRequest,
  InquiryRefundRequest,
  RefundRequest,
} from './types/request';
import {
  PayResponse,
  CaptureResponse,
  CancelResponse,
  InquiryPaymentResponse,
  InquiryRefundResponse,
  RefundResponse,
} from './types/response';

/** Maximum number of retries when rate-limited (HTTP 429) */
const MAX_RETRIES = 3;

/** Read timeout in milliseconds */
const READ_TIMEOUT_MS = 30_000;

export interface PaygateClientConfig {
  /** Merchant API key, prefixed with mk_live_ or mk_test_ */
  apiKey: string;
  /** Merchant API secret for HMAC-SHA256 signing */
  apiSecret: string;
  /** Target environment, defaults to SANDBOX */
  environment?: Environment;
  /** @internal Override base URL for testing */
  baseUrl?: string;
}

/**
 * Main entry point for the PayGate TypeScript SDK.
 *
 * @example
 * ```
 * const client = new PaygateClient({
 *   apiKey: 'mk_test_xxx',
 *   apiSecret: 'your_api_secret',
 *   environment: Environment.SANDBOX,
 * });
 *
 * const resp = await client.pay({
 *   amount: '100.00',
 *   paymentMethodCode: 'ALIPAY_CN',
 *   terminalType: 'WEB',
 *   settlementCurrency: 'USD',
 * });
 * ```
 *
 * @author alphah
 * @since 1.0.0
 */
export class PaygateClient {
  /** Merchant API key, prefixed with mk_live_ or mk_test_ */
  private readonly apiKey: string;

  /** Merchant API secret for HMAC-SHA256 signing */
  private readonly apiSecret: string;

  /** Resolved API base URL from the configured Environment */
  private readonly baseUrl: string;

  /** API path prefix used for HMAC signing (e.g. /api/gateway/v1) */
  private readonly basePath: string;

  constructor(config: PaygateClientConfig) {
    if (!config.apiKey) throw new Error('apiKey is required');
    if (!config.apiSecret) throw new Error('apiSecret is required');

    this.apiKey = config.apiKey;
    this.apiSecret = config.apiSecret;
    this.baseUrl = config.baseUrl ?? (config.environment ?? Environment.SANDBOX);
    this.basePath = config.baseUrl ? '' : getBasePath(this.baseUrl);
  }

  /**
   * Initiates a payment.
   * @returns response with redirectUrl and paymentRequestId
   */
  pay(request: PayRequest): Promise<PayResponse> {
    return this.execute<PayResponse>('/pay', request);
  }

  /**
   * Captures a previously authorized payment.
   * @returns response with captureId and status
   */
  capture(request: CaptureRequest): Promise<CaptureResponse> {
    return this.execute<CaptureResponse>('/capture', request);
  }

  /**
   * Cancels a payment.
   * @returns response with status
   */
  cancel(request: CancelRequest): Promise<CancelResponse> {
    if (!request.reason) request.reason = 'MERCHANT_MANUAL';
    return this.execute<CancelResponse>('/cancel', request);
  }

  /**
   * Queries the status of a payment.
   * @returns response with tradeStatus, paymentId, paymentStatus
   */
  inquiryPayment(request: InquiryPaymentRequest): Promise<InquiryPaymentResponse> {
    return this.execute<InquiryPaymentResponse>('/inquiry-payment', request);
  }

  /**
   * Queries the status of a refund.
   * @returns response with tradeStatus, refundId, refundStatus
   */
  inquiryRefund(request: InquiryRefundRequest): Promise<InquiryRefundResponse> {
    return this.execute<InquiryRefundResponse>('/inquiry-refund', request);
  }

  /**
   * Initiates a refund.
   * @returns response with refundTransactionId and refundStatus
   */
  refund(request: RefundRequest): Promise<RefundResponse> {
    if (!request.reason) request.reason = 'ISV退款';
    return this.execute<RefundResponse>('/refund', request);
  }

  private async execute<T>(path: string, request: unknown, attempt: number = 0): Promise<T> {
    const body = JSON.stringify(request);
    const timestamp = String(Date.now());
    const signingPath = this.basePath + path;  // e.g. /api/gateway/v1/pay
    const signature = sign(this.apiSecret, 'POST', signingPath, timestamp, body);

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), READ_TIMEOUT_MS);

    let response: Response;
    try {
      response = await fetch(`${this.baseUrl}${path}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Api-Key': this.apiKey,
          'X-Signature': signature,
          'X-Timestamp': timestamp,
          'X-Request-Id': randomUUID().substring(0, 8),
        },
        body,
        signal: controller.signal,
      });
    } catch (e) {
      throw new PaygateException(ErrorCode.NETWORK_ERROR, `Network request failed: ${(e as Error).message}`);
    } finally {
      clearTimeout(timeout);
    }

    const retryable = response.status === 429 || response.status === 502 || response.status === 503 || response.status === 504;
    if (retryable && attempt < MAX_RETRIES) {
      await new Promise(resolve => setTimeout(resolve, Math.pow(2, attempt + 1) * 1000));
      return this.execute<T>(path, request, attempt + 1);
    }

    if (response.status >= 500) {
      const text = await response.text();
      throw new PaygateException(ErrorCode.SERVER_ERROR, `Server returned ${response.status}: ${text}`, response.status);
    }

    let data: T;
    try {
      data = (await response.json()) as T;
    } catch (e) {
      const text = await response.text();
      throw new PaygateException(ErrorCode.UNKNOWN_ERROR, `Failed to parse response: ${text}`);
    }

    const apiResp = data as unknown as { code?: number; message?: string };
    if (apiResp.code !== undefined && apiResp.code !== 200) {
      throw new PaygateException(String(apiResp.code), apiResp.message ?? 'Unknown error');
    }

    return data;
  }
}
