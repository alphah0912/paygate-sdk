/**
 * Quick-start example: create a payment via the PayGate SDK.
 * Run: npx ts-node examples/pay-example.ts
 *
 * @author alphah
 * @since 1.0.0
 */
import { PaygateClient, Environment } from '../src/index';

const client = new PaygateClient({
  apiKey: 'mk_test_your_api_key',
  apiSecret: 'your_api_secret',
  environment: Environment.SANDBOX,
});

async function main() {
  try {
    const resp = await client.pay({
      amount: '100.00',
      paymentMethodCode: 'ALIPAY_CN',
      terminalType: 'WEB',
      settlementCurrency: 'USD',
      orderDescription: 'Test order',
      buyerCountry: 'CN',
    });
    console.log('Payment created!');
    console.log('  paymentRequestId:', resp.paymentRequestId);
    console.log('  redirectUrl:', resp.redirectUrl);
  } catch (e: any) {
    console.error('Error [' + e.errorCode + ']:', e.message);
  }
}

main();
