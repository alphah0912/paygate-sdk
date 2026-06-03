<?php

namespace Paygate\Sdk;

/**
 * Target environment for the PayGate API.
 *
 * @author alphah
 * @since 1.0.0
 */
class Environment
{
    /** Sandbox testing environment. */
    const SANDBOX = ['host' => 'https://sandbox.antom.com', 'basePath' => '/api/gateway/v1'];

    /** Live production environment. */
    const LIVE = ['host' => 'https://api.antom.com', 'basePath' => '/gateway/v1'];

    public static function baseUrl(array $env): string
    {
        return $env['host'] . $env['basePath'];
    }

    public static function basePath(array $env): string
    {
        return $env['basePath'];
    }
}
