# Mybeme Mobile Browser 🌐⚡

Aplikasi mobile browser native Android (APK) berdesain ergonomis ala **Brave Browser** dengan proteksi privasi ketat (*ad & tracker blocker* bawaan) dan tema visual premium **Obsidian Titanium Black & Molten Orange**, terintegrasi dua arah (*two-way browser handoff*) dengan agen kecerdasan buatan **Mybeme** via jaringan privat Tailscale.

---

## ✨ Fitur Utama

- **🛡️ Brave-Style Shields (Anti Iklan & Pelacak):**
  - Mencegat network request iklan, pelacak (Google Analytics, DoubleClick, Facebook pixel, dll).
  - Peningkatan otomatis HTTPS (*HTTPS Everywhere*).
  - Indikator counter pelacak diblokir langsung di bilah bawah.
- **📱 Navigasi Ergonomis Jempol (Bottom Navigation Bar):**
  - Bottom Omnibox dengan auto-search DuckDuckGo.
  - Tab Switcher grid view dengan penanda khusus tab **"Dari Mybeme"**.
  - Tombol Mybeme Co-Pilot terjangkau satu tangan.
- **⚡ Mybeme Co-Pilot Sheet:**
  - **Ringkas Halaman:** Ekstraksi instan artikel web menjadi poin penting dalam hitungan detik.
  - **Oper ke VPS:** Lempar tab aktif dari HP ke riset agen Mybeme di VPS.
  - **Audit Keamanan:** Cek status SSL dan keamanan koneksi situs.
- **🔄 Two-Way Handoff (VPS ⇄ HP):**
  - Agen Mybeme di VPS dapat mengirim tab langsung ke browser HP Pak Basuki kapan saja melalui perintah CLI: `mybeme-browser push <url>`.
  - Terhubung aman peer-to-peer lewat antarmuka privat Tailscale (`100.80.80.80:8765`).
- **🎨 Tema Eksklusif Obsidian Titanium:**
  - Obsidian Titanium Black (`#050507`), Molten Orange (`#FF6A00`), dan Desert Gold (`#FFB347`).
  - Fluid glassmorphism & dynamic status bar.

---

## 📂 Struktur Proyek

```
mybeme-browser/
├── android/                         # Aplikasi Native Android (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── src/main/java/space/mrbasukirahmat/browser/
│   │   │   ├── shield/              # Brave Shields Engine (Ad/Tracker Blocker)
│   │   │   ├── sync/                # Tailscale WebSocket Client
│   │   │   ├── ui/                  # Compose Screens, Omnibox, CoPilot Sheet
│   │   │   ├── webview/             # Android Modern WebView Client
│   │   │   └── data/db/             # SQLite Room DB (Tabs & History)
│   │   └── build.gradle.kts
│   ├── gradlew                      # Gradle Wrapper
│   └── settings.gradle.kts
├── gateway/                         # VPS Companion Service (FastAPI + WebSocket)
│   ├── app.py                       # FastAPI & WebSocket Sync Endpoint
│   ├── cli.py                       # CLI Utilitas `mybeme-browser`
│   ├── config.py                    # Konfigurasi Token & Port Tailscale
│   ├── protocol.py                  # Skema Event Sinkronisasi
│   └── mybeme-browser-gateway.service # Systemd Service Unit
└── .github/workflows/
    └── build-apk.yml                # CI/CD Otomatis Build APK & Rilis
```

---

## 🚀 Cara Memasang APK di HP Android

1. Buka repositori `https://github.com/mrbasuki/mybeme-browser` di HP Android.
2. Masuk ke tab **Actions** (atau **Releases**).
3. Unduh berkas **`mybeme-browser-release.apk`** atau **`mybeme-browser-debug.apk`**.
4. Pasang APK di HP (izinkan *Install unknown apps* jika diminta).
5. Buka aplikasi, dan browser otomatis tersambung ke VPS Mybeme via Tailscale!

---

## 🛠️ Penggunaan CLI di VPS (Mybeme Companion)

Kirim tautan riset dari VPS ke HP Pak Basuki:
```bash
mybeme-browser push "https://github.com/mrbasuki" --note "Pak Basuki, silakan cek repositori ini"
```

Melihat daftar tab aktif di HP & VPS:
```bash
mybeme-browser list-tabs
```

---

Didesain khusus untuk Pak Basuki & didukung oleh Mybeme.
