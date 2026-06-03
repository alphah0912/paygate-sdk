# PayGate SDK — TypeScript

PayGate TypeScript client SDK. Node.js 18+, 零外部依赖。

## 安装

```bash
npm install github:alphah0912/paygate-sdk
```

## 快速开始

```ts
import { PaygateClient, Environment } from '@alphah0912/paygate-sdk';

const client = new PaygateClient({
  apiKey: 'mk_test_your_api_key',
  apiSecret: 'your_api_secret',
  environment: Environment.SANDBOX,
});

const resp = await client.pay({
  amount: '100.00',
  paymentMethodCode: 'ALIPAY_CN',
  terminalType: 'WEB',
  settlementCurrency: 'USD',
});

console.log(resp.redirectUrl);
```

## API

| 方法 | 参数 | 返回值 |
|------|------|--------|
| `client.pay(request)` | PayRequest | Promise\<PayResponse\> |
| `client.capture(request)` | CaptureRequest | Promise\<CaptureResponse\> |
| `client.cancel(request)` | CancelRequest | Promise\<CancelResponse\> |
| `client.inquiryPayment(request)` | InquiryPaymentRequest | Promise\<InquiryPaymentResponse\> |
| `client.inquiryRefund(request)` | InquiryRefundRequest | Promise\<InquiryRefundResponse\> |
| `client.refund(request)` | RefundRequest | Promise\<RefundResponse\> |

## Webhook 接收

```ts
import { WebhookHandler } from '@alphah0912/paygate-sdk';
import express from 'express';

const app = express();
const handler = new WebhookHandler('your_webhook_secret');

app.post('/webhook', express.raw({ type: '*/*' }), (req, res) => {
  const body = req.body.toString();
  const url = `${req.protocol}://${req.get('host')}${req.originalUrl}`;
  const headers: Record<string, string> = {};
  Object.entries(req.headers).forEach(([k, v]) => { headers[k] = String(v); });

  const event = handler.handle(headers, body, url);
  // event.type: 'payment.result' | 'sign.success' | 'payment.completed' | 'refund.completed'
  res.sendStatus(200);
});

app.listen(3000);
```

## 错误处理

```ts
try {
  await client.pay(request);
} catch (e) {
  console.log(e.errorCode);  // "40001"
  console.log(e.message);    // "Invalid API key"
}
```

## 本地开发

```bash
pnpm test     # 运行测试
pnpm build    # 编译
```
