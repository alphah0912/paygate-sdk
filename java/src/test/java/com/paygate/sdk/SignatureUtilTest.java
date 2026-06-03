package com.paygate.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureUtilTest {

    private static final String SECRET = "test_secret_key";
    private static final String PATH = "/pay";
    private static final String METHOD = "POST";

    @Test
    void shouldProduceConsistentSignature() {
        String sig1 = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "{\"amount\":\"100\"}");
        String sig2 = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "{\"amount\":\"100\"}");

        assertThat(sig1).startsWith("sha256=");
        assertThat(sig1).isEqualTo(sig2);
    }

    @Test
    void differentBodyShouldProduceDifferentSignature() {
        String sig1 = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "{\"amount\":\"100\"}");
        String sig2 = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "{\"amount\":\"200\"}");

        assertThat(sig1).isNotEqualTo(sig2);
    }

    @Test
    void differentTimestampShouldProduceDifferentSignature() {
        String sig1 = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "{\"amount\":\"100\"}");
        String sig2 = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000001", "{\"amount\":\"100\"}");

        assertThat(sig1).isNotEqualTo(sig2);
    }

    @Test
    void shouldVerifyValidSignature() {
        String sig = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "body");

        boolean valid = SignatureUtil.verify(SECRET, sig, METHOD, PATH, "1700000000000", "body");
        assertThat(valid).isTrue();
    }

    @Test
    void shouldRejectTamperedBody() {
        String sig = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "body");

        boolean valid = SignatureUtil.verify(SECRET, sig, METHOD, PATH, "1700000000000", "tampered");
        assertThat(valid).isFalse();
    }

    @Test
    void shouldRejectWrongSecret() {
        String sig = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "body");

        boolean valid = SignatureUtil.verify("wrong_secret", sig, METHOD, PATH, "1700000000000", "body");
        assertThat(valid).isFalse();
    }

    @Test
    void signatureShouldBeUrlSafeBase64() {
        String sig = SignatureUtil.sign(SECRET, METHOD, PATH, "1700000000000", "{\"key\":\"value\"}");

        assertThat(sig).matches("^sha256=[A-Za-z0-9+/=]+$");
    }
}
