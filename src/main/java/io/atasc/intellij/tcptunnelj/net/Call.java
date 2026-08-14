package io.atasc.intellij.tcptunnelj.net;

import io.atasc.intellij.tcptunnelj.ui.CallStringFormatter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author boruvka/atasc
 * @since
 */
public class Call {
  public static final int CMD_LENGTH = 80;

  /**
   * Matches an HTTP request line, e.g. "GET /api/v1/food/search?origin=100 HTTP/1.1".
   * A single call may carry more than one when the connection is kept alive.
   */
  private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile(
      "(?m)^(?:GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT) +\\S+ +HTTP/\\d(?:\\.\\d)?$");

  /**
   * Matches a {@code Transfer-Encoding} header whose value lists {@code chunked}. Header names are
   * case-insensitive and the value may carry several codings, e.g. {@code gzip, chunked}.
   */
  private static final Pattern CHUNKED_HEADER_PATTERN = Pattern.compile(
      "(?im)^Transfer-Encoding:[^\\r\\n]*\\bchunked\\b");

  private static final byte[] CRLF = {'\r', '\n'};
  private static final byte[] CRLF_CRLF = {'\r', '\n', '\r', '\n'};

  private long start;
  private long end = -1;
  private String srcHost;
  private String destHost;
  private int srcPort;
  private int destPort;
  private final LogStream input;
  private final LogStream output;

  public Call(String srcHost, int srcPort, String destHost, int destPort) {
    this.start = System.currentTimeMillis();
    this.srcHost = srcHost;
    this.srcPort = srcPort;
    this.destHost = destHost;
    this.destPort = destPort;
    this.input = new LogStream();
    this.output = new LogStream();
  }

  /**
   * A {@link ByteArrayOutputStream} that can hand out the head of what it holds and its current
   * size without copying the whole conversation, so that cell renderers stay cheap even when a
   * call has accumulated megabytes.
   */
  public static class LogStream extends ByteArrayOutputStream {
    public synchronized String head(int max) {
      return new String(buf, 0, Math.min(max, count), StandardCharsets.UTF_8);
    }
  }

  public OutputStream getOutputLogger() {
    return output;
  }

  public OutputStream getInputLogger() {
    return input;
  }

  public String toString() {
    return CallStringFormatter.format(this);
  }

