<?php

namespace Paygate\Sdk;

/**
 * Unified error codes returned by the PayGate API.
 *
 * @author alphah
 * @since 1.0.0
 */
class ErrorCode
{
    const INVALID_API_KEY = '40001';
    const INVALID_SIGNATURE = '40002';
    const REQUEST_EXPIRED = '40003';
    const INVALID_PARAMETER = '40004';
    const PAYMENT_NOT_FOUND = '40401';
    const REFUND_NOT_FOUND = '40402';
    const INVALID_PAYMENT_STATUS = '40901';
    const DUPLICATE_REQUEST = '40902';
    const NETWORK_ERROR = '50001';
    const SERVER_ERROR = '50002';
    const RATE_LIMITED = '50003';
    const UNKNOWN_ERROR = '59999';

    private static $messages = [
        '40001' => 'Invalid API key',
        '40002' => 'Signature verification failed',
        '40003' => 'Request timestamp expired',
        '40004' => 'Invalid parameter',
        '40401' => 'Payment not found',
        '40402' => 'Refund not found',
        '40901' => 'Invalid payment status for this operation',
        '40902' => 'Duplicate request',
        '50001' => 'Network request failed',
        '50002' => 'Server returned error',
        '50003' => 'Rate limited',
        '59999' => 'Unknown error',
    ];

    public static function message(string $code): string
    {
        return self::$messages[$code] ?? 'Unknown error';
    }
}
