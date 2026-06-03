# PayGate SDK — Java

PayGate Java client SDK. JDK 8+, 零 HTTP 依赖，一个 Jackson。

## 依赖

### Maven

```xml
<dependency>
    <groupId>io.github.alphah0912</groupId>
    <artifactId>paygate-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.alphah0912:paygate-sdk:1.0.0'
```

## 快速开始

```java
import com.paygate.sdk.PaygateClient;
import com.paygate.sdk.Environment;
import com.paygate.sdk.request.PayRequest;

PaygateClient client = PaygateClient.builder()
    .apiKey("mk_test_your_api_key")
    .apiSecret("your_api_secret")
    .environment(Environment.SANDBOX)
    .build();

PayResponse resp = client.pay(
    PayRequest.builder()
        .amount("100.00")
        .paymentMethodCode("ALIPAY_CN")
        .terminalType("WEB")
        .settlementCurrency("USD")
        .build()
);

System.out.println(resp.getRedirectUrl());
```

## API

| 方法 | 参数 | 返回值 |
|------|------|--------|
| `client.pay(request)` | PayRequest | PayResponse |
| `client.capture(request)` | CaptureRequest | CaptureResponse |
| `client.cancel(request)` | CancelRequest | CancelResponse |
| `client.inquiryPayment(request)` | InquiryPaymentRequest | InquiryPaymentResponse |
| `client.inquiryRefund(request)` | InquiryRefundRequest | InquiryRefundResponse |
| `client.refund(request)` | RefundRequest | RefundResponse |

## Webhook 接收

```java
import com.paygate.sdk.webhook.WebhookHandler;
import com.paygate.sdk.webhook.WebhookEvent;

WebhookHandler handler = new WebhookHandler("isv_secret", "notify_secret");

// 在你的 Controller 里
@PostMapping("/webhook")
public void webhook(@RequestHeader Map<String, String> headers, @RequestBody String body) {
    WebhookEvent event = handler.handle(headers, body, "/webhook");
    if (event instanceof WebhookEvent.PaymentResult) {
        WebhookEvent.PaymentResult pr = (WebhookEvent.PaymentResult) event;
        System.out.println(pr.getStatus());
    }
}
```

## 错误处理

```java
try {
    client.pay(request);
} catch (PaygateException e) {
    System.out.println(e.getErrorCode());    // "40001"
    System.out.println(e.getMessage());      // "Invalid API key"
    System.out.println(e.getErrorCodeEnum()); // ErrorCode.INVALID_API_KEY
}
```

## 本地开发

```bash
./gradlew test      # 运行测试
./gradlew build     # 编译
```
