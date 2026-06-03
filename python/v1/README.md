# PayGate SDK — Python

Python 3.8+. 单依赖 `httpx`.

## 安装

```bash
pip install git+https://github.com/alphah0912/paygate-sdk.git@python/v1.0.0#subdirectory=python/v1
```

## 快速开始

```python
from paygate_sdk import PaygateClient, Environment

client = PaygateClient(
    api_key="mk_test_your_api_key",
    api_secret="your_api_secret",
    environment=Environment.SANDBOX,
)

resp = client.pay(PayRequest(
    amount="100.00",
    payment_method_code="ALIPAY_CN",
    terminal_type="WEB",
    settlement_currency="USD",
))

print(resp.redirect_url)
```

## API

| 方法 | 参数 | 返回值 |
|------|------|--------|
| `client.pay(req)` | `PayRequest` | `PayResponse` |
| `client.capture(req)` | `CaptureRequest` | `CaptureResponse` |
| `client.cancel(req)` | `CancelRequest` | `CancelResponse` |
| `client.inquiry_payment(req)` | `InquiryPaymentRequest` | `InquiryPaymentResponse` |
| `client.inquiry_refund(req)` | `InquiryRefundRequest` | `InquiryRefundResponse` |
| `client.refund(req)` | `RefundRequest` | `RefundResponse` |

## Webhook

```python
from flask import Flask, request
from paygate_sdk import WebhookHandler

app = Flask(__name__)
handler = WebhookHandler("your_webhook_secret")

@app.route('/webhook', methods=['POST'])
def webhook():
    event = handler.handle(dict(request.headers), request.get_data(as_text=True), request.url)
    return '', 200
```

## 错误处理

```python
from paygate_sdk import PaygateException

try:
    client.pay(req)
except PaygateException as e:
    print(e.error_code)  # "40001"
    print(e.error_code_enum)  # ErrorCode.INVALID_API_KEY
```

## 本地开发

```bash
pip install -e ".[dev]"
pytest tests/
```
