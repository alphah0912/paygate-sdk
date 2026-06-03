package client

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestPayParsesResponse(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		json.NewEncoder(w).Encode(map[string]interface{}{
			"code":             "200",
			"message":          "ok",
			"redirectUrl":      "https://checkout.example.com/abc",
			"paymentRequestId": "REQ001",
		})
	}))
	defer server.Close()

	c := NewWithURL("mk_test_key", "test_secret", server.URL)
	resp, err := c.Pay(PayRequest{
		Amount:             "100.00",
		PaymentMethodCode:  "ALIPAY_CN",
		TerminalType:       "WEB",
		SettlementCurrency: "USD",
	})
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if resp.RedirectURL != "https://checkout.example.com/abc" {
		t.Errorf("expected redirectUrl, got %s", resp.RedirectURL)
	}
}

func TestPayErrorResponse(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		json.NewEncoder(w).Encode(map[string]interface{}{
			"code":    "40001",
			"message": "Invalid API key",
		})
	}))
	defer server.Close()

	c := NewWithURL("mk_test_key", "test_secret", server.URL)
	_, err := c.Pay(PayRequest{
		Amount:             "100.00",
		PaymentMethodCode:  "ALIPAY_CN",
		TerminalType:       "WEB",
		SettlementCurrency: "USD",
	})
	if err == nil {
		t.Fatal("expected error response")
	}
}
