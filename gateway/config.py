import os
from pydantic import BaseModel, Field

class GatewayConfig(BaseModel):
    host: str = Field(default="100.80.80.80", description="IP Tailscale VPS")
    port: int = Field(default=8765, description="Port gateway")
    auth_token: str = Field(default="mybeme-browser-key-991823", description="Secret bearer token")
    db_path: str = Field(default="/srv/vault/browser-sync.db", description="Database file path")

def get_config() -> GatewayConfig:
    token = os.environ.get("MYBEME_BROWSER_TOKEN", "mybeme-browser-key-991823")
    host = os.environ.get("MYBEME_BROWSER_HOST", "100.80.80.80")
    port = int(os.environ.get("MYBEME_BROWSER_PORT", "8765"))
    db_path = os.environ.get("MYBEME_BROWSER_DB", "/srv/vault/browser-sync.db")
    return GatewayConfig(
        host=host,
        port=port,
        auth_token=token,
        db_path=db_path
    )
