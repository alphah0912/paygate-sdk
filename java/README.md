# PayGate SDK — Java

PayGate Java client SDK. JDK 8+, 零 HTTP 依赖，一个 Jackson。

## 安装

### Maven

```xml
<dependency>
    <groupId>io.github.alphah0912</groupId>
    <artifactId>paygate-sdk</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.alphah0912:paygate-sdk:1.0.3'
```

## 快速开始

```java
PaygateClient client = PaygateClient.builder()
    .apiKey("mk_test_your_api_key")
    .apiSecret("your_api_secret")
    .environment(Environment.SANDBOX)
    .build();

PayResponse resp = client.pay(PayRequest.builder()
    .amount("100.00")
    .paymentMethodCode("ALIPAY_CN")
    .terminalType("WEB")
    .settlementCurrency("USD")
    .build());

System.out.println(resp.getRedirectUrl());
```

## API

| 方法 | 参数 | 返回值 |
|------|------|--------|
| `client.pay(req)` | PayRequest | PayResponse |
| `client.capture(req)` | CaptureRequest | CaptureResponse |
| `client.cancel(req)` | CancelRequest | CancelResponse |
| `client.inquiryPayment(req)` | InquiryPaymentRequest | InquiryPaymentResponse |
| `client.inquiryRefund(req)` | InquiryRefundRequest | InquiryRefundResponse |
| `client.refund(req)` | RefundRequest | RefundResponse |

## Webhook 接收

```java
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class WebhookController {

    private final WebhookHandler handler = new WebhookHandler("your_webhook_secret");

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestHeader Map<String, String> httpHeaders,
            @RequestBody String rawBody,
            HttpServletRequest request) {

        Map<String, List<String>> headers = new HashMap<>();
        httpHeaders.forEach((k, v) -> headers.put(k, List.of(v)));

        String url = request.getRequestURL().toString();

        WebhookEvent event = handler.handle(headers, rawBody, url);

        if (event instanceof WebhookEvent.PaymentResult pr) {
            // paymentRequestId, status, amount 等字段
            System.out.println(pr.getStatus());
        }
        return ResponseEntity.ok().build();
    }
}
```

## 错误处理

```java
try {
    client.pay(request);
} catch (PaygateException e) {
    e.getErrorCode();   // "40001"
    e.getMessage();     // "Invalid API key"
}
```

## 本地开发

```bash
cd java/v1 && ./gradlew test
```
