package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"time"

	"crypto/rand"
	"encoding/hex"
)

const maxRetries = 3

// Client is the main entry point for the PayGate SDK.
type Client struct {
	apiKey    string
	apiSecret string
	baseURL   string
	basePath  string
	http      *http.Client
}

// New creates a new PaygateClient.
func New(apiKey, apiSecret string, env Environment) *Client {
	return &Client{
		apiKey:    apiKey,
		apiSecret: apiSecret,
		baseURL:   env.BaseURL(),
		basePath:  env.BasePath(),
		http:      &http.Client{Timeout: 30 * time.Second},
	}
}

// NewWithURL creates a client with a custom base URL (for testing).
func NewWithURL(apiKey, apiSecret, baseURL string) *Client {
	return &Client{
		apiKey:    apiKey,
		apiSecret: apiSecret,
		baseURL:   baseURL,
		basePath:  "",
		http:      &http.Client{Timeout: 30 * time.Second},
	}
}

func genRequestID() string {
	b := make([]byte, 4)
	rand.Read(b)
	return hex.EncodeToString(b)
}

func isRetryable(statusCode int) bool {
	return statusCode == 429 || statusCode == 502 || statusCode == 503 || statusCode == 504
}

// Pay initiates a payment.
func (c *Client) Pay(req PayRequest) (*PayResponse, error) {
	resp := &PayResponse{}
	err := c.executeWithRetry("/pay", req, resp, 0)
	return resp, err
}

// Capture captures an authorized payment.
func (c *Client) Capture(req CaptureRequest) (*CaptureResponse, error) {
	resp := &CaptureResponse{}
	err := c.executeWithRetry("/capture", req, resp, 0)
	return resp, err
}

// Cancel cancels a payment.
func (c *Client) Cancel(req CancelRequest) (*CancelResponse, error) {
	if req.Reason == "" {
		req.Reason = "MERCHANT_MANUAL"
	}
	resp := &CancelResponse{}
	err := c.executeWithRetry("/cancel", req, resp, 0)
	return resp, err
}

// InquiryPayment queries payment status.
func (c *Client) InquiryPayment(req InquiryPaymentRequest) (*InquiryPaymentResponse, error) {
	resp := &InquiryPaymentResponse{}
	err := c.executeWithRetry("/inquiry-payment", req, resp, 0)
	return resp, err
}

// InquiryRefund queries refund status.
func (c *Client) InquiryRefund(req InquiryRefundRequest) (*InquiryRefundResponse, error) {
	resp := &InquiryRefundResponse{}
	err := c.executeWithRetry("/inquiry-refund", req, resp, 0)
	return resp, err
}

// Refund initiates a refund.
func (c *Client) Refund(req RefundRequest) (*RefundResponse, error) {
	if req.Reason == "" {
		req.Reason = "ISV退款"
	}
	resp := &RefundResponse{}
	err := c.executeWithRetry("/refund", req, resp, 0)
	return resp, err
}

func (c *Client) executeWithRetry(path string, req interface{}, resp interface{}, attempt int) error {
	body, _ := json.Marshal(req)
	timestamp := strconv.FormatInt(time.Now().UnixMilli(), 10)
	signingPath := c.basePath + path
	sig := Sign(c.apiSecret, "POST", signingPath, timestamp, string(body))
	requestID := genRequestID()

	httpReq, _ := http.NewRequest("POST", c.baseURL+path, bytes.NewReader(body))
	httpReq.Header.Set("Content-Type", "application/json")
	httpReq.Header.Set("X-Api-Key", c.apiKey)
	httpReq.Header.Set("X-Signature", sig)
	httpReq.Header.Set("X-Timestamp", timestamp)
	httpReq.Header.Set("X-Request-Id", requestID)

	httpResp, err := c.http.Do(httpReq)
	if err != nil {
		return NewPaygateError(ErrNetworkError, "Network request failed: "+err.Error())
	}
	defer httpResp.Body.Close()

	respBytes, _ := io.ReadAll(httpResp.Body)

	if isRetryable(httpResp.StatusCode) && attempt < maxRetries {
		time.Sleep(time.Duration(1<<uint(attempt+1)) * time.Second)
		return c.executeWithRetry(path, req, resp, attempt+1)
	}

	if httpResp.StatusCode >= 500 {
		return &PaygateError{Code: ErrServerError, Message: fmt.Sprintf("Server returned %d: %s", httpResp.StatusCode, string(respBytes)), HTTPStatus: httpResp.StatusCode}
	}

	if err := json.Unmarshal(respBytes, resp); err != nil {
		return NewPaygateError(ErrUnknownError, "Failed to parse response: "+string(respBytes))
	}

	var base apiResponse
	json.Unmarshal(respBytes, &base)
	if !base.IsSuccess() {
		return NewPaygateError(ErrorCode(base.Code), base.Message)
	}

	return nil
}
