package examples;

import com.paygate.sdk.webhook.WebhookEvent;
import com.paygate.sdk.webhook.WebhookHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Example HTTP server that receives and validates PayGate webhook notifications.
 *
 * <p>Listens on {@code http://localhost:8080/webhook}. Verifies ISV signature
 * or NotificationService secret, then prints the parsed event.
 *
 * @author alphah
 * @since 1.0.0
 */
public class WebhookServerExample {

    public static void main(String[] args) throws IOException {
        WebhookHandler handler = new WebhookHandler("isv_webhook_secret", "notify_secret");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/webhook", exchange -> {
            byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            String path = exchange.getRequestURI().getPath();

            Map<String, List<String>> headers = new HashMap<>();
            exchange.getRequestHeaders().forEach(headers::put);

            try {
                WebhookEvent event = handler.handle(headers, body, path);
                System.out.println("Received event: " + event.getType());

                if (event instanceof WebhookEvent.PaymentResult pr) {
                    System.out.println("  Payment " + pr.getPaymentRequestId() + ": " + pr.getStatus());
                }

                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            } catch (Exception e) {
                System.err.println("Webhook verification failed: " + e.getMessage());
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
            }
        });

        server.start();
        System.out.println("Webhook server listening on http://localhost:8080/webhook");
    }
}
