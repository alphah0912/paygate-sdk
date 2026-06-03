# PayGate SDK

支付网关多语言客户端 SDK，封装 HMAC-SHA256 签名、限流重试、Webhook 接收，商户只需填 POJO 调方法。

| 语言 | 包名 | 发布渠道 | 状态 |
|------|------|----------|------|
| Java | `com.github.alphah0912:paygate-sdk` | [JitPack](https://jitpack.io/#alphah0912/paygate-sdk) | ✅ |
| TypeScript | `@paygate/sdk` | npm (git source) | 🚧 |
| Python | `paygate-sdk` | pip (git source) | 🚧 |
| PHP | `paygate/sdk` | Composer (git source) | 🚧 |
| Go | `paygate-sdk-go` | `go get` | 🚧 |

## Java 快速接入

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.alphah0912:paygate-sdk:java/v1.0.1'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.alphah0912</groupId>
    <artifactId>paygate-sdk</artifactId>
    <version>java/v1.0.1</version>
</dependency>
```

### 使用示例

```java
PaygateClient client = PaygateClient.builder()
    .apiKey("mk_test_xxx")
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
```

更多示例见 [java/examples/](java/examples/)。

## 接口覆盖

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pay` | 发起支付 |
| POST | `/capture` | 资金捕获 |
| POST | `/cancel` | 取消支付 |
| POST | `/inquiry-payment` | 查询支付 |
| POST | `/inquiry-refund` | 查询退款 |
| POST | `/refund` | 发起退款 |

所有 SDK 同时提供 **Webhook 接收端**，支持 ISV 支付结果通知和 NotificationService 事件通知的签名校验与 Payload 解析。

## 认证

所有请求通过 3 个 Header 认证：`X-Api-Key`、`X-Signature`（HMAC-SHA256）、`X-Timestamp`（防重放）。

## 许可证

MIT
