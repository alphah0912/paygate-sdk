<?php

namespace Paygate\Sdk\webhook;

use Paygate\Sdk\ErrorCode;
use Paygate\Sdk\PaygateException;
use Paygate\Sdk\Signature;

/**
 * Handles incoming webhook notifications from the PayGate platform.
 *
 * Two webhook types, both using HMAC-SHA256:
 * - ISV Webhook: headers X-Isv-Signature + X-Isv-Timestamp.
 * - NotificationService: headers X-Webhook-Signature + X-Webhook-Timestamp.
 *
 * @author alphah
 * @since 1.0.0
 */
class WebhookHandler
{
    private string $isvSecret;
    private ?string $merchantSecret;

    public function __construct(string $isvWebhookSecret, ?string $merchantWebhookSecret)
    {
        $this->isvSecret = $isvWebhookSecret;
        $this->merchantSecret = $merchantWebhookSecret;
    }

    /**
     * Process an incoming webhook request.
     *
     * @param array<string, array<string>> $headers Request headers (multi-valued).
     * @param string $body Raw JSON request body.
     * @param string $notifyUrl Full URL the webhook was sent to.
     * @return array Parsed event data.
     * @throws PaygateException
     */
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
        if (!Signature::verify($this->isvSecret, $sig, 'POST', $url, $ts, $body)) {
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

        // Platform sends signature only when a secret is configured.
        // If sig present, secret must be configured and must pass verification.
        if ($sig && $ts) {
            if (!$this->merchantSecret) {
                throw new PaygateException(ErrorCode::INVALID_SIGNATURE,
                    'Webhook signature received but no merchant secret configured');
            }
            if (!Signature::verify($this->merchantSecret, $sig, 'POST', $url, $ts, $body)) {
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
