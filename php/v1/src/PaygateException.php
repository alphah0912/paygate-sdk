<?php

namespace Paygate\Sdk;

/**
 * Exception thrown when the PayGate API returns an error.
 *
 * @author alphah
 * @since 1.0.0
 */
class PaygateException extends \RuntimeException
{
    /** @var string Numeric error code string */
    private string $errorCode;

    /** @var int HTTP status code from the server, or 0 */
    private int $httpStatus;

    public function __construct(string $errorCode, string $message, int $httpStatus = 0)
    {
        parent::__construct($message);
        $this->errorCode = $errorCode;
        $this->httpStatus = $httpStatus;
    }

    public function getErrorCode(): string { return $this->errorCode; }
    public function getHttpStatus(): int { return $this->httpStatus; }
}
