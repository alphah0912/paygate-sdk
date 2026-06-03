import { ErrorCode, errorCodeFromCode } from './error-code';

/**
 * Exception thrown when the PayGate API returns an error,
 * or when a client-side error (network, serialization) occurs.
 *
 * Always carries an `errorCode` that maps to {@link ErrorCode}.
 *
 * @author alphah
 * @since 1.0.0
 */
export class PaygateException extends Error {
  /** Numeric error code string, maps to ErrorCode */
  readonly errorCode: string;

  /** HTTP status code from the server, or 0 if not applicable */
  readonly httpStatus: number;

  constructor(errorCode: string, message: string, httpStatus: number = 0) {
    super(message);
    this.name = 'PaygateException';
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  /** @returns the ErrorCode enum constant matching this error */
  get errorCodeEnum(): ErrorCode {
    return errorCodeFromCode(this.errorCode);
  }
}
