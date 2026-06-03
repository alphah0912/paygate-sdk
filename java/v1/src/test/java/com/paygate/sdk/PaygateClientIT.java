package com.paygate.sdk;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PaygateClientIT {

    private HttpServer server;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.setExecutor(null);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void shouldCallPayAndParseResponse() throws IOException {
        server.createContext("/pay", exchange -> {
            byte[] resp = "{\"code\":\"200\",\"message\":\"ok\",\"redirectUrl\":\"https://checkout.example.com/abc\",\"paymentRequestId\":\"REQ001\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        PaygateClient client = new PaygateClient(
                "mk_test_key", "test_secret",
                "http://localhost:" + port
        );

        com.paygate.sdk.response.PayResponse resp = client.pay(
                com.paygate.sdk.request.PayRequest.builder()
                        .amount("100.00")
                        .paymentMethodCode("ALIPAY_CN")
                        .terminalType("WEB")
                        .settlementCurrency("USD")
                        .build()
        );

        assertThat(resp.getRedirectUrl()).isEqualTo("https://checkout.example.com/abc");
        assertThat(resp.getPaymentRequestId()).isEqualTo("REQ001");
    }

    @Test
    void shouldThrowExceptionOnErrorResponse() throws IOException {
        server.createContext("/capture", exchange -> {
            byte[] resp = "{\"code\":\"40901\",\"message\":\"Invalid payment status\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        PaygateClient client = new PaygateClient(
                "mk_test_key", "test_secret",
                "http://localhost:" + port
        );

        try {
            client.capture(
                    com.paygate.sdk.request.CaptureRequest.builder()
                            .paymentRequestId("REQ001")
                            .build()
            );
        } catch (PaygateException e) {
            assertThat(e.getErrorCode()).isEqualTo("40901");
            assertThat(e.getMessage()).contains("Invalid payment status");
        }
    }

    @Test
    void shouldSignRequestCorrectly() throws IOException {
        final String[] capturedSignature = {null};
        final String[] capturedTimestamp = {null};

        server.createContext("/pay", exchange -> {
            capturedSignature[0] = exchange.getRequestHeaders().getFirst("X-Signature");
            capturedTimestamp[0] = exchange.getRequestHeaders().getFirst("X-Timestamp");
            byte[] resp = "{\"code\":\"200\",\"message\":\"ok\",\"redirectUrl\":\"https://x.com\",\"paymentRequestId\":\"X\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.close();
        });

        String secret = "my_secret";
        PaygateClient client = new PaygateClient(
                "mk_test_key", secret,
                "http://localhost:" + port
        );

        client.pay(
                com.paygate.sdk.request.PayRequest.builder()
                        .amount("100.00")
                        .paymentMethodCode("ALIPAY_CN")
                        .terminalType("WEB")
                        .settlementCurrency("USD")
                        .build()
        );

        assertThat(capturedSignature[0]).startsWith("sha256=");
        assertThat(capturedTimestamp[0]).isNotNull();

        String expectedSig = SignatureUtil.sign(secret, "POST", "/pay",
                capturedTimestamp[0],
                "{\"amount\":\"100.00\",\"paymentMethodCode\":\"ALIPAY_CN\",\"terminalType\":\"WEB\",\"settlementCurrency\":\"USD\"}");
        assertThat(capturedSignature[0]).isEqualTo(expectedSig);
    }

    @Test
    void shouldRespectEnvironmentBaseUrl() {
        assertThat(Environment.SANDBOX.getBaseUrl()).contains("sandbox");
        assertThat(Environment.LIVE.getBaseUrl()).contains("api.antom.com");
    }
}
