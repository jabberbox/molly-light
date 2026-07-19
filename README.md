<div align="center">
<img src="docs/screenshots/app-icon.png" width="96" height="96" alt="Molly Light icon">

# Molly Light

**A minimal, distraction-free reskin of Molly, inspired by the [Light Phone III](https://www.thelightphone.com/).**

</div>

> **This is an unofficial personal fork of [Molly](https://github.com/mollyim/mollyim-android)**,
> which is itself a hardened fork of [Signal](https://github.com/signalapp/Signal-Android).
> It is not affiliated with, endorsed by, or sponsored by the Molly project,
> Signal Messenger, LLC, or the Signal Foundation. It connects to Signal's
> servers the same way Molly and Signal do, so it works with your existing
> Signal contacts and account. For the official, actively-maintained app, see
> the upstream [mollyim/mollyim-android](https://github.com/mollyim/mollyim-android)
> repo.

## Why

Signal (and Molly) are great, but the default UI is a lot of screen: a
persistent text box, a row of icons, everything always visible and reachable.
The Light Phone philosophy is the opposite — show only what you need, when
you need it, and get out of the way otherwise.

Molly Light rebuilds the conversation screen around that idea. There's no
always-on text box. Composing a message is a deliberate, full-screen action,
not a passive row sitting at the bottom of every chat.

## Screenshots

<table>
<tr>
<td align="center" width="33%">
<img src="docs/screenshots/welcome.png" width="220" alt="Welcome screen"><br>
<sub>Welcome screen</sub>
</td>
<td align="center" width="33%">
<img src="docs/screenshots/chatlist.png" width="220" alt="Chat list"><br>
<sub>Chat list</sub>
</td>
<td align="center" width="33%">
<img src="docs/screenshots/conversation.png" width="220" alt="Conversation view"><br>
<sub>Resting state: phone / plus / pencil</sub>
</td>
</tr>
<tr>
<td align="center" width="33%">
<img src="docs/screenshots/attach-sheet.png" width="220" alt="Attachment sheet"><br>
<sub>Plus: camera / gallery / audio</sub>
</td>
<td align="center" width="33%">
<img src="docs/screenshots/compose.png" width="220" alt="Compose mode"><br>
<sub>Pencil: full-screen compose</sub>
</td>
<td align="center" width="33%"></td>
</tr>
</table>

## What's different from Molly

- **No persistent text box.** A conversation at rest shows three icons —
  call, add attachment, compose — and nothing else.
- **Full-screen compose.** Tapping the pencil replaces the conversation with
  a dedicated compose view: back arrow, contact name, send arrow, and a bare
  cursor. No visible input box.
- **A simpler attachment picker.** Tapping the plus icon opens a half-screen
  sheet with three options — camera, gallery, audio — instead of Signal's
  full attachment keyboard.
- **Reworked launcher icon and splash screen**, a simplified Help page, and a
  handful of Molly's own settings (in-app update checker, donation link)
  either removed or left as-is where they didn't need to change. See
  [LEGAL.md](LEGAL.md) for the full disclosure.
- Everything else — encryption, registration, backups, linked devices,
  Molly's hardening features — works exactly as it does in upstream Molly.

## Download

Grab the latest APK from this repo's [Releases](https://github.com/jabberbox/molly-light/releases) page.

This is a personal build, signed with a key that only I control — it is
**not** related to Molly's or Signal's official signing keys, and won't
update in place over an existing Molly or Signal install.

## Building from source

See [BUILDING.md](BUILDING.md) for build instructions. Molly Light is built
from the same source tree as upstream Molly, with the reskin layered on top.

## Compatibility with Signal

Molly Light connects to Signal's servers, so it works with your existing
Signal account and contacts. If you want to keep using Signal or Molly on the
same device with the same phone number, register Molly Light as a **linked
device** instead of a primary device — otherwise, whichever app you
registered most recently stays active and the other goes offline.

## Backups

Backups are fully compatible with Molly and Signal. You can restore a Molly
or Signal backup in Molly Light, and the other way around, by choosing the
backup folder or file during setup.

## License

Licensed under the GNU Affero General Public License, version 3 only
([`AGPL-3.0-only`](LICENSE)).

See [LEGAL.md](LEGAL.md) for legal, copyright, and trademark information.

## Acknowledgements

Molly Light is a personal reskin built entirely on the work of the
[Molly](https://github.com/mollyim/mollyim-android) and
[Signal](https://github.com/signalapp/Signal-Android) projects. All of the
underlying messaging, encryption, and hardening work is theirs — deep thanks
to both sets of contributors.
