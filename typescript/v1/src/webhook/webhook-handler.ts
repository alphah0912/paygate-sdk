import { verify } from '../signature';
import { PaygateException } from '../paygate-exception';
import { ErrorCode } from '../error-code';
import { WebhookEvent } from '../types/webhook';

const ISV_SIGNATURE_HEADER = 'x-isv-signature';
const ISV_TIMESTAMP_HEADER = 'x-isv-timestamp';
const WEBHOOK_SIGNATURE_HEADER = 'x-webhook-signature';
const WEBHOOK_TIMESTAMP_HEADER = 'x-webhook-timestamp';

/**
 * Handles incoming webhook notifications from the PayGate platform.
 *
 * Both ISV and NotificationService webhooks share the same secret
 * (aligned with platform: webhook.getSecret()).
 *
 * @author alphah
 * @since 1.0.0
 */
export class WebhookHandler {
  private readonly secret: string | null;

  constructor(secret: string | null) {
    this.secret = secret;
  }

  handle(headers: Record<string, string | string[] | undefined>, body: string, notifyUrl: string): WebhookEvent {
    const lower = normalizeHeaders(headers);

    if (lower[ISV_SIGNATURE_HEADER]) {
      return this.handleISV(lower, body, notifyUrl);
    }
    if (lower[WEBHOOK_SIGNATURE_HEADER]) {
      return this.handleNotification(lower, body, notifyUrl);
    }
    throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'No webhook signature header found');
  }

  private handleISV(headers: Record<string, string>, body: string, notifyUrl: string): WebhookEvent {
    const sig = headers[ISV_SIGNATURE_HEADER];
    const ts = headers[ISV_TIMESTAMP_HEADER];
    if (!sig || !ts) {
      throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'Missing ISV webhook signature headers');
    }
    if (!this.secret) {
      throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'Webhook secret not configured');
    }
    if (!verify(this.secret, sig, 'POST', notifyUrl, ts, body)) {
      throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'ISV webhook signature mismatch');
    }
    try {
      return JSON.parse(body) as WebhookEvent;
    } catch (e) {
      throw new PaygateException(ErrorCode.UNKNOWN_ERROR, 'Failed to parse ISV webhook payload');
    }
  }

  private handleNotification(headers: Record<string, string>, body: string, notifyUrl: string): WebhookEvent {
    const sig = headers[WEBHOOK_SIGNATURE_HEADER];
    const ts = headers[WEBHOOK_TIMESTAMP_HEADER];
    if (sig && ts) {
      if (!this.secret) {
        throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'Webhook signature received but no secret configured');
      }
      if (!verify(this.secret, sig, 'POST', notifyUrl, ts, body)) {
        throw new PaygateException(ErrorCode.INVALID_SIGNATURE, 'Webhook signature mismatch');
      }
    }
    const parsed = JSON.parse(body);
    if (!parsed.type) {
      throw new PaygateException(ErrorCode.INVALID_PARAMETER, 'Webhook payload missing type field');
    }
    if (!['sign.success', 'payment.completed', 'refund.completed'].includes(parsed.type)) {
      throw new PaygateException(ErrorCode.INVALID_PARAMETER, `Unknown webhook event type: ${parsed.type}`);
    }
    return parsed as WebhookEvent;
  }
}

function normalizeHeaders(headers: Record<string, string | string[] | undefined>): Record<string, string> {
  const result: Record<string, string> = {};
  for (const [key, value] of Object.entries(headers)) {
    const v = Array.isArray(value) ? value[0] : value;
    if (v !== undefined) result[key.toLowerCase()] = v;
  }
  return result;
}
