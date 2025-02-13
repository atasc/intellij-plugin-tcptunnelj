package io.atasc.intellij.tcptunnelj.net;

import io.atasc.intellij.tcptunnelj.ui.CallStringFormatter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @author boruvka/atasc
 * @since
 */
public class Call {
  public static final int CMD_LENGTH = 80;

  private long start;
  private long end = -1;
  private String srcHost;
  private String destHost;
  private int srcPort;
  private int destPort;
  private ByteArrayOutputStream input;
  private ByteArrayOutputStream output;

  public Call(String srcHost, int srcPort, String destHost, int destPort) {
    this.start = System.currentTimeMillis();
    this.srcHost = srcHost;
    this.srcPort = srcPort;
    this.destHost = destHost;
    this.destPort = destPort;
    this.input = new ByteArrayOutputStream();
    this.output = new ByteArrayOutputStream();
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

//  public static String removeChunkedEncoding(String response) {
//    // Use a regex to identify chunks (hexadecimal numbers followed by a newline)
//    if (response.contains("Transfer-Encoding: chunked")) {
//      return response.replaceAll("(?m)^[0-9a-fA-F]+\\r?\\n", "");
//    }
//    return response;
//  }

  public static String removeChunkedEncoding(String response) {
    // Divide header and body using double newline as delimiter
    int headerEndIndex = response.indexOf("\r\n\r\n");
    if (headerEndIndex == -1) {
      // No headers found, return response as is
      return response;
    }

    // Extract header and body
    String header = response.substring(0, headerEndIndex);
    String body = response.substring(headerEndIndex + 4);

    // Check for Transfer-Encoding: chunked in the header only
    if (header.contains("Transfer-Encoding: chunked")) {
      // Remove chunked encoding from the body
      body = body.replaceAll("(?m)^[0-9a-fA-F]+\\r?\\n", "");
    }

    // Reassemble response
    return header + "\r\n\r\n" + body;
  }
}
