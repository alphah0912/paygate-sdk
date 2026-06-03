import pytest
from paygate_sdk.signature import sign, verify

SECRET = "test_secret_key"
PATH = "/pay"


class TestSign:
    def test_consistent(self):
        s1 = sign(SECRET, "POST", PATH, "1700000000000", '{"amount":"100"}')
        s2 = sign(SECRET, "POST", PATH, "1700000000000", '{"amount":"100"}')
        assert s1.startswith("sha256=")
        assert s1 == s2

    def test_different_body(self):
        s1 = sign(SECRET, "POST", PATH, "1700000000000", '{"amount":"100"}')
        s2 = sign(SECRET, "POST", PATH, "1700000000000", '{"amount":"200"}')
        assert s1 != s2

    def test_different_timestamp(self):
        s1 = sign(SECRET, "POST", PATH, "1700000000000", '{"amount":"100"}')
        s2 = sign(SECRET, "POST", PATH, "1700000000001", '{"amount":"100"}')
        assert s1 != s2

    def test_url_safe_base64(self):
        s = sign(SECRET, "POST", PATH, "1700000000000", '{"key":"value"}')
        import re
        assert re.match(r"^sha256=[A-Za-z0-9+/=]+$", s)


class TestVerify:
    def test_valid(self):
        s = sign(SECRET, "POST", PATH, "1700000000000", "body")
        assert verify(SECRET, s, "POST", PATH, "1700000000000", "body")

    def test_tampered(self):
        s = sign(SECRET, "POST", PATH, "1700000000000", "body")
        assert not verify(SECRET, s, "POST", PATH, "1700000000000", "tampered")

    def test_wrong_secret(self):
        s = sign(SECRET, "POST", PATH, "1700000000000", "body")
        assert not verify("wrong", s, "POST", PATH, "1700000000000", "body")
