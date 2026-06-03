/**
 * Unified error codes returned by the PayGate API.
 * Each SDK language maps to the same codes defined in `spec/error-codes.yaml`.
 *
 * @author alphah
 * @since 1.0.0
 */
export enum ErrorCode {
  INVALID_API_KEY = '40001',
  INVALID_SIGNATURE = '40002',
  REQUEST_EXPIRED = '40003',
  INVALID_PARAMETER = '40004',
  PAYMENT_NOT_FOUND = '40401',
  REFUND_NOT_FOUND = '40402',
  INVALID_PAYMENT_STATUS = '40901',
  DUPLICATE_REQUEST = '40902',
  NETWORK_ERROR = '50001',
  SERVER_ERROR = '50002',
  RATE_LIMITED = '50003',
  UNKNOWN_ERROR = '59999',
}

/** Human-readable messages for each error code. */
const DEFAULT_MESSAGES: Record<string, string> = {
  '40001': 'Invalid API key',
  '40002': 'Signature verification failed',
  '40003': 'Request timestamp expired',
  '40004': 'Invalid parameter',
  '40401': 'Payment not found',
  '40402': 'Refund not found',
  '40901': 'Invalid payment status for this operation',
  '40902': 'Duplicate request',
  '50001': 'Network request failed',
  '50002': 'Server returned error',
  '50003': 'Rate limited',
  '59999': 'Unknown error',
};

/**
 * Resolves a raw error code string to its enum constant.
 * @param code numeric error code from the API response
 * @returns matching ErrorCode, or UNKNOWN_ERROR if unrecognized
 */
export function errorCodeFromCode(code: string): ErrorCode {
  const found = Object.values(ErrorCode).find(v => v === code);
  return (found as ErrorCode) ?? ErrorCode.UNKNOWN_ERROR;
}

/**
 * Returns the default human-readable message for an error code.
 */
export function errorCodeMessage(code: string): string {
  return DEFAULT_MESSAGES[code] ?? 'Unknown error';
}
