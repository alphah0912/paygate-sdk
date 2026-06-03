<?php

namespace Paygate\Sdk;

/**
 * HMAC-SHA256 signing and verification for PayGate API requests.
 *
 * Sign string: METHOD\nPATH\nTIMESTAMP\nBODY
 * Output: sha256=<base64-hmac>
 *
 * @author alphah
 * @since 1.0.0
 */
class Signature
{
    /**
     * Compute HMAC-SHA256 signature.
     */
    public static function sign(string $apiSecret, string $method, string $path, string $timestamp, string $body): string
    {
        $signStr = "{$method}\n{$path}\n{$timestamp}\n{$body}";
        $hash = hash_hmac('sha256', $signStr, $apiSecret, true);
        return 'sha256=' . base64_encode($hash);
    }

    /**
     * Verify a signature against the expected value.
     */
    public static function verify(string $secret, string $expectedSig, string $method, string $path, string $timestamp, string $body): bool
    {
        $actual = self::sign($secret, $method, $path, $timestamp, $body);
        return hash_equals($actual, $expectedSig);
    }
}
