import { verify } from '../signature';
import { PaygateException } from '../paygate-exception';
import { ErrorCode } from '../error-code';
import { WebhookEvent } from '../types/webhook';

/** Header name for ISV webhook HMAC signature */
const ISV_SIGNATURE_HEADER = 'x-isv-signature';
/** Header name for ISV webhook timestamp */
const ISV_TIMESTAMP_HEADER = 'x-isv-timestamp';
/** Header name for NotificationService webhook HMAC signature */
const WEBHOOK_SIGNATURE_HEADER = 'x-webhook-signature';
/** Header name for NotificationService webhook timestamp */
const WEBHOOK_TIMESTAMP_HEADER = 'x-webhook-timestamp';

/**
 * Handles incoming webhook notifications from the PayGate platform.
 *
 * Two webhook types, both using HMAC-SHA256:
 * - ISV Webhook — headers `X-Isv-Signature` + `X-Isv-Timestamp`.
 * - NotificationService Webhook — headers `X-Webhook-Signature` + `X-Webhook-Timestamp`.
 *
 * Sign string: `POST\n{notifyUrl}\n{timestamp}\n{body}`
 *
 * @author alphah
 * @since 1.0.0
 */
export class WebhookHandler {
  /** Platform ISV webhook secret for HMAC-SHA256 verification */
  private readonly isvWebhookSecret: string;

  /** Merchant's own webhook secret for NotificationService HMAC verification */
  private readonly merchantWebhookSecret: string | null;

  /**
   * @param isvWebhookSecret      platform ISV webhook secret
   * @param merchantWebhookSecret merchant's own webhook secret for NotificationService
   */
  constructor(isvWebhookSecret: string, merchantWebhookSecret: string | null) {
    this.isvWebhookSecret = isvWebhookSecret;
    this.merchantWebhookSecret = merchantWebhookSecret;
  }

  /**
   * Processes an incoming webhook request.
   *
   * @param headers   request headers as a Record
   * @param body      raw JSON request body
   * @param notifyUrl the full URL the webhook was sent to (e.g. https://merchant.com/webhook)
   * @returns the parsed webhook event
   * @throws PaygateException if signature verification fails or payload is invalid
   */
  handle(headers: Record<string, string | string[] | undefined>, body: string, notifyUrl: string): WebhookEvent {
    const lowerHeaders = normalizeHeaders(headers);

    if (lowerHeaders[ISV_SIGNATURE_HEADER]) {
      return this.handleIsvWebhook(lowerHeaders, body, notifyUrl);
    }
    if (lowerHeaders[WEBHOOK_SIGNATURE_HEADER]) {
      return this.handleNotificationWebhook(lowerHeaders, body, notifyUrl);
    }
    throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'No webhook signature header found');
  }

  private handleIsvWebhook(headers: Record<string, string>, body: string, notifyUrl: string): WebhookEvent {
    const sigHeader = headers[ISV_SIGNATURE_HEADER];
    const tsHeader = headers[ISV_TIMESTAMP_HEADER];

    if (!sigHeader || !tsHeader) {
      throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'Missing ISV webhook signature headers');
    }

    // Platform signs with "POST\n{notifyUrl}\n{timestamp}\n{body}" (full URL, not just path)
    const valid = verify(this.isvWebhookSecret, sigHeader, 'POST', notifyUrl, tsHeader, body);
    if (!valid) {
      throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'ISV webhook signature mismatch');
    }

    try {
      return JSON.parse(body) as WebhookEvent;
    } catch (e) {
      throw new PaygateException(ErrorCode.UNKNOWN_ERROR, 'Failed to parse ISV webhook payload');
    }
  }

  private handleNotificationWebhook(headers: Record<string, string>, body: string, notifyUrl: string): WebhookEvent {
    const sigHeader = headers[WEBHOOK_SIGNATURE_HEADER];
    const tsHeader = headers[WEBHOOK_TIMESTAMP_HEADER];

    // Verify HMAC-SHA256 signature (same algorithm as ISV, different secret)
    // Platform sends signature only when a secret is configured.
    // If sig present, secret must be configured and must pass verification.
    if (sigHeader && tsHeader) {
      if (!this.merchantWebhookSecret) {
        throw new PaygateException(ErrorCode.INVALID_SIGNATURE,
          'Webhook signature received but no merchant secret configured');
      }
      const valid = verify(this.merchantWebhookSecret, sigHeader, 'POST', notifyUrl, tsHeader, body);
      if (!valid) {
        throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'Webhook signature mismatch');
      }
    }

    let parsed: Record<string, unknown>;
    try {
      parsed = JSON.parse(body);
    } catch (e) {
      throw new PaygateException(ErrorCode.UNKNOWN_ERROR, 'Failed to parse webhook payload');
    }

    const type = parsed.type as string | undefined;
    if (!type) {
      throw new PaygateException(ErrorCode.INVALID_PARAMETER, 'Webhook payload missing type field');
    }

    const knownTypes = ['sign.success', 'payment.completed', 'refund.completed'];
    if (!knownTypes.includes(type)) {
      throw new PaygateException(ErrorCode.INVALID_PARAMETER, `Unknown webhook event type: ${type}`);
    }

    return parsed as unknown as WebhookEvent;
  }
}

/**
 * Normalizes headers to lowercase keys with single string values.
 */
function normalizeHeaders(headers: Record<string, string | string[] | undefined>): Record<string, string> {
  const result: Record<string, string> = {};
  for (const [key, value] of Object.entries(headers)) {
    const normalizedKey = key.toLowerCase();
    const normalizedValue = Array.isArray(value) ? value[0] : value;
    if (normalizedValue !== undefined) {
      result[normalizedKey] = normalizedValue;
    }
  }
  return result;
}
