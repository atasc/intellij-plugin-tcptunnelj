package io.atasc.intellij.tcptunnelj.net;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author atasc
 * @since
 */
public class TunnelWriter extends Thread {
  private static final int BUFFER_SIZE = 8192;
  private final InputStream inputStream;
  private final OutputStream outputStream;
  private final OutputStream logStream;

  public TunnelWriter(InputStream inputStream, OutputStream outputStream, OutputStream logStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.logStream = logStream;
  }

  @Override
  public void run() {
    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
        outputStream.flush();

        if (logStream != null) {
          logStream.write(buffer, 0, bytesRead);
          logStream.flush();
        }
      }
    } catch (Exception e) {
      System.err.println("Error in TunnelWriter: " + e.getMessage());
    } finally {
      closeStreams();
    }
  }

  private void closeStreams() {
    try {
      if (outputStream != null) {
        outputStream.close();
      }
      if (logStream != null) {
        logStream.close();
      }
    } catch (Exception e) {
      System.err.println("Error closing streams: " + e.getMessage());
    }
  }
}
