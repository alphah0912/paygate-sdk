<?php

namespace Paygate\Sdk\webhook;

use Paygate\Sdk\ErrorCode;
use Paygate\Sdk\PaygateException;
use Paygate\Sdk\Signature;

/**
 * Handles incoming webhook notifications from the PayGate platform.
 *
 * Both ISV and NotificationService webhooks share the same secret.
 *
 * @author alphah
 * @since 1.0.0
 */
class WebhookHandler
{
    private ?string $secret;

    public function __construct(?string $webhookSecret)
    {
        $this->secret = $webhookSecret;
    }

    public function handle(array $headers, string $body, string $notifyUrl): array
    {
        if (isset($headers['X-Isv-Signature'])) {
            return $this->handleISV($headers, $body, $notifyUrl);
        }
        if (isset($headers['X-Webhook-Signature'])) {
            return $this->handleNotification($headers, $body, $notifyUrl);
        }
        throw new PaygateException(ErrorCode::INVALID_SIGNATURE, 'No webhook signature header found');
    }

    private function handleISV(array $headers, string $body, string $url): array
    {
        $sig = $headers['X-Isv-Signature'][0] ?? null;
        $ts = $headers['X-Isv-Timestamp'][0] ?? null;
        if (!$sig || !$ts) {
            throw new PaygateException(ErrorCode::INVALID_SIGNATURE, 'Missing ISV webhook signature headers');
        }
        if (!$this->secret) {
            throw new PaygateException(ErrorCode::INVALID_SIGNATURE, 'Webhook secret not configured');
        }
        if (!Signature::verify($this->secret, $sig, 'POST', $url, $ts, $body)) {
            throw new PaygateException(ErrorCode::INVALID_SIGNATURE, 'ISV webhook signature mismatch');
        }
        $data = json_decode($body, true);
        if (!$data) {
            throw new PaygateException(ErrorCode::UNKNOWN_ERROR, 'Failed to parse ISV webhook payload');
        }
        return $data;
    }

    private function handleNotification(array $headers, string $body, string $url): array
    {
        $sig = $headers['X-Webhook-Signature'][0] ?? null;
        $ts = $headers['X-Webhook-Timestamp'][0] ?? null;

        if ($sig && $ts) {
            if (!$this->secret) {
                throw new PaygateException(ErrorCode::INVALID_SIGNATURE,
                    'Webhook signature received but no secret configured');
            }
            if (!Signature::verify($this->secret, $sig, 'POST', $url, $ts, $body)) {
                throw new PaygateException(ErrorCode::INVALID_SIGNATURE, 'Webhook signature mismatch');
            }
        }

        $data = json_decode($body, true);
        if (!$data || !isset($data['type'])) {
            throw new PaygateException(ErrorCode::INVALID_PARAMETER, 'Webhook payload missing type field');
        }

        $knownTypes = ['sign.success', 'payment.completed', 'refund.completed'];
        if (!in_array($data['type'], $knownTypes)) {
            throw new PaygateException(ErrorCode::INVALID_PARAMETER, 'Unknown webhook event type: ' . $data['type']);
        }

        return $data;
    }
}
