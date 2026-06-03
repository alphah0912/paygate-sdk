package com.paygate.sdk;

/**
 * Target environment for the PayGate API.
 * Determines the base URL used for all requests.
 *
 * @author alphah
 * @since 1.0.0
 */
public enum Environment {

    /** Sandbox testing environment with no real transactions. */
    SANDBOX("https://sandbox.antom.com/api/gateway/v1"),

    /** Live production environment with real transactions. */
    LIVE("https://api.antom.com/gateway/v1");

    /** API base URL for this environment, e.g. {@code https://sandbox.antom.com/api/gateway/v1} */
    private final String baseUrl;

    Environment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** @return the API base URL for this environment */
    public String getBaseUrl() {
        return baseUrl;
    }
}
