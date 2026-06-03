import { describe, it, expect } from 'vitest';
import { WebhookHandler } from '../src/webhook/webhook-handler';
import { sign } from '../src/signature';

const ISV_SECRET = 'isv_webhook_secret';
const MERCHANT_SECRET = 'merchant_webhook_secret';
const NOTIFY_URL = 'https://merchant.example.com/webhook';

describe('WebhookHandler', () => {
  const handler = new WebhookHandler(ISV_SECRET, MERCHANT_SECRET);

  it('should reject request without webhook headers', () => {
    expect(() => handler.handle({}, '{}', NOTIFY_URL)).toThrow('No webhook signature header');
  });

  it('should verify ISV webhook with valid signature', () => {
    const body = JSON.stringify({
      paymentRequestId: 'REQ123',
      status: 'SUCCESS',
      amount: '100.00',
      currency: 'USD',
      tradeTime: '2024-01-01T00:00:00Z',
      message: '支付成功',
    });
    const timestamp = '1700000000000';
    const sig = sign(ISV_SECRET, 'POST', NOTIFY_URL, timestamp, body);

    const event = handler.handle(
      { 'X-Isv-Signature': sig, 'X-Isv-Timestamp': timestamp },
      body,
      NOTIFY_URL,
    );

    expect(event).toMatchObject({ paymentRequestId: 'REQ123', status: 'SUCCESS' });
  });

  it('should reject ISV webhook with invalid signature', () => {
    expect(() =>
      handler.handle(
        { 'X-Isv-Signature': 'sha256=invalid', 'X-Isv-Timestamp': '1700000000000' },
        '{"paymentRequestId":"REQ123"}',
        NOTIFY_URL,
      ),
    ).toThrow('signature mismatch');
  });

  it('should handle notification sign.success event', () => {
    const body = JSON.stringify({
      type: 'sign.success',
      referenceMerchantId: 'M001',
      message: '签约成功',
      timestamp: '1700000000000',
    });
    const timestamp = '1700000000000';
    const sig = sign(MERCHANT_SECRET, 'POST', NOTIFY_URL, timestamp, body);

    const event = handler.handle(
      { 'X-Webhook-Signature': sig, 'X-Webhook-Timestamp': timestamp },
      body,
      NOTIFY_URL,
    );
    expect(event).toMatchObject({ type: 'sign.success', referenceMerchantId: 'M001' });
  });

  it('should reject notification with invalid signature', () => {
    expect(() =>
      handler.handle(
        { 'X-Webhook-Signature': 'sha256=invalid', 'X-Webhook-Timestamp': '1700000000000' },
        '{"type":"sign.success"}',
        NOTIFY_URL,
      ),
    ).toThrow('signature mismatch');
  });
});
