# PayGate SDK — PHP

PHP 8.0+. 单依赖 `guzzlehttp/guzzle`.

## 安装

```bash
# composer.json
{
    "repositories": [{"type": "vcs", "url": "https://github.com/alphah0912/paygate-sdk"}],
    "require": {"paygate/sdk": "dev-master"}
}
composer install
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

// 在你的 PHP  Controller / 入口文件里
$handler = new WebhookHandler('your_webhook_secret');

$headers = getallheaders();
$body    = file_get_contents('php://input');
$url     = ($_SERVER['HTTPS'] ?? 'off') === 'on' ? 'https://' : 'http://'
         . $_SERVER['HTTP_HOST'] . $_SERVER['REQUEST_URI'];

$event = $handler->handle($headers, $body, $url);
// $event['paymentRequestId'], $event['status'] 等字段
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
