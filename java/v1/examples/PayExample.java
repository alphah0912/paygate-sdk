package examples;

import com.paygate.sdk.Environment;
import com.paygate.sdk.PaygateClient;
import com.paygate.sdk.PaygateException;
import com.paygate.sdk.request.PayRequest;
import com.paygate.sdk.response.PayResponse;

/**
 * Quick-start example: create a payment via the PayGate SDK.
 *
 * <p>Replace {@code apiKey} and {@code apiSecret} with real values before running.
 *
 * @author alphah
 * @since 1.0.0
 */
public class PayExample {

    public static void main(String[] args) {
        PaygateClient client = PaygateClient.builder()
                .apiKey("mk_test_your_api_key")
                .apiSecret("your_api_secret")
                .environment(Environment.SANDBOX)
                .build();

        PayRequest request = PayRequest.builder()
                .amount("100.00")
                .paymentMethodCode("ALIPAY_CN")
                .terminalType("WEB")
                .settlementCurrency("USD")
                .orderDescription("Test order")
                .buyerCountry("CN")
                .build();

        try {
            PayResponse response = client.pay(request);
            System.out.println("Payment created!");
            System.out.println("  paymentRequestId: " + response.getPaymentRequestId());
            System.out.println("  redirectUrl: " + response.getRedirectUrl());
        } catch (PaygateException e) {
            System.err.println("Error [" + e.getErrorCode() + "]: " + e.getMessage());
        }
    }
}
