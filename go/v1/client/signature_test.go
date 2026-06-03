package client

import (
	"testing"
)

func TestSignConsistent(t *testing.T) {
	s1 := Sign("secret", "POST", "/pay", "1700000000000", `{"amount":"100"}`)
	s2 := Sign("secret", "POST", "/pay", "1700000000000", `{"amount":"100"}`)
	if s1 != s2 {
		t.Error("signatures should be consistent")
	}
}

func TestSignDifferentBody(t *testing.T) {
	s1 := Sign("secret", "POST", "/pay", "1700000000000", `{"amount":"100"}`)
	s2 := Sign("secret", "POST", "/pay", "1700000000000", `{"amount":"200"}`)
	if s1 == s2 {
		t.Error("different bodies should produce different signatures")
	}
}

func TestVerifyValid(t *testing.T) {
	s := Sign("secret", "POST", "/pay", "1700000000000", "body")
	if !Verify("secret", s, "POST", "/pay", "1700000000000", "body") {
		t.Error("valid signature should verify")
	}
}

func TestVerifyTampered(t *testing.T) {
	s := Sign("secret", "POST", "/pay", "1700000000000", "body")
	if Verify("secret", s, "POST", "/pay", "1700000000000", "tampered") {
		t.Error("tampered body should not verify")
	}
}
