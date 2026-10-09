import time
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field

class HandoffPayload(BaseModel):
    url: str
    title: Optional[str] = None
    note: Optional[str] = None
    cleaned_text: Optional[str] = None
    action_requested: Optional[str] = "open"
    timestamp: float = Field(default_factory=time.time)

class SummarizeRequest(BaseModel):
    url: str
    title: Optional[str] = None
    cleaned_text: str

class SummarizeResponse(BaseModel):
    url: str
    summary: str
    points: List[str] = []
    tokens_used: int = 0

class TabItem(BaseModel):
    id: str
    url: str
    title: str
    source: str = "mobile" # "mobile" or "vps"
    timestamp: float = Field(default_factory=time.time)

class WebSocketMessage(BaseModel):
    event: str
    client_id: Optional[str] = None
    payload: Dict[str, Any] = {}
