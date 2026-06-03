<?php

use PHPUnit\Framework\TestCase;
use Paygate\Sdk\Signature;

class SignatureTest extends TestCase
{
    public function testConsistent(): void
    {
        $s1 = Signature::sign('secret', 'POST', '/pay', '1700000000000', '{"amount":"100"}');
        $s2 = Signature::sign('secret', 'POST', '/pay', '1700000000000', '{"amount":"100"}');
        $this->assertStringStartsWith('sha256=', $s1);
        $this->assertEquals($s1, $s2);
    }

    public function testDifferentBody(): void
    {
        $s1 = Signature::sign('secret', 'POST', '/pay', '1700000000000', '{"amount":"100"}');
        $s2 = Signature::sign('secret', 'POST', '/pay', '1700000000000', '{"amount":"200"}');
        $this->assertNotEquals($s1, $s2);
    }

    public function testVerifyValid(): void
    {
        $s = Signature::sign('secret', 'POST', '/pay', '1700000000000', 'body');
        $this->assertTrue(Signature::verify('secret', $s, 'POST', '/pay', '1700000000000', 'body'));
    }

    public function testVerifyTampered(): void
    {
        $s = Signature::sign('secret', 'POST', '/pay', '1700000000000', 'body');
        $this->assertFalse(Signature::verify('secret', $s, 'POST', '/pay', '1700000000000', 'tampered'));
    }
}
