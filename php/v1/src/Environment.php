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
    const SANDBOX = ['host' => 'https://sandbox.backend.hunanxiaojunzi.com', 'basePath' => '/api/sandbox/gateway/v1'];

    /** Live production environment. */
    const LIVE = ['host' => 'https://api.backend.hunanxiaojunzi.com', 'basePath' => '/api/gateway/v1'];

    public static function baseUrl(array $env): string
    {
        return $env['host'] . $env['basePath'];
    }

    public static function basePath(array $env): string
    {
        return $env['basePath'];
    }
}
