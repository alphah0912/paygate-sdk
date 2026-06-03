package client

// ── Requests ──────────────────────────────────────────────

// PayRequest params for /pay endpoint.
type PayRequest struct {
	Amount                  string `json:"amount"`
	PaymentMethodCode       string `json:"paymentMethodCode"`
	TerminalType            string `json:"terminalType"`
	SettlementCurrency      string `json:"settlementCurrency"`
	Currency                string `json:"currency,omitempty"`
	OrderDescription        string `json:"orderDescription,omitempty"`
	BuyerCountry            string `json:"buyerCountry,omitempty"`
	ReferenceBuyerID        string `json:"referenceBuyerId,omitempty"`
	BuyerPhoneNo            string `json:"buyerPhoneNo,omitempty"`
	PaymentMethod           string `json:"paymentMethod,omitempty"`
	AvailablePaymentMethod  string `json:"availablePaymentMethod,omitempty"`
	SavedPaymentMethods     string `json:"savedPaymentMethods,omitempty"`
	OsType                  string `json:"osType,omitempty"`
}

// CaptureRequest params for /capture endpoint.
type CaptureRequest struct {
	PaymentRequestID string `json:"paymentRequestId"`
}

// CancelRequest params for /cancel endpoint.
type CancelRequest struct {
	PaymentRequestID string `json:"paymentRequestId"`
	Reason           string `json:"reason"`
}

// InquiryPaymentRequest params for /inquiry-payment endpoint.
type InquiryPaymentRequest struct {
	PaymentRequestID string `json:"paymentRequestId"`
}

// InquiryRefundRequest params for /inquiry-refund endpoint.
type InquiryRefundRequest struct {
	RefundTransactionID string `json:"refundTransactionId,omitempty"`
	PaymentRequestID    string `json:"paymentRequestId,omitempty"`
}

// RefundRequest params for /refund endpoint.
type RefundRequest struct {
	PaymentRequestID string `json:"paymentRequestId"`
	RefundAmount     string `json:"refundAmount"`
	Reason           string `json:"reason"`
}

// ── Responses ─────────────────────────────────────────────

type apiResponse struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

func (r apiResponse) IsSuccess() bool {
	return r.Code == "200" || r.Code == "0"
}

// PayResponse for /pay.
type PayResponse struct {
	apiResponse
	RedirectURL      string `json:"redirectUrl"`
	PaymentRequestID string `json:"paymentRequestId"`
}

// CaptureResponse for /capture.
type CaptureResponse struct {
	apiResponse
	CaptureID string `json:"captureId"`
	Status    string `json:"status"`
}

// CancelResponse for /cancel.
type CancelResponse struct {
	apiResponse
	Status string `json:"status"`
}

// InquiryPaymentResponse for /inquiry-payment.
type InquiryPaymentResponse struct {
	apiResponse
	TradeStatus   string `json:"tradeStatus"`
	PaymentID     string `json:"paymentId"`
	PaymentStatus string `json:"paymentStatus"`
}

// InquiryRefundResponse for /inquiry-refund.
type InquiryRefundResponse struct {
	apiResponse
	TradeStatus  string `json:"tradeStatus"`
	RefundID     string `json:"refundId"`
	RefundStatus string `json:"refundStatus"`
}

// RefundResponse for /refund.
type RefundResponse struct {
	apiResponse
	RefundTransactionID string `json:"refundTransactionId"`
	RefundStatus        string `json:"refundStatus"`
}
