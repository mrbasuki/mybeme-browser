import pytest
from fastapi.testclient import TestClient
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app import create_app
from config import GatewayConfig

def test_unauthorized_access():
    app = create_app(GatewayConfig(auth_token="mybeme-secret-token"))
    client = TestClient(app)
    res = client.get("/api/v1/tabs")
    assert res.status_code == 401

def test_push_handoff_and_list_tabs():
    app = create_app(GatewayConfig(auth_token="mybeme-secret-token"))
    client = TestClient(app)
    headers = {"Authorization": "Bearer mybeme-secret-token"}
    payload = {
        "url": "https://news.ycombinator.com",
        "title": "Hacker News",
        "note": "Riset"
    }
    res = client.post("/api/v1/handoff", json=payload, headers=headers)
    assert res.status_code == 200
    assert res.json()["status"] == "queued"

    res_tabs = client.get("/api/v1/tabs", headers=headers)
    assert res_tabs.status_code == 200
    data = res_tabs.json()
    assert any(tab["url"] == "https://news.ycombinator.com" for tab in data["tabs"])

def test_summarize_endpoint():
    app = create_app(GatewayConfig(auth_token="mybeme-secret-token"))
    client = TestClient(app)
    headers = {"Authorization": "Bearer mybeme-secret-token"}
    payload = {
        "url": "https://example.com/article",
        "title": "Example Article",
        "cleaned_text": "Hermes Agent adalah agen otonom sumber terbuka. Mendukung browser use dan alat sistem."
    }
    res = client.post("/api/v1/summarize", json=payload, headers=headers)
    assert res.status_code == 200
    assert "summary" in res.json()
    assert len(res.json()["summary"]) > 0

def test_websocket_unauthorized():
    app = create_app(GatewayConfig(auth_token="mybeme-secret-token"))
    client = TestClient(app)
    with pytest.raises(Exception):
        with client.websocket_connect("/ws/sync?token=wrong-token"):
            pass

def test_websocket_sync_flow():
    app = create_app(GatewayConfig(auth_token="mybeme-secret-token"))
    client = TestClient(app)
    with client.websocket_connect("/ws/sync?token=mybeme-secret-token") as websocket:
        websocket.send_json({
            "event": "HANDOFF_TO_AGENT",
            "client_id": "android_mybeme",
            "payload": {
                "url": "https://example.org",
                "title": "Example",
                "cleaned_text": "Sample text",
                "action_requested": "summarize",
                "timestamp": 1728456000
            }
        })
        response = websocket.receive_json()
        assert response["event"] in ["SUMMARY_RESULT", "ACK"]
