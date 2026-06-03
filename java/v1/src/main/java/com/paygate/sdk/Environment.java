package com.paygate.sdk;

/**
 * Target environment for the PayGate API.
 * Determines the base URL and signing path prefix used for all requests.
 *
 * @author alphah
 * @since 1.0.0
 */
public enum Environment {

    /** Sandbox testing environment with no real transactions. */
    SANDBOX("https://sandbox.antom.com", "/api/gateway/v1"),

    /** Live production environment with real transactions. */
    LIVE("https://api.antom.com", "/gateway/v1");

    /** API host for this environment */
    private final String host;

    /** API path prefix for this environment, used for signing (e.g. /api/gateway/v1) */
    private final String basePath;

    Environment(String host, String basePath) {
        this.host = host;
        this.basePath = basePath;
    }

    /** @return the full API base URL for this environment */
    public String getBaseUrl() {
        return host + basePath;
    }

    /** @return the path prefix used for HMAC signing (e.g. /api/gateway/v1) */
    public String getBasePath() {
        return basePath;
    }
}
