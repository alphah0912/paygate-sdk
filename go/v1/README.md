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
package main

import (
    "io"
    "net/http"
    "github.com/alphah0912/paygate-sdk/go/v1/webhook"
)

var h = webhook.NewHandler("your_webhook_secret")

func webhookHandler(w http.ResponseWriter, r *http.Request) {
    body, _ := io.ReadAll(r.Body)
    event, err := h.Handle(r.Header, string(body), "https://"+r.Host+r.URL.String())
    if err != nil {
        http.Error(w, err.Error(), 401)
        return
    }
    _ = event
    w.WriteHeader(200)
}

func main() {
    http.HandleFunc("/webhook", webhookHandler)
    http.ListenAndServe(":8080", nil)
}
```

## 本地开发

```bash
go test ./...
```
