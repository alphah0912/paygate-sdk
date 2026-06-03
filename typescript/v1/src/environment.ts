/**
 * Target environment for the PayGate API.
 * Determines the base URL and signing path prefix used for all requests.
 *
 * @author alphah
 * @since 1.0.0
 */
export enum Environment {
  /** Sandbox testing environment with no real transactions. */
  SANDBOX = 'https://sandbox.antom.com/api/gateway/v1',

  /** Live production environment with real transactions. */
  LIVE = 'https://api.antom.com/gateway/v1',
}

/** @internal Extracts the path prefix from a base URL for HMAC signing. */
export function getBasePath(baseUrl: string): string {
  const url = new URL(baseUrl);
  return url.pathname; // e.g. /api/gateway/v1
}
