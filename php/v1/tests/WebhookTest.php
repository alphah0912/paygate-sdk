<?php

use PHPUnit\Framework\TestCase;
use Paygate\Sdk\Signature;
use Paygate\Sdk\webhook\WebhookHandler;

class WebhookTest extends TestCase
{
    private string $isvSecret = 'isv_webhook_secret';
    private string $merchantSecret = 'merchant_webhook_secret';
    private string $notifyUrl = 'https://merchant.example.com/webhook';

    public function testRejectNoHeaders(): void
    {
        $handler = new WebhookHandler($this->isvSecret);
        $this->expectException(\Paygate\Sdk\PaygateException::class);
        $handler->handle([], '{}', $this->notifyUrl);
    }

    public function testISVValidSignature(): void
    {
        $handler = new WebhookHandler($this->isvSecret);
        $body = json_encode([
            'paymentRequestId' => 'REQ123',
            'status' => 'SUCCESS',
            'amount' => '100.00',
            'currency' => 'USD',
            'tradeTime' => '2024-01-01T00:00:00Z',
            'message' => '支付成功',
        ]);
        $ts = '1700000000000';
        $sig = Signature::sign($this->isvSecret, 'POST', $this->notifyUrl, $ts, $body);

        $event = $handler->handle(
            ['X-Isv-Signature' => [$sig], 'X-Isv-Timestamp' => [$ts]],
            $body,
            $this->notifyUrl
        );
        $this->assertEquals('REQ123', $event['paymentRequestId']);
        $this->assertEquals('SUCCESS', $event['status']);
    }
}
