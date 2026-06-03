# PayGate SDK

支付网关多语言客户端 SDK，封装 HMAC-SHA256 签名、限流重试、Webhook 接收，商户只需填 POJO 调方法。

## 目录结构

```
paygate-sdk/
  java/v1/        # Java SDK v1
  typescript/v1/  # TypeScript SDK v1
  python/v1/      # Python SDK v1（待开发）
  php/v1/         # PHP SDK v1（待开发）
  go/v1/          # Go SDK v1（待开发）
  spec/           # 跨语言统一错误码
```

## 状态

| 语言 | 包名 | 发布渠道 | 状态 |
|------|------|----------|------|
| Java | `io.github.alphah0912:paygate-sdk:1.0.0` | Maven Central | ✅ |
| TypeScript | `@alphah0912/paygate-sdk` | npm | ✅ |
| Python | `paygate-sdk` | pip | 🚧 |
| PHP | `paygate/sdk` | Composer | 🚧 |
| Go | `paygate-sdk-go` | `go get` | 🚧 |

## Java 快速接入

```xml
<dependency>
    <groupId>io.github.alphah0912</groupId>
    <artifactId>paygate-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

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

更多见 [java/v1/examples/](java/v1/examples/) 和 [java/README.md](java/README.md)。

## TypeScript 快速接入

```bash
npm install github:alphah0912/paygate-sdk#typescript/v1
```

```ts
const client = new PaygateClient({
  apiKey: 'mk_test_xxx',
  apiSecret: 'your_api_secret',
  environment: Environment.SANDBOX,
});

const resp = await client.pay({
  amount: '100.00',
  paymentMethodCode: 'ALIPAY_CN',
  terminalType: 'WEB',
  settlementCurrency: 'USD',
});
```

更多见 [typescript/v1/examples/](typescript/v1/examples/) 和 [typescript/README.md](typescript/README.md)。

## 接口覆盖

| 方法   | 路径                | 说明     |
|--------|---------------------|----------|
| POST   | `/pay`              | 发起支付 |
| POST   | `/capture`          | 资金捕获 |
| POST   | `/cancel`           | 取消支付 |
| POST   | `/inquiry-payment`  | 查询支付 |
| POST   | `/inquiry-refund`   | 查询退款 |
| POST   | `/refund`           | 发起退款 |

所有 SDK 同时提供 Webhook 接收端，支持 ISV 和 NotificationService 两种通知的 HMAC-SHA256 签名校验。

## 认证

所有请求通过 3 个 Header 认证：`X-Api-Key`、`X-Signature`（HMAC-SHA256）、`X-Timestamp`（±5 分钟防重放）。

## 许可证

MIT
