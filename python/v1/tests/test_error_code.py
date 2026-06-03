from paygate_sdk.error_code import ErrorCode, error_code_from_code


class TestErrorCode:
    def test_map_all(self):
        assert error_code_from_code("40001") == ErrorCode.INVALID_API_KEY
        assert error_code_from_code("40002") == ErrorCode.INVALID_SIGNATURE
        assert error_code_from_code("40003") == ErrorCode.REQUEST_EXPIRED
        assert error_code_from_code("40004") == ErrorCode.INVALID_PARAMETER
        assert error_code_from_code("40401") == ErrorCode.PAYMENT_NOT_FOUND
        assert error_code_from_code("40402") == ErrorCode.REFUND_NOT_FOUND
        assert error_code_from_code("40901") == ErrorCode.INVALID_PAYMENT_STATUS
        assert error_code_from_code("40902") == ErrorCode.DUPLICATE_REQUEST
        assert error_code_from_code("50001") == ErrorCode.NETWORK_ERROR
        assert error_code_from_code("50002") == ErrorCode.SERVER_ERROR
        assert error_code_from_code("50003") == ErrorCode.RATE_LIMITED
        assert error_code_from_code("59999") == ErrorCode.UNKNOWN_ERROR

    def test_unknown(self):
        assert error_code_from_code("99999") == ErrorCode.UNKNOWN_ERROR

    def test_count(self):
        assert len(ErrorCode) == 12
