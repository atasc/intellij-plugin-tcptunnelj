package io.atasc.intellij.tcptunnelj.net;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * {@link Call#decodeResponseBody(byte[])} is what the "Copy body (decoded)" action puts on the
 * clipboard: the payload alone, ready to be pasted into a {@code .json} file, with the headers, the
 * chunked framing and the content coding all gone.
 */
public class CallResponseBodyTest {
  private static final String JSON = "{\"items\":[{\"id\":\"a1\",\"name\":\"Frittàta\"}]}";

  @Test
  public void dropsTheHeadersOfAPlainResponse() {
    byte[] capture = bytes("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n"
        + "Content-Length: " + bytes(JSON).length + "\r\n\r\n" + JSON);

    assertEquals(JSON, Call.decodeResponseBody(capture));
  }

  @Test
  public void unframesAChunkedBody() throws IOException {
    byte[] capture = chunked("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n"
        + "Transfer-Encoding: chunked", bytes(JSON), 7);

    assertEquals(JSON, Call.decodeResponseBody(capture));
  }

  @Test
  public void decompressesAChunkedGzipBody() throws IOException {
    byte[] capture = chunked("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n"
        + "Content-Encoding: gzip\r\nTransfer-Encoding: chunked", gzip(bytes(JSON)), 16);

    assertEquals(JSON, Call.decodeResponseBody(capture));
  }

  @Test
  public void putsOneBodyPerLineWhenTheCallIsKeptAlive() {
    String first = "{\"n\":1}";
    String second = "{\"n\":2}";
    byte[] capture = bytes(response(first) + response(second));

    assertEquals(first + "\n" + second, Call.decodeResponseBody(capture));
  }

  /**
   * The capture this was written for: a 50 KB JSON body chunked in 8 KB pieces. It has to come back
   * as one single line — for a while the viewers broke long lines up to keep Swing from smearing the
   * glyphs, that ended up in the clipboard through "Copy", and a break landing inside a token
   * ({@code nul\nl}) left a body that would not parse.
   */
  @Test
  public void keepsALargeBodyOnASingleLine() throws IOException {
    StringBuilder json = new StringBuilder("[");
    while (json.length() < 50_000) {
      json.append("{\"key\":\"nkVLTOWbroU_CsUpv6pXP\",\"label\":\"Farine\",\"leaf\":null},");
    }
    json.setLength(json.length() - 1);
    json.append(']');

    byte[] capture = chunked("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n"
        + "Transfer-Encoding: chunked", bytes(json.toString()), 8192);
    String body = Call.decodeResponseBody(capture);

    assertEquals(json.toString(), body);
    assertEquals("no line break may survive anywhere in the body", -1, body.indexOf('\n'));
  }

  @Test
  public void handlesEmptyAndNullCaptures() {
    assertEquals("", Call.decodeResponseBody(null));
    assertEquals("", Call.decodeResponseBody(new byte[0]));
  }

  @Test
  public void passesThroughTrafficWithoutHeaders() {
    assertEquals("not http at all", Call.decodeResponseBody(bytes("not http at all")));
  }

  private static String response(String body) {
    return "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: "
        + bytes(body).length + "\r\n\r\n" + body;
  }

  private static byte[] chunked(String header, byte[] body, int chunkSize) throws IOException {
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

  private static byte[] gzip(byte[] body) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
      gzip.write(body);
    }
    return out.toByteArray();
  }

  private static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }
}
