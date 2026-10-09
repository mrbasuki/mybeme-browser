#!/usr/bin/env python3
import sys
import os
import argparse
import httpx
from typing import List, Dict, Any, Optional

try:
    from config import get_config
except ImportError:
    from gateway.config import get_config

def get_base_url() -> str:
    config = get_config()
    # Default local check: if running on VPS, can talk to 127.0.0.1 or 100.80.80.80
    host = os.environ.get("MYBEME_BROWSER_HOST", "127.0.0.1")
    port = os.environ.get("MYBEME_BROWSER_PORT", str(config.port))
    return f"http://{host}:{port}"

def execute_push(url: str, note: Optional[str] = None, title: Optional[str] = None) -> bool:
    config = get_config()
    base_url = get_base_url()
    headers = {"Authorization": f"Bearer {config.auth_token}"}
    payload = {
        "url": url,
        "note": note,
        "title": title or url,
        "action_requested": "open"
    }
    try:
        res = httpx.post(f"{base_url}/api/v1/handoff", json=payload, headers=headers, timeout=10.0)
        if res.status_code == 200:
            return True
        print(f"Error ({res.status_code}): {res.text}", file=sys.stderr)
        return False
    except Exception as e:
        print(f"Connection failed to {base_url}: {e}", file=sys.stderr)
        return False

def execute_list_tabs() -> List[Dict[str, Any]]:
    config = get_config()
    base_url = get_base_url()
    headers = {"Authorization": f"Bearer {config.auth_token}"}
    try:
        res = httpx.get(f"{base_url}/api/v1/tabs", headers=headers, timeout=10.0)
        if res.status_code == 200:
            return res.json().get("tabs", [])
        print(f"Error ({res.status_code}): {res.text}", file=sys.stderr)
        return []
    except Exception as e:
        print(f"Connection failed to {base_url}: {e}", file=sys.stderr)
        return []

def execute_summarize(url: str, title: str, text: str) -> str:
    config = get_config()
    base_url = get_base_url()
    headers = {"Authorization": f"Bearer {config.auth_token}"}
    payload = {
        "url": url,
        "title": title,
        "cleaned_text": text
    }
    try:
        res = httpx.post(f"{base_url}/api/v1/summarize", json=payload, headers=headers, timeout=15.0)
        if res.status_code == 200:
            return res.json().get("summary", "")
        return f"Gagal merangkum ({res.status_code})"
    except Exception as e:
        return f"Error koneksi: {e}"

def main():
    parser = argparse.ArgumentParser(description="Mybeme Browser Gateway CLI - Oper tab & kontrol sinkronisasi")
    subparsers = parser.add_subparsers(dest="command")

    push_parser = subparsers.add_parser("push", help="Kirim URL/tab ke browser HP Pak Basuki")
    push_parser.add_argument("url", help="URL yang akan dibuka di HP")
    push_parser.add_argument("--note", "-n", help="Catatan pesan untuk Pak Basuki")
    push_parser.add_argument("--title", "-t", help="Judul halaman opsional")

    list_parser = subparsers.add_parser("list-tabs", help="Lihat daftar tab aktif di HP & VPS")

    sum_parser = subparsers.add_parser("summarize", help="Minta rangkuman untuk teks web")
    sum_parser.add_argument("url", help="URL halaman")
    sum_parser.add_argument("--title", "-t", default="", help="Judul halaman")
    sum_parser.add_argument("--text", "-x", required=True, help="Teks artikel yang akan dirangkum")

    args = parser.parse_args()

    if args.command == "push":
        success = execute_push(args.url, note=args.note, title=args.title)
        if success:
            print(f"✓ Tab berhasil dikirim ke HP Pak Basuki: {args.url}")
        else:
            sys.exit(1)
    elif args.command == "list-tabs":
        tabs = execute_list_tabs()
        print(f"Total Tab ({len(tabs)}):")
        for tab in tabs:
            source = "[HP]" if tab.get("source") == "mobile" else "[VPS]"
            print(f" - {source} {tab.get('title')} ({tab.get('url')})")
    elif args.command == "summarize":
        summary = execute_summarize(args.url, args.title, args.text)
        print(summary)
    else:
        parser.print_help()

if __name__ == "__main__":
    main()
