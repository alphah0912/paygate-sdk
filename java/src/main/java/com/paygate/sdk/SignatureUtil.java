package com.paygate.sdk;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * HMAC-SHA256 signing and verification for PayGate API request authentication.
 *
 * <p>Signing string format (newline-separated):
 * <pre>{@code
 * HTTP_METHOD
 * REQUEST_PATH
 * TIMESTAMP
 * REQUEST_BODY
 * }</pre>
 *
 * <p>Output format: {@code sha256=<base64-encoded-hmac>}
 *
 * @author alphah
 * @since 1.0.0
 */
public final class SignatureUtil {

    /** JCA algorithm name for HMAC-SHA256 */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /** Prefix prepended to every generated signature value */
    private static final String SIGNATURE_PREFIX = "sha256=";

    private SignatureUtil() {
    }

    /**
     * Computes the HMAC-SHA256 signature for a PayGate API request.
     *
     * @param apiSecret merchant API secret
     * @param method    HTTP method, uppercase (e.g. {@code POST})
     * @param path      request path (e.g. {@code /pay})
     * @param timestamp Unix millisecond timestamp string
     * @param body      JSON request body
     * @return the signature in {@code sha256=<base64>} format
     */
    public static String sign(String apiSecret, String method, String path, String timestamp, String body) {
        String signStr = method + "\n" + path + "\n" + timestamp + "\n" + body;
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                    apiSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(signStr.getBytes(StandardCharsets.UTF_8));
            return SIGNATURE_PREFIX + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new PaygateException(ErrorCode.UNKNOWN_ERROR.getCode(), "HMAC signature failed", e);
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
     * @param body              raw JSON body used in the original signature
     * @return {@code true} if the signature matches
     */
    public static boolean verify(String secret, String expectedSignature,
            String method, String path, String timestamp, String body) {
        String actual = sign(secret, method, path, timestamp, body);
        return actual.equals(expectedSignature);
    }
}
