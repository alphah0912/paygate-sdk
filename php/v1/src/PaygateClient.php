<?php

namespace Paygate\Sdk;

use GuzzleHttp\Client as HttpClient;
use GuzzleHttp\Exception\RequestException;

/**
 * Main entry point for the PayGate PHP SDK.
 *
 * @author alphah
 * @since 1.0.0
 */
class PaygateClient
{
    /** @var string */
    private string $apiKey;
    /** @var string */
    private string $apiSecret;
    /** @var string */
    private string $baseUrl;
    /** @var string */
    private string $basePath;
    /** @var HttpClient */
    private HttpClient $http;

    private const MAX_RETRIES = 3;

    /**
     * @param array $env One of Environment::SANDBOX or Environment::LIVE.
     */
    public function __construct(string $apiKey, string $apiSecret, array $env = null)
    {
        if (empty($apiKey)) throw new \InvalidArgumentException('apiKey is required');
        if (empty($apiSecret)) throw new \InvalidArgumentException('apiSecret is required');
        $this->apiKey = $apiKey;
        $this->apiSecret = $apiSecret;
        $env = $env ?? Environment::SANDBOX;
        $this->baseUrl = Environment::baseUrl($env);
        $this->basePath = Environment::basePath($env);
        $this->http = new HttpClient(['timeout' => 30.0]);
    }

    /**
     * @internal Constructor for testing with custom base URL.
     */
    public static function forTesting(string $apiKey, string $apiSecret, string $baseUrl): self
    {
        $client = new self($apiKey, $apiSecret);
        $client->baseUrl = $baseUrl;
        $client->basePath = '';
        return $client;
    }

    /** @return array */
    public function pay(array $request): array { return $this->execute('/pay', $request); }
    public function capture(array $request): array { return $this->execute('/capture', $request); }
    public function cancel(array $request): array {
        if (!isset($request['reason'])) $request['reason'] = 'MERCHANT_MANUAL';
        return $this->execute('/cancel', $request);
    }
    public function inquiryPayment(array $request): array { return $this->execute('/inquiry-payment', $request); }
    public function inquiryRefund(array $request): array { return $this->execute('/inquiry-refund', $request); }
    public function refund(array $request): array {
        if (!isset($request['reason'])) $request['reason'] = 'ISV退款';
        return $this->execute('/refund', $request);
    }

    private function execute(string $path, array $request, int $attempt = 0): array
    {
        $body = json_encode($request, JSON_UNESCAPED_UNICODE);
        $timestamp = (string)(int)(microtime(true) * 1000);
        $signingPath = $this->basePath . $path;
        $signature = Signature::sign($this->apiSecret, 'POST', $signingPath, $timestamp, $body);
        $requestId = bin2hex(random_bytes(4));

        try {
            $resp = $this->http->post($this->baseUrl . $path, [
                'headers' => [
                    'Content-Type'  => 'application/json',
                    'X-Api-Key'     => $this->apiKey,
                    'X-Signature'   => $signature,
                    'X-Timestamp'   => $timestamp,
                    'X-Request-Id'  => $requestId,
                ],
                'body' => $body,
            ]);
        } catch (RequestException $e) {
            throw new PaygateException(ErrorCode::NETWORK_ERROR, 'Network request failed: ' . $e->getMessage());
        }

        $statusCode = $resp->getStatusCode();
        if (in_array($statusCode, [429, 502, 503, 504]) && $attempt < self::MAX_RETRIES) {
            usleep((int)pow(2, $attempt + 1) * 1000000);
            return $this->execute($path, $request, $attempt + 1);
        }

        $data = json_decode((string)$resp->getBody(), true);
        if (!$data) {
            throw new PaygateException(ErrorCode::UNKNOWN_ERROR, 'Failed to parse response');
        }

        $code = (string)($data['code'] ?? '');
        if ($code !== '200') {
            throw new PaygateException($code, (string)($data['message'] ?? ''), $statusCode);
        }

        return $data;
    }
}
