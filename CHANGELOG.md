# Changelog

## 1.5 (2026-07-27)

### Added
- **PIN lock** — new option in Privacy Settings to require a PIN every time the app is opened after going to the background. PIN is stored as a salted SHA-256 hash; the app never persists the raw PIN.
- **In-app PDF viewer** — PDF attachments received in conversations now open inside Molly Light rather than launching an external app.
- **In-app web view** — tapping links and link previews in conversations opens them in an in-app browser instead of the system browser.

### Fixed
- **PIN lock crash on app open** — after setting a PIN, the app crashed immediately on every subsequent launch with `IllegalStateException` (unable to read encrypted preferences before the master secret is loaded). Startup now checks PIN state via a safe method that doesn't require the cipher.

---

## 1.4 and earlier

See git log for changes prior to the versioned changelog.
