package webhook

// PaymentResultInfo holds 3DS result data.
type PaymentResultInfo struct {
	ThreeDSResult string `json:"threeDSResult,omitempty"`
}

// PaymentResult is the ISV payment result webhook event.
type PaymentResult struct {
	Type               string             `json:"-"`
	PaymentRequestID   string             `json:"paymentRequestId"`
	Status             string             `json:"status"`
	Amount             string             `json:"amount"`
	Currency           string             `json:"currency"`
	TradeTime          string             `json:"tradeTime"`
	Message            string             `json:"message"`
	PaymentResultInfo  *PaymentResultInfo `json:"paymentResultInfo,omitempty"`
}

func (e PaymentResult) EventType() string { return "payment.result" }

// SignSuccess is the sign.success notification event.
type SignSuccess struct {
	Type                string `json:"type"`
	ReferenceMerchantID string `json:"referenceMerchantId"`
	Message             string `json:"message"`
	Timestamp           string `json:"timestamp"`
}

// PaymentCompleted is the payment.completed notification event.
type PaymentCompleted struct {
	Type            string `json:"type"`
	PlatformTradeNo string `json:"platformTradeNo"`
	Status          string `json:"status"`
	Timestamp       string `json:"timestamp"`
}

// RefundCompleted is the refund.completed notification event.
type RefundCompleted struct {
	Type          string `json:"type"`
	RefundTradeNo string `json:"refundTradeNo"`
	Status        string `json:"status"`
	Timestamp     string `json:"timestamp"`
}

// Event is the interface for all webhook events.
type Event interface {
	EventType() string
}

func (e SignSuccess) EventType() string      { return "sign.success" }
func (e PaymentCompleted) EventType() string { return "payment.completed" }
func (e RefundCompleted) EventType() string  { return "refund.completed" }