  public void setEnd(long end) {
    this.end = end;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  public String getSrcHost() {
    return srcHost;
  }

  public String getDestHost() {
    return destHost;
  }

  public int getSrcPort() {
    return srcPort;
  }

  public int getDestPort() {
    return destPort;
  }

  public ByteArrayOutputStream getInput() {
    return input;
  }

  public ByteArrayOutputStream getOutput() {
    return output;
  }

  /**
   * The first {@code max} bytes the client sent, as text. Cheap enough for a cell renderer.
   */
  public String getRequestHead(int max) {
    return output.head(max);
  }

  public int getRequestSize() {
    return output.size();
  }

  public int getResponseSize() {
    return input.size();
  }

  /**
   * What the client sent, as text.
   */
  public String getRequestText() {
    return new String(output.toByteArray(), StandardCharsets.UTF_8);
  }

  /**
   * What the destination answered, as text, with any chunked framing removed.
   */
  public String getResponseText() {
    return removeChunkedEncoding(input.toByteArray());
  }

  /**
   * What the destination answered, as text, exactly as it came off the wire.
   */
  public String getRawResponseText() {
    return new String(input.toByteArray(), StandardCharsets.UTF_8);
  }

  /**
   * The HTTP request lines sent by the client on this call, in the order they were sent.
   * Empty when the call does not carry HTTP traffic.
   */
  public List<String> getRequestLines() {
    return extractRequestLines(getRequestText());
  }

  public static List<String> extractRequestLines(String request) {
    List<String> lines = new ArrayList<>();
    if (request == null || request.isEmpty()) {
      return lines;
    }

    Matcher matcher = REQUEST_LINE_PATTERN.matcher(request);
    while (matcher.find()) {
      lines.add(matcher.group().trim());
    }

    return lines;
  }

  /**
   * Rebuilds the captured response with every {@code Transfer-Encoding: chunked} body decoded, so
   * that the payload comes out byte-identical to what the client's own HTTP stack sees.
   * <p>
   * Chunk sizes count <em>bytes</em>, so this has to work on the raw capture: decoding to a
   * {@link String} first would shift every boundary by the number of multi-byte characters seen so
   * far. A keep-alive call can carry several responses back to back, so each header/body pair is
   * decoded in turn. Whatever does not parse as chunked framing — a truncated capture, a non-HTTP
   * protocol — is copied through verbatim rather than dropped.
   */
  public static String removeChunkedEncoding(byte[] response) {
    if (response == null || response.length == 0) {
      return "";
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream(response.length);
    int pos = 0;

    while (pos < response.length) {
      int headerEnd = indexOf(response, CRLF_CRLF, pos);
      if (headerEnd == -1) {
        // No header block: not HTTP, or the capture stopped mid-headers.
        out.write(response, pos, response.length - pos);
        break;
      }

      int bodyStart = headerEnd + CRLF_CRLF.length;
      out.write(response, pos, bodyStart - pos);

      String header = new String(response, pos, headerEnd - pos, StandardCharsets.ISO_8859_1);
      if (!CHUNKED_HEADER_PATTERN.matcher(header).find()) {
        out.write(response, bodyStart, response.length - bodyStart);
        break;
      }

      pos = decodeChunkedBody(response, bodyStart, out);
    }

    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }

  /**
   * Writes the decoded chunks starting at {@code from} into {@code out} and returns the offset just
   * past the terminating chunk, where the next response of a keep-alive call begins. On malformed
   * or truncated framing the remainder is written out untouched and the buffer length is returned.
   */
  private static int decodeChunkedBody(byte[] body, int from, ByteArrayOutputStream out) {
    int pos = from;

    while (pos < body.length) {
      int eol = indexOf(body, CRLF, pos);
      if (eol == -1) {
        break;
      }

      int size = parseChunkSize(body, pos, eol);
      if (size < 0) {
        break;
      }

      if (size == 0) {
        // Terminating chunk, followed by optional trailers and the final empty line.
        int trailerEnd = indexOf(body, CRLF_CRLF, eol);
        return trailerEnd == -1 ? body.length : trailerEnd + CRLF_CRLF.length;
      }

      int chunkStart = eol + CRLF.length;
      if (size > body.length - chunkStart) {
        break;
      }

      out.write(body, chunkStart, size);
      pos = chunkStart + size;

      // Each chunk is closed by its own CRLF, which is framing, not payload.
      if (!startsWith(body, CRLF, pos)) {
        break;
      }
      pos += CRLF.length;
    }

    out.write(body, pos, body.length - pos);
    return body.length;
  }

  /**
   * The chunk size declared in {@code [from, end)} — a hex length optionally followed by a
   * {@code ;extension} — or -1 when the line is not a chunk header.
   */
  private static int parseChunkSize(byte[] body, int from, int end) {
    int digits = 0;
    int size = 0;

    for (int i = from; i < end; i++) {
      int digit = Character.digit(body[i], 16);
      if (digit == -1) {
        // Only a chunk extension or trailing whitespace may follow the length.
        if (digits > 0 && (body[i] == ';' || body[i] == ' ' || body[i] == '\t')) {
          break;
        }
        return -1;
      }
      if (++digits > 8) {
        return -1;
      }
      size = (size << 4) | digit;
    }

    return digits == 0 ? -1 : size;
  }

  private static int indexOf(byte[] haystack, byte[] needle, int from) {
    for (int i = Math.max(from, 0); i <= haystack.length - needle.length; i++) {
      if (startsWith(haystack, needle, i)) {
        return i;
      }
    }
    return -1;
  }

  private static boolean startsWith(byte[] haystack, byte[] needle, int at) {
    if (at < 0 || at > haystack.length - needle.length) {
      return false;
    }
    for (int i = 0; i < needle.length; i++) {
      if (haystack[at + i] != needle[i]) {
        return false;
      }
    }
    return true;
  }
}
