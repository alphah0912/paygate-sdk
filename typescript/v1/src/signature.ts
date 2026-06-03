import { createHmac, timingSafeEqual } from 'crypto';
import { PaygateException } from './paygate-exception';
import { ErrorCode } from './error-code';

/** HMAC algorithm name */
const HMAC_ALGORITHM = 'sha256';

/** Prefix prepended to every generated signature value */
const SIGNATURE_PREFIX = 'sha256=';

/**
 * Computes the HMAC-SHA256 signature for a PayGate API request.
 *
 * Signing string format (newline-separated):
 *   METHOD
 *   PATH
 *   TIMESTAMP
 *   BODY
 *
 * Output format: `sha256=<base64-hmac>`
 *
 * @param apiSecret merchant API secret
 * @param method    HTTP method, uppercase (e.g. `POST`)
 * @param path      request path (e.g. `/pay`)
 * @param timestamp Unix millisecond timestamp string
 * @param body      JSON request body
 * @returns the signature in `sha256=<base64>` format
 */
export function sign(
  apiSecret: string,
  method: string,
  path: string,
  timestamp: string,
  body: string,
): string {
  const signStr = `${method}\n${path}\n${timestamp}\n${body}`;
  try {
    const hmac = createHmac(HMAC_ALGORITHM, apiSecret);
    hmac.update(signStr);
    const hash = hmac.digest('base64');
    return SIGNATURE_PREFIX + hash;
  } catch (e) {
    throw new PaygateException(ErrorCode.UNKNOWN_ERROR, 'HMAC signature failed: ' + (e as Error).message);
  }
}

/**
 * Verifies a signature against the expected value.
 *
 * @param secret            merchant API secret
 * @param expectedSignature the signature header value to verify
 * @param method            HTTP method
 * @param path              request path
 * @param timestamp         timestamp used in the original signature
 * @param body              raw JSON body
 * @returns true if the signature matches
 */
export function verify(
  secret: string,
  expectedSignature: string,
  method: string,
  path: string,
  timestamp: string,
  body: string,
): boolean {
  const actual = sign(secret, method, path, timestamp, body);
  const a = Buffer.from(actual);
  const b = Buffer.from(expectedSignature);
  if (a.length !== b.length) return false;
  return timingSafeEqual(a, b);
}
