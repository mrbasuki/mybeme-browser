import pytest
import sys
import os
from unittest.mock import patch, MagicMock

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from cli import execute_push, execute_list_tabs, execute_summarize

def test_cli_push_command():
    with patch("httpx.post") as mock_post:
        mock_resp = MagicMock()
        mock_resp.status_code = 200
        mock_resp.json.return_value = {"status": "queued", "id": "12345"}
        mock_post.return_value = mock_resp

        result = execute_push("https://github.com/mrbasuki", "Test Note")
        assert result is True
        mock_post.assert_called_once()
        args, kwargs = mock_post.call_args
        assert kwargs["json"]["url"] == "https://github.com/mrbasuki"
        assert kwargs["json"]["note"] == "Test Note"

def test_cli_list_tabs_command():
    with patch("httpx.get") as mock_get:
        mock_resp = MagicMock()
        mock_resp.status_code = 200
        mock_resp.json.return_value = {
            "tabs": [
                {"id": "1", "url": "https://news.ycombinator.com", "title": "HN", "source": "mobile"}
            ]
        }
        mock_get.return_value = mock_resp

        tabs = execute_list_tabs()
        assert len(tabs) == 1
        assert tabs[0]["title"] == "HN"

def test_cli_summarize_command():
    with patch("httpx.post") as mock_post:
        mock_resp = MagicMock()
        mock_resp.status_code = 200
        mock_resp.json.return_value = {
            "url": "https://test.com",
            "summary": "Rangkuman artikel testing",
            "tokens_used": 42
        }
        mock_post.return_value = mock_resp

        summary = execute_summarize("https://test.com", "Title", "Text body")
        assert "Rangkuman artikel testing" in summary
