<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# intellij-plugin-tcptunnelj Changelog

## [Unreleased]

- Multiple selection on the calls list, with a "Copy requests" action that copies the request lines of the selected calls to the clipboard (Ctrl/Cmd+C)
- "Save calls" is now enabled whenever the list is not empty, also while the tunnel is running
- Fixed chunked responses being copied and saved with a stray CRLF every chunk (8 KB with most servers), which corrupted JSON and any other payload that cannot carry raw newlines. The chunked framing is now parsed properly, on the raw bytes, so multi-byte UTF-8 payloads and keep-alive calls carrying several responses come out byte-identical to what the client received
- Compressed responses are decompressed in what gets copied or saved. A client that sends `Accept-Encoding: gzip` — every browser does — used to get a wall of illegible bytes where the body should be; `gzip` and `deflate` (both zlib-wrapped and raw) are now decoded, and a body that cannot be decoded, such as `br`, is left as it came off the wire
- The response viewer keeps showing the body with its chunked framing undone and its payload decompressed, so the chunk sizes no longer sit interleaved in the text. Its context menu gained "Copy body (decoded)", which puts the payload alone on the clipboard ready to paste into a `.json` file, "Copy response (decoded)", and "Copy response (raw)" for when the wire framing is the point
- The request and response viewers are now platform editors instead of text areas, which fixes a JSON body rendering as an unreadable compressed strip: Swing lays a line out in a single run, so with the wrap off the glyph positions of a 50 KB line collapse, while the editor only paints the part of the line that is on screen. Soft wrap is on by default and "Wrap lines" toggles it, and either way the text is never altered to make it drawable — so a selection copied out of a viewer pastes as valid JSON. Breaking long lines up for display was tried first and reverted: "Copy" copies what is on screen, so the breaks reached the clipboard and cut the JSON mid-token
- Ctrl+F, Ctrl+G and the rest of the editor's own search and navigation now work inside the viewers
- Closing the tool window or the project now disposes the plugin: the tunnel is closed, the calls list stops its refresh timer, the viewers' editors are released and the settings are persisted. None of that used to happen, because nothing ever disposed it

## [0.0.1] - 2024-12-15

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

[Unreleased]: https://github.com/atasc/intellij-plugin-tcptunnelj/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/atasc/intellij-plugin-tcptunnelj/commits/v0.0.1
