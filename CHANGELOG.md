<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-plugin-tcptunnelj Changelog

## [Unreleased]

- Multiple selection on the calls list, with a "Copy requests" action that copies the request lines of the selected calls to the clipboard (Ctrl/Cmd+C)
- "Save calls" is now enabled whenever the list is not empty, also while the tunnel is running
- Fixed chunked responses being copied and saved with a stray CRLF every chunk (8 KB with most servers), which corrupted JSON and any other payload that cannot carry raw newlines. The chunked framing is now parsed properly, on the raw bytes, so multi-byte UTF-8 payloads and keep-alive calls carrying several responses come out byte-identical to what the client received
- Compressed responses are decompressed in what gets copied or saved. A client that sends `Accept-Encoding: gzip` — every browser does — used to get a wall of illegible bytes where the body should be; `gzip` and `deflate` (both zlib-wrapped and raw) are now decoded, and a body that cannot be decoded, such as `br`, is left as it came off the wire
- The viewers show the capture exactly as it went over the wire, chunk sizes and all: undoing the framing merged a whole JSON body into a single line of tens of thousands of characters, which Swing draws as a smear of overlapping glyphs. Decoding stays where it is useful — "Save calls" and the new "Copy body (decoded)" / "Copy response (decoded)" entries in the response viewer's context menu, the first of which puts the payload alone on the clipboard, ready to paste into a `.json` file
- Fixed the request and response viewers turning unreadable on very long lines, whatever their origin: a line past 1000 characters is now broken up for display only, and never in what is copied or saved

## [0.0.1] - 2024-12-15

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

[Unreleased]: https://github.com/atasc/intellij-plugin-tcptunnelj/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/atasc/intellij-plugin-tcptunnelj/commits/v0.0.1
