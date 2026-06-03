package client

// Environment is the target environment for the PayGate API.
type Environment struct {
	baseURL  string
	basePath string
}

var (
	// Sandbox testing environment.
	SANDBOX = Environment{"https://backend.hunanxiaojunzi.com", "/api/sandbox/gateway/v1"}
	// Live production environment.
	LIVE = Environment{"https://backend.hunanxiaojunzi.com", "/api/gateway/v1"}
)

// BaseURL returns the full API base URL.
func (e Environment) BaseURL() string { return e.baseURL + e.basePath }

// BasePath returns the path prefix for HMAC signing.
func (e Environment) BasePath() string { return e.basePath }
