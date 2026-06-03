"""
Target environment for the PayGate API.

@author alphah
@since 1.0.0
"""

from enum import Enum


class Environment(str, Enum):
    """Determines the base URL used for all requests."""

    # Sandbox testing environment.
    SANDBOX = "https://sandbox.antom.com"

    # Live production environment.
    LIVE = "https://api.antom.com"

    @property
    def base_path(self) -> str:
        """API path prefix used for HMAC signing."""
        if self == Environment.SANDBOX:
            return "/api/gateway/v1"
        return "/gateway/v1"

    @property
    def base_url(self) -> str:
        """Full API base URL."""
        return self.value + self.base_path
