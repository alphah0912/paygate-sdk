import { describe, it, expect } from 'vitest';
import { ErrorCode, errorCodeFromCode } from '../src/error-code';

describe('errorCodeFromCode', () => {
  it('should map all defined codes', () => {
    expect(errorCodeFromCode('40001')).toBe(ErrorCode.INVALID_API_KEY);
    expect(errorCodeFromCode('40002')).toBe(ErrorCode.INVALID_SIGNATURE);
    expect(errorCodeFromCode('40003')).toBe(ErrorCode.REQUEST_EXPIRED);
    expect(errorCodeFromCode('40004')).toBe(ErrorCode.INVALID_PARAMETER);
    expect(errorCodeFromCode('40401')).toBe(ErrorCode.PAYMENT_NOT_FOUND);
    expect(errorCodeFromCode('40402')).toBe(ErrorCode.REFUND_NOT_FOUND);
    expect(errorCodeFromCode('40901')).toBe(ErrorCode.INVALID_PAYMENT_STATUS);
    expect(errorCodeFromCode('40902')).toBe(ErrorCode.DUPLICATE_REQUEST);
    expect(errorCodeFromCode('50001')).toBe(ErrorCode.NETWORK_ERROR);
    expect(errorCodeFromCode('50002')).toBe(ErrorCode.SERVER_ERROR);
    expect(errorCodeFromCode('50003')).toBe(ErrorCode.RATE_LIMITED);
    expect(errorCodeFromCode('59999')).toBe(ErrorCode.UNKNOWN_ERROR);
  });

  it('should return UNKNOWN_ERROR for unrecognized code', () => {
    expect(errorCodeFromCode('99999')).toBe(ErrorCode.UNKNOWN_ERROR);
    expect(errorCodeFromCode('')).toBe(ErrorCode.UNKNOWN_ERROR);
  });
});

describe('ErrorCode', () => {
  it('should have 12 error codes', () => {
    expect(Object.keys(ErrorCode).length).toBe(12);
  });
});
