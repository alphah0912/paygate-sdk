package com.paygate.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void shouldMapAllDefinedCodes() {
        assertThat(ErrorCode.fromCode("40001")).isEqualTo(ErrorCode.INVALID_API_KEY);
        assertThat(ErrorCode.fromCode("40002")).isEqualTo(ErrorCode.INVALID_SIGNATURE);
        assertThat(ErrorCode.fromCode("40003")).isEqualTo(ErrorCode.REQUEST_EXPIRED);
        assertThat(ErrorCode.fromCode("40004")).isEqualTo(ErrorCode.INVALID_PARAMETER);
        assertThat(ErrorCode.fromCode("40401")).isEqualTo(ErrorCode.PAYMENT_NOT_FOUND);
        assertThat(ErrorCode.fromCode("40402")).isEqualTo(ErrorCode.REFUND_NOT_FOUND);
        assertThat(ErrorCode.fromCode("40901")).isEqualTo(ErrorCode.INVALID_PAYMENT_STATUS);
        assertThat(ErrorCode.fromCode("40902")).isEqualTo(ErrorCode.DUPLICATE_REQUEST);
        assertThat(ErrorCode.fromCode("50001")).isEqualTo(ErrorCode.NETWORK_ERROR);
        assertThat(ErrorCode.fromCode("50002")).isEqualTo(ErrorCode.SERVER_ERROR);
        assertThat(ErrorCode.fromCode("50003")).isEqualTo(ErrorCode.RATE_LIMITED);
        assertThat(ErrorCode.fromCode("59999")).isEqualTo(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    void shouldReturnUnknownForUnrecognizedCode() {
        assertThat(ErrorCode.fromCode("99999")).isEqualTo(ErrorCode.UNKNOWN_ERROR);
        assertThat(ErrorCode.fromCode("")).isEqualTo(ErrorCode.UNKNOWN_ERROR);
    }

    @Test
    void allCodesShouldBeAlignedWithSpec() {
        // Verify counts match spec/error-codes.yaml
        assertThat(ErrorCode.values()).hasSize(12);
    }
}
