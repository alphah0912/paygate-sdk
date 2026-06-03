# PayGate SDK

支付网关多语言客户端 SDK。封装 HMAC-SHA256 签名、±5 分钟时间戳防重放、限流重试、Webhook 接收及签名校验。

## 目录

```
paygate-sdk/
  java/v1/        → Java SDK   `io.github.alphah0912:paygate-sdk:1.0.3` (Maven Central)
  typescript/v1/  → TypeScript SDK   `npm install ../paygate-sdk/typescript/v1`
  python/v1/      → Python SDK       `pip install git+...@python/v1.0.0`
  php/v1/         → PHP SDK          `composer require paygate/sdk`
  go/v1/          → Go SDK           `go get github.com/alphah0912/paygate-sdk/go/v1@v1.0.0`
  spec/           → 跨语言统一错误码定义
```

各语言接入方式见对应 README：

| 语言 | 文档 | 示例 |
|------|------|------|
| Java | [java/README.md](java/README.md) | [java/v1/examples/](java/v1/examples/) |
| TypeScript | [typescript/README.md](typescript/README.md) | [typescript/v1/examples/](typescript/v1/examples/) |
| Python | [python/v1/README.md](python/v1/README.md) | python/v1/examples/ |
| PHP | [php/v1/README.md](php/v1/README.md) | php/v1/examples/ |
| Go | [go/v1/README.md](go/v1/README.md) | go/v1/examples/ |

## 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/pay` | 发起支付 |
| POST | `/capture` | 资金捕获 |
| POST | `/cancel` | 取消支付 |
| POST | `/inquiry-payment` | 查询支付 |
| POST | `/inquiry-refund` | 查询退款 |
| POST | `/refund` | 发起退款 |

所有 SDK 同时提供 **Webhook 接收端**，支持 ISV 和 NotificationService 两种通知的 HMAC-SHA256 签名校验。

## 认证

所有请求通过 3 个 Header：`X-Api-Key`、`X-Signature`（HMAC-SHA256）、`X-Timestamp`（±5 分钟防重放）。

## 许可证

MIT
