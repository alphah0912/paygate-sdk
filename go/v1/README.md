# PayGate SDK — Go

Go 1.20+. 零外部依赖，标准库 `net/http`。

## 安装

```bash
go get github.com/alphah0912/paygate-sdk/go@v1.0.0
```

## 快速开始

```go
import "github.com/alphah0912/paygate-sdk/go/client"

c := client.New("mk_test_key", "test_secret", client.SANDBOX)

resp, err := c.Pay(client.PayRequest{
    Amount:             "100.00",
    PaymentMethodCode:  "ALIPAY_CN",
    TerminalType:       "WEB",
    SettlementCurrency: "USD",
})
```

## API

| 方法 | 参数 | 返回值 |
|------|------|--------|
| `c.Pay(req)` | `PayRequest` | `*PayResponse, error` |
| `c.Capture(req)` | `CaptureRequest` | `*CaptureResponse, error` |
| `c.Cancel(req)` | `CancelRequest` | `*CancelResponse, error` |
| `c.InquiryPayment(req)` | `InquiryPaymentRequest` | `*InquiryPaymentResponse, error` |
| `c.InquiryRefund(req)` | `InquiryRefundRequest` | `*InquiryRefundResponse, error` |
| `c.Refund(req)` | `RefundRequest` | `*RefundResponse, error` |

## Webhook

```go
import "github.com/alphah0912/paygate-sdk/go/webhook"

h := webhook.NewHandler("isv_secret", "merchant_secret")
event, err := h.Handle(headers, body, "https://merchant.com/webhook")
```

## 本地开发

```bash
go test ./...
```
