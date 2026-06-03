package client

import (
	"crypto/hmac"
	"crypto/sha256"
	"crypto/subtle"
	"encoding/base64"
)

const signaturePrefix = "sha256="

// Sign computes an HMAC-SHA256 signature for a PayGate API request.
// Sign string: METHOD\nPATH\nTIMESTAMP\nBODY
func Sign(apiSecret, method, path, timestamp, body string) string {
	signStr := method + "\n" + path + "\n" + timestamp + "\n" + body
	mac := hmac.New(sha256.New, []byte(apiSecret))
	mac.Write([]byte(signStr))
	return signaturePrefix + base64.StdEncoding.EncodeToString(mac.Sum(nil))
}

// Verify checks a signature against the expected value.
func Verify(secret, expectedSig, method, path, timestamp, body string) bool {
	actual := Sign(secret, method, path, timestamp, body)
	return subtle.ConstantTimeCompare([]byte(actual), []byte(expectedSig)) == 1
}
