package webhook

import (
	"encoding/json"
	"fmt"

	"github.com/alphah0912/paygate-sdk/go/v1/client"
)

const (
	isvSigHeader = "X-Isv-Signature"
	isvTsHeader  = "X-Isv-Timestamp"
	whSigHeader  = "X-Webhook-Signature"
	whTsHeader   = "X-Webhook-Timestamp"
)

// Handler processes incoming webhook notifications.
type Handler struct {
	secret string
}

// NewHandler creates a new WebhookHandler.
func NewHandler(secret string) *Handler {
	return &Handler{secret: secret}
}

// Handle processes an incoming webhook request.
func (h *Handler) Handle(headers map[string][]string, body, notifyURL string) (Event, error) {
	if _, ok := headers[isvSigHeader]; ok {
		return h.handleISV(headers, body, notifyURL)
	}
	if _, ok := headers[whSigHeader]; ok {
		return h.handleNotification(headers, body, notifyURL)
	}
	return nil, client.NewPaygateError(client.ErrInvalidSignature, "No webhook signature header found")
}

func (h *Handler) handleISV(headers map[string][]string, body, notifyURL string) (Event, error) {
	sig := first(headers, isvSigHeader)
	ts := first(headers, isvTsHeader)
	if sig == "" || ts == "" {
		return nil, client.NewPaygateError(client.ErrInvalidSignature, "Missing ISV webhook signature headers")
	}
	if h.secret == "" {
		return nil, client.NewPaygateError(client.ErrInvalidSignature, "Webhook secret not configured")
	}
	if !client.Verify(h.secret, sig, "POST", notifyURL, ts, body) {
		return nil, client.NewPaygateError(client.ErrInvalidSignature, "ISV webhook signature mismatch")
	}
	var evt PaymentResult
	if err := json.Unmarshal([]byte(body), &evt); err != nil {
		return nil, client.NewPaygateError(client.ErrUnknownError, "Failed to parse ISV webhook payload: "+err.Error())
	}
	return evt, nil
}

func (h *Handler) handleNotification(headers map[string][]string, body, notifyURL string) (Event, error) {
	sig := first(headers, whSigHeader)
	ts := first(headers, whTsHeader)
	if sig != "" && ts != "" {
		if h.secret == "" {
			return nil, client.NewPaygateError(client.ErrInvalidSignature, "Webhook signature received but no secret configured")
		}
		if !client.Verify(h.secret, sig, "POST", notifyURL, ts, body) {
			return nil, client.NewPaygateError(client.ErrInvalidSignature, "Webhook signature mismatch")
		}
	}

	var raw map[string]interface{}
	if err := json.Unmarshal([]byte(body), &raw); err != nil {
		return nil, client.NewPaygateError(client.ErrUnknownError, "Failed to parse webhook payload: "+err.Error())
	}
	typ, _ := raw["type"].(string)
	if typ == "" {
		return nil, client.NewPaygateError(client.ErrInvalidParameter, "Webhook payload missing type field")
	}

	switch typ {
	case "sign.success":
		var evt SignSuccess
		json.Unmarshal([]byte(body), &evt)
		return evt, nil
	case "payment.completed":
		var evt PaymentCompleted
		json.Unmarshal([]byte(body), &evt)
		return evt, nil
	case "refund.completed":
		var evt RefundCompleted
		json.Unmarshal([]byte(body), &evt)
		return evt, nil
	default:
		return nil, client.NewPaygateError(client.ErrInvalidParameter, fmt.Sprintf("Unknown webhook event type: %s", typ))
	}
}

func first(headers map[string][]string, key string) string {
	vals, ok := headers[key]
	if !ok || len(vals) == 0 {
		return ""
	}
	return vals[0]
}
