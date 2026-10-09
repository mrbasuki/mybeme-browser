import uuid
import time
from typing import List, Dict, Any, Optional
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, Depends, HTTPException, Header, Query, status
from fastapi.middleware.cors import CORSMiddleware

from config import GatewayConfig, get_config
from protocol import HandoffPayload, SummarizeRequest, SummarizeResponse, TabItem, WebSocketMessage

class ConnectionManager:
    def __init__(self):
        self.active_connections: List[WebSocket] = []

    async def connect(self, websocket: WebSocket):
        await websocket.accept()
        self.active_connections.append(websocket)

    def disconnect(self, websocket: WebSocket):
        if websocket in self.active_connections:
            self.active_connections.remove(websocket)

    async def broadcast(self, message: dict):
        for connection in self.active_connections:
            try:
                await connection.send_json(message)
            except Exception:
                pass

def create_app(cfg: Optional[GatewayConfig] = None) -> FastAPI:
    config = cfg or get_config()
    app = FastAPI(title="Mybeme Browser Gateway", version="1.0.0")

    app.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    manager = ConnectionManager()
    tabs_store: List[TabItem] = []
    handoff_queue: List[HandoffPayload] = []

    def verify_auth_header(authorization: Optional[str] = Header(None)):
        if not authorization or not authorization.startswith("Bearer "):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing or invalid token")
        token = authorization.split("Bearer ", 1)[1].strip()
        if token != config.auth_token:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
        return token

    @app.get("/health")
    def health_check():
        return {"status": "ok", "service": "mybeme-browser-gateway", "time": time.time()}

    @app.post("/api/v1/handoff")
    async def post_handoff(payload: HandoffPayload, token: str = Depends(verify_auth_header)):
        handoff_queue.append(payload)
        tab = TabItem(
            id=str(uuid.uuid4())[:8],
            url=payload.url,
            title=payload.title or payload.url,
            source="vps"
        )
        tabs_store.append(tab)

        # Broadcast event to connected mobile devices
        await manager.broadcast({
            "event": "OPEN_TAB",
            "source": "mybeme_agent",
            "payload": {
                "url": payload.url,
                "title": payload.title or payload.url,
                "note": payload.note or "Tab dikirim oleh Mybeme",
                "timestamp": payload.timestamp
            }
        })

        return {"status": "queued", "id": tab.id, "url": payload.url}

    @app.get("/api/v1/tabs")
    def list_tabs(token: str = Depends(verify_auth_header)):
        return {"tabs": [tab.model_dump() for tab in tabs_store]}

    @app.post("/api/v1/summarize", response_model=SummarizeResponse)
    def summarize_content(request: SummarizeRequest, token: str = Depends(verify_auth_header)):
        text = request.cleaned_text.strip()
        if not text:
            summary = "Halaman tidak memiliki teks yang cukup untuk diringkas."
            points = []
        else:
            # Clean summarization heuristic
            lines = [line.strip() for line in text.split("\n") if len(line.strip()) > 30]
            preview = lines[:4] if lines else [text[:200]]
            summary = f"Rangkuman untuk '{request.title or request.url}':\n" + "\n".join([f"• {p}" for p in preview])
            points = preview

        return SummarizeResponse(
            url=request.url,
            summary=summary,
            points=points,
            tokens_used=len(text.split())
        )

    @app.websocket("/ws/sync")
    async def websocket_sync_endpoint(websocket: WebSocket, token: Optional[str] = Query(None)):
        if token != config.auth_token:
            await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
            return

        await manager.connect(websocket)
        try:
            while True:
                data = await websocket.receive_json()
                event = data.get("event")
                payload = data.get("payload", {})

                if event == "HANDOFF_TO_AGENT":
                    url = payload.get("url", "")
                    title = payload.get("title", "")
                    cleaned_text = payload.get("cleaned_text", "")
                    action = payload.get("action_requested", "open")

                    tab = TabItem(
                        id=str(uuid.uuid4())[:8],
                        url=url,
                        title=title or url,
                        source="mobile"
                    )
                    tabs_store.append(tab)

                    if action == "summarize" and cleaned_text:
                        summary_res = f"Rangkuman untuk '{title}':\n• {cleaned_text[:300]}..."
                        await websocket.send_json({
                            "event": "SUMMARY_RESULT",
                            "payload": {
                                "url": url,
                                "summary": summary_res,
                                "tokens_used": len(cleaned_text.split())
                            }
                        })
                    else:
                        await websocket.send_json({
                            "event": "ACK",
                            "payload": {
                                "status": "received",
                                "url": url,
                                "tab_id": tab.id
                            }
                        })
                elif event == "PING":
                    await websocket.send_json({"event": "PONG", "timestamp": time.time()})
                else:
                    await websocket.send_json({"event": "ACK", "payload": {"status": "ok"}})
        except WebSocketDisconnect:
            manager.disconnect(websocket)
        except Exception:
            manager.disconnect(websocket)

    return app

app = create_app()
