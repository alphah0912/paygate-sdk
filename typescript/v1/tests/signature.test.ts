import { describe, it, expect } from 'vitest';
import { sign, verify } from '../src/signature';

const SECRET = 'test_secret_key';
const PATH = '/pay';
const METHOD = 'POST';

describe('sign', () => {
  it('should produce consistent signature', () => {
    const sig1 = sign(SECRET, METHOD, PATH, '1700000000000', '{"amount":"100"}');
    const sig2 = sign(SECRET, METHOD, PATH, '1700000000000', '{"amount":"100"}');
    expect(sig1).toMatch(/^sha256=/);
    expect(sig1).toBe(sig2);
  });

  it('should produce different signatures for different bodies', () => {
    const sig1 = sign(SECRET, METHOD, PATH, '1700000000000', '{"amount":"100"}');
    const sig2 = sign(SECRET, METHOD, PATH, '1700000000000', '{"amount":"200"}');
    expect(sig1).not.toBe(sig2);
  });

  it('should produce different signatures for different timestamps', () => {
    const sig1 = sign(SECRET, METHOD, PATH, '1700000000000', '{"amount":"100"}');
    const sig2 = sign(SECRET, METHOD, PATH, '1700000000001', '{"amount":"100"}');
    expect(sig1).not.toBe(sig2);
  });

  it('signature should be url-safe base64', () => {
    const sig = sign(SECRET, METHOD, PATH, '1700000000000', '{"key":"value"}');
    expect(sig).toMatch(/^sha256=[A-Za-z0-9+/=]+$/);
  });
});

describe('verify', () => {
  it('should verify valid signature', () => {
    const sig = sign(SECRET, METHOD, PATH, '1700000000000', 'body');
    expect(verify(SECRET, sig, METHOD, PATH, '1700000000000', 'body')).toBe(true);
  });

  it('should reject tampered body', () => {
    const sig = sign(SECRET, METHOD, PATH, '1700000000000', 'body');
    expect(verify(SECRET, sig, METHOD, PATH, '1700000000000', 'tampered')).toBe(false);
  });

  it('should reject wrong secret', () => {
    const sig = sign(SECRET, METHOD, PATH, '1700000000000', 'body');
    expect(verify('wrong_secret', sig, METHOD, PATH, '1700000000000', 'body')).toBe(false);
  });
});
