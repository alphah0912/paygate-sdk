package com.paygate.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paygate.sdk.request.CancelRequest;
import com.paygate.sdk.request.CaptureRequest;
import com.paygate.sdk.request.InquiryPaymentRequest;
import com.paygate.sdk.request.InquiryRefundRequest;
import com.paygate.sdk.request.PayRequest;
import com.paygate.sdk.request.RefundRequest;
import com.paygate.sdk.response.CancelResponse;
import com.paygate.sdk.response.CaptureResponse;
import com.paygate.sdk.response.InquiryPaymentResponse;
import com.paygate.sdk.response.InquiryRefundResponse;
import com.paygate.sdk.response.PayResponse;
import com.paygate.sdk.response.RefundResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldSerializePayRequestWithRequiredFieldsOnly() throws Exception {
        PayRequest req = PayRequest.builder()
                .amount("100.00")
                .paymentMethodCode("ALIPAY_CN")
                .terminalType("WEB")
                .settlementCurrency("USD")
                .build();

        String json = MAPPER.writeValueAsString(req);

        assertThat(json).contains("\"amount\":\"100.00\"");
        assertThat(json).contains("\"paymentMethodCode\":\"ALIPAY_CN\"");
        assertThat(json).doesNotContain("orderDescription");
    }

    @Test
    void shouldIncludeOptionalFieldsWhenSet() throws Exception {
        PayRequest req = PayRequest.builder()
                .amount("100.00")
                .paymentMethodCode("ALIPAY_CN")
                .terminalType("WEB")
                .settlementCurrency("USD")
                .orderDescription("Test order")
                .buyerCountry("CN")
                .build();

        String json = MAPPER.writeValueAsString(req);

        assertThat(json).contains("\"orderDescription\":\"Test order\"");
        assertThat(json).contains("\"buyerCountry\":\"CN\"");
    }

    @Test
    void shouldRejectPayRequestMissingRequiredFields() {
        assertThatThrownBy(() -> PayRequest.builder().build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDeserializePayResponse() throws Exception {
        String json = "{\"code\":\"0\",\"message\":\"success\",\"redirectUrl\":\"https://example.com/checkout\",\"paymentRequestId\":\"REQ123\"}";

        PayResponse resp = MAPPER.readValue(json, PayResponse.class);

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getRedirectUrl()).isEqualTo("https://example.com/checkout");
        assertThat(resp.getPaymentRequestId()).isEqualTo("REQ123");
    }

    @Test
    void shouldDeserializeErrorResponse() throws Exception {
        String json = "{\"code\":\"40001\",\"message\":\"Invalid API key\"}";

        PayResponse resp = MAPPER.readValue(json, PayResponse.class);

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getCode()).isEqualTo("40001");
    }

    @Test
    void cancelRequestShouldDefaultReason() {
        CancelRequest req = CancelRequest.builder()
                .paymentRequestId("REQ123")
                .build();

        assertThat(req.getReason()).isEqualTo("MERCHANT_MANUAL");
    }

    @Test
    void refundRequestShouldDefaultReason() {
        RefundRequest req = RefundRequest.builder()
                .paymentRequestId("REQ123")
                .refundAmount("50.00")
                .build();

        assertThat(req.getReason()).isEqualTo("ISV退款");
    }

    @Test
    void inquiryRefundShouldRequireAtLeastOneId() {
        assertThatThrownBy(() -> InquiryRefundRequest.builder().build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDeserializeAllResponseTypes() throws Exception {
        String json = "{\"code\":\"0\",\"message\":\"ok\"}";

        assertThat(MAPPER.readValue(json, CaptureResponse.class).isSuccess()).isTrue();
        assertThat(MAPPER.readValue(json, CancelResponse.class).isSuccess()).isTrue();
        assertThat(MAPPER.readValue(json, InquiryPaymentResponse.class).isSuccess()).isTrue();
        assertThat(MAPPER.readValue(json, InquiryRefundResponse.class).isSuccess()).isTrue();
        assertThat(MAPPER.readValue(json, RefundResponse.class).isSuccess()).isTrue();
    }
}
