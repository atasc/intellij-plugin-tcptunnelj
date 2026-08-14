package io.atasc.intellij.tcptunnelj.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A client that sends {@code Accept-Encoding: gzip} — every browser does — gets a compressed body
 * back, which the viewer used to render as a wall of illegible bytes. {@link
 * Call#decodeResponse(byte[])} undoes the content coding as well as the chunked framing; {@link
 * Call#removeChunkedEncoding(byte[])} still leaves the payload as it was on the wire.
 */
public class CallCompressedTest {
  private static final String JSON = "{\"nome\":\"Frittàta però àèìòù\",\"leaf\":false}";

  @Test
  public void decodesGzipInsideChunkedFraming() throws IOException {
    byte[] capture = capture("Transfer-Encoding: chunked\r\nContent-Encoding: gzip",
        chunked(gzip(bytes(JSON)), 16));

    assertEquals(JSON, bodyOf(Call.decodeResponse(capture)));
  }

  @Test
  public void decodesGzipDelimitedByContentLength() throws IOException {
    byte[] body = gzip(bytes(JSON));
    byte[] capture = capture("Content-Encoding: gzip\r\nContent-Length: " + body.length, body);

    assertEquals(JSON, bodyOf(Call.decodeResponse(capture)));
  }

  @Test
  public void decodesGzipWithoutContentLength() throws IOException {
    byte[] capture = capture("Content-Encoding: GZIP", gzip(bytes(JSON)));

    assertEquals(JSON, bodyOf(Call.decodeResponse(capture)));
  }

  @Test
  public void decodesZlibWrappedDeflate() throws IOException {
    byte[] capture = capture("Content-Encoding: deflate", deflate(bytes(JSON), false));

    assertEquals(JSON, bodyOf(Call.decodeResponse(capture)));
  }

  @Test
  public void decodesRawDeflate() throws IOException {
    // Not what the RFC asks for, but what a fair number of servers actually send.
    byte[] capture = capture("Content-Encoding: deflate", deflate(bytes(JSON), true));

    assertEquals(JSON, bodyOf(Call.decodeResponse(capture)));
  }

  @Test
  public void decodesEveryResponseOfAKeepAliveCall() throws IOException {
    byte[] first = gzip(bytes("{\"a\":1}"));
    byte[] second = gzip(bytes("{\"b\":2}"));

    ByteArrayOutputStream capture = new ByteArrayOutputStream();
    capture.write(capture("Content-Encoding: gzip\r\nContent-Length: " + first.length, first));
    capture.write(capture("Content-Encoding: gzip\r\nContent-Length: " + second.length, second));

    String decoded = Call.decodeResponse(capture.toByteArray());

    assertTrue(decoded, decoded.contains("\r\n\r\n{\"a\":1}HTTP/1.1 200 OK"));
    assertTrue(decoded, decoded.endsWith("\r\n\r\n{\"b\":2}"));
  }

  @Test
  public void keepsWhatInflatedBeforeATruncatedStreamEnded() throws IOException {
    StringBuilder json = new StringBuilder("{\"items\":[");
    for (int i = 0; i < 2000; i++) {
      json.append("{\"nome\":\"Frittàta ").append(i).append("\"},");
    }
    json.setLength(json.length() - 1);
    json.append("]}");

    byte[] compressed = gzip(bytes(json.toString()));
    byte[] cut = Arrays.copyOf(compressed, compressed.length / 2);
    String decoded = bodyOf(Call.decodeResponse(capture("Content-Encoding: gzip", cut)));

    assertTrue(decoded, decoded.startsWith("{\"items\":[{\"nome\":\"Frittàta 0\"}"));
    assertFalse("the tail was never captured", decoded.endsWith("]}"));
  }

  @Test
  public void leavesCodingsTheJdkCannotUndoUntouched() throws IOException {
    byte[] body = bytes("not really brotli");
    byte[] capture = capture("Content-Encoding: br", body);

    assertEquals(new String(capture, StandardCharsets.UTF_8), Call.decodeResponse(capture));
  }

  @Test
  public void leavesAPayloadThatWillNotInflateUntouched() throws IOException {
    // Header announces gzip but the bytes are not: better to show them than to swallow them.
    byte[] capture = capture("Content-Encoding: gzip", bytes(JSON));

    assertEquals(JSON, bodyOf(Call.decodeResponse(capture)));
  }

  @Test
  public void leavesUncompressedResponsesAlone() throws IOException {
    byte[] capture = capture("Content-Type: application/json", bytes(JSON));

    assertEquals(new String(capture, StandardCharsets.UTF_8), Call.decodeResponse(capture));
  }

  @Test
  public void removeChunkedEncodingStillLeavesThePayloadCompressed() throws IOException {
    byte[] compressed = gzip(bytes(JSON));
    byte[] capture = capture("Transfer-Encoding: chunked\r\nContent-Encoding: gzip",
        chunked(compressed, 16));

    String unframed = Call.removeChunkedEncoding(capture);

    // Framing gone, payload still compressed — which is what the "raw" view is for.
    assertFalse(unframed, unframed.contains("\"leaf\""));
    assertFalse(unframed, bodyOf(unframed).startsWith(Integer.toHexString(16)));
  }

  private static byte[] capture(String headers, byte[] body) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(bytes("HTTP/1.1 200 OK\r\n" + headers + "\r\n\r\n"));
    out.write(body);
    return out.toByteArray();
  }

  private static byte[] chunked(byte[] body, int chunkSize) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    for (int i = 0; i < body.length; i += chunkSize) {
      int size = Math.min(chunkSize, body.length - i);
      out.write(bytes(Integer.toHexString(size) + "\r\n"));
      out.write(body, i, size);
      out.write(bytes("\r\n"));
    }
    out.write(bytes("0\r\n\r\n"));
    return out.toByteArray();
  }

  private static byte[] gzip(byte[] body) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
      gzip.write(body);
    }
    return out.toByteArray();
  }

  private static byte[] deflate(byte[] body, boolean raw) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (DeflaterOutputStream deflater =
             new DeflaterOutputStream(out, new Deflater(Deflater.DEFAULT_COMPRESSION, raw))) {
      deflater.write(body);
    }
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
