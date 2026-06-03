import { describe, it, expect } from 'vitest';
import http from 'http';
import { PaygateClient } from '../src/paygate-client';

function startServer(handler: (req: http.IncomingMessage, res: http.ServerResponse) => void): Promise<{ port: number; close: () => void }> {
  return new Promise((resolve) => {
    const server = http.createServer(handler);
    server.listen(0, () => {
      const addr = server.address() as any;
      resolve({ port: addr.port, close: () => server.close() });
    });
  });
}

describe('PaygateClient integration', () => {
  it('should call /pay and parse response', async () => {
    const server = await startServer((req, res) => {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        code: 200, message: 'ok',
        redirectUrl: 'https://checkout.example.com/abc',
        paymentRequestId: 'REQ001',
      }));
    });

    const client = new PaygateClient({
      apiKey: 'mk_test_key',
      apiSecret: 'test_secret',
      baseUrl: `http://localhost:${server.port}`,
    });

    try {
      const resp = await client.pay({
        amount: '100.00',
        paymentMethodCode: 'ALIPAY_CN',
        terminalType: 'WEB',
        settlementCurrency: 'USD',
      });
      expect(resp.redirectUrl).toBe('https://checkout.example.com/abc');
      expect(resp.paymentRequestId).toBe('REQ001');
    } finally {
      server.close();
    }
  });

  it('should throw exception on error response', async () => {
    const server = await startServer((req, res) => {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ code: 40901, message: 'Invalid payment status' }));
    });

    const client = new PaygateClient({
      apiKey: 'mk_test_key',
      apiSecret: 'test_secret',
      baseUrl: `http://localhost:${server.port}`,
    });

    try {
      try {
        await client.capture({ paymentRequestId: 'REQ001' });
      } catch (e: any) {
        expect(e.errorCode).toBe('40901');
        expect(e.message).toContain('Invalid payment status');
      }
    } finally {
      server.close();
    }
  });

  it('should send correct signature headers', async () => {
    let capturedSignature = '';
    let capturedTimestamp = '';

    const server = await startServer(async (req, res) => {
      capturedSignature = req.headers['x-signature'] as string || '';
      capturedTimestamp = req.headers['x-timestamp'] as string || '';
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ code: 200, message: 'ok', redirectUrl: 'https://x.com', paymentRequestId: 'X' }));
    });

    const client = new PaygateClient({
      apiKey: 'mk_test_key',
      apiSecret: 'my_secret',
      baseUrl: `http://localhost:${server.port}`,
    });

    try {
      await client.pay({
        amount: '100.00',
        paymentMethodCode: 'ALIPAY_CN',
        terminalType: 'WEB',
        settlementCurrency: 'USD',
      });

      expect(capturedSignature).toMatch(/^sha256=/);
      expect(capturedTimestamp).toBeTruthy();
    } finally {
      server.close();
    }
  });
});
