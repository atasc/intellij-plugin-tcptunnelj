package io.atasc.intellij.tcptunnelj.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link Call#removeChunkedEncoding(byte[])} used to strip only the chunk-size lines and leave the
 * CRLF that terminates each chunk behind, so a captured JSON response came out with a stray CRLF
 * every 8192 bytes — often in the middle of a token, which no parser accepts.
 */
public class CallChunkedTest {
  private static final String CHUNKED_HEADER =
      "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nTransfer-Encoding: chunked";

  @Test
  public void decodesChunkBoundariesWithoutInjectingNewlines() throws IOException {
    StringBuilder json = new StringBuilder("{\"items\":[");
    for (int i = 0; i < 1500; i++) {
      json.append("{\"nome\":\"Frittàta però àèìòù ").append(i).append("\",\"leaf\":false},");
    }
    json.setLength(json.length() - 1);
    json.append("]}");

    byte[] body = json.toString().getBytes(StandardCharsets.UTF_8);
    String decoded = Call.removeChunkedEncoding(chunk(CHUNKED_HEADER, body, 8192));

    // Chunk sizes count bytes: with accented characters in the payload the boundaries only land
    // where they should if the decoding happens before the UTF-8 conversion.
    assertTrue("payload should contain multi-byte characters",
        body.length > json.length());
    assertEquals(json.toString(), bodyOf(decoded));
  }

  @Test
  public void decodedBodyIsByteIdenticalToTheOriginal() throws IOException {
    byte[] body = "{\"a\":\"àèìòù\",\"b\":[1,2,3]}".getBytes(StandardCharsets.UTF_8);
    String decoded = Call.removeChunkedEncoding(chunk(CHUNKED_HEADER, body, 7));

    assertArrayEquals(body, bodyOf(decoded).getBytes(StandardCharsets.UTF_8));
  }

  @Test
  public void keepsPayloadLinesThatLookLikeChunkSizes() throws IOException {
    byte[] body = "line1\r\ndeadbeef\r\nline3".getBytes(StandardCharsets.UTF_8);
    String decoded = Call.removeChunkedEncoding(chunk(CHUNKED_HEADER, body, 1024));

    assertEquals("line1\r\ndeadbeef\r\nline3", bodyOf(decoded));
  }

  @Test
  public void handlesChunkExtensionsTrailersAndLowercaseHeader() {
    String capture = "HTTP/1.1 200 OK\r\ntransfer-encoding: gzip, chunked\r\n\r\n"
        + "5;name=value\r\nhello\r\n0\r\nX-Checksum: abc\r\n\r\n";

    assertEquals("hello", bodyOf(Call.removeChunkedEncoding(bytes(capture))));
  }

  @Test
  public void decodesEveryResponseOfAKeepAliveCall() throws IOException {
    ByteArrayOutputStream capture = new ByteArrayOutputStream();
    capture.write(chunk(CHUNKED_HEADER, bytes("{\"a\":1}"), 3));
    capture.write(chunk(CHUNKED_HEADER, bytes("{\"b\":2}"), 4));

    String decoded = Call.removeChunkedEncoding(capture.toByteArray());

    assertTrue(decoded, decoded.contains("\r\n\r\n{\"a\":1}HTTP/1.1 200 OK"));
    assertTrue(decoded, decoded.endsWith("\r\n\r\n{\"b\":2}"));
  }

  @Test
  public void leavesNonChunkedResponsesAlone() {
    String capture = "HTTP/1.1 200 OK\r\nContent-Length: 7\r\n\r\n{\"a\":1}";

    assertEquals(capture, Call.removeChunkedEncoding(bytes(capture)));
  }

  @Test
  public void keepsWhatItHasWhenTheCaptureIsTruncated() {
    // The chunk announces 0x64 bytes but the connection died after five.
    String capture = "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\n64\r\nhello";

    assertEquals("64\r\nhello", bodyOf(Call.removeChunkedEncoding(bytes(capture))));
  }

  @Test
  public void handlesEmptyAndNullCaptures() {
    assertEquals("", Call.removeChunkedEncoding(null));
    assertEquals("", Call.removeChunkedEncoding(new byte[0]));
  }

  @Test
  public void passesThroughTrafficWithoutHeaders() {
    assertEquals("not http at all", Call.removeChunkedEncoding(bytes("not http at all")));
  }

  private static byte[] chunk(String header, byte[] body, int chunkSize) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(bytes(header + "\r\n\r\n"));
    for (int i = 0; i < body.length; i += chunkSize) {
      int size = Math.min(chunkSize, body.length - i);
      out.write(bytes(Integer.toHexString(size) + "\r\n"));
      out.write(body, i, size);
      out.write(bytes("\r\n"));
    }
    out.write(bytes("0\r\n\r\n"));
    return out.toByteArray();
  }

  private static String bodyOf(String response) {
    int headerEnd = response.indexOf("\r\n\r\n");
    return headerEnd == -1 ? response : response.substring(headerEnd + 4);
  }

  private static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }
}
