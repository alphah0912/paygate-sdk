# PayGate SDK — PHP

PHP 8.0+. 单依赖 `guzzlehttp/guzzle`.

## 安装

```bash
composer require paygate/sdk
```

## 快速开始

```php
use Paygate\Sdk\PaygateClient;
use Paygate\Sdk\Environment;

$client = new PaygateClient('mk_test_key', 'test_secret', Environment::SANDBOX);

$resp = $client->pay([
    'amount'             => '100.00',
    'paymentMethodCode'  => 'ALIPAY_CN',
    'terminalType'       => 'WEB',
    'settlementCurrency' => 'USD',
]);

echo $resp['redirectUrl'];
```

## API

| 方法 | 参数 | 返回值 |
|------|------|--------|
| `$client->pay($req)` | `array` | `array` |
| `$client->capture($req)` | `array` | `array` |
| `$client->cancel($req)` | `array` | `array` |
| `$client->inquiryPayment($req)` | `array` | `array` |
| `$client->inquiryRefund($req)` | `array` | `array` |
| `$client->refund($req)` | `array` | `array` |

## Webhook

```php
use Paygate\Sdk\webhook\WebhookHandler;

$handler = new WebhookHandler('isv_secret', 'merchant_secret');
$event = $handler->handle($headers, file_get_contents('php://input'), 'https://merchant.com/webhook');
```

## 错误处理

```php
use Paygate\Sdk\PaygateException;

try {
    $client->pay($req);
} catch (PaygateException $e) {
    echo $e->getErrorCode(); // "40001"
    echo $e->getMessage();   // "Invalid API key"
}
```

## 本地开发

```bash
cd php/v1 && composer install && vendor/bin/phpunit tests/
```
