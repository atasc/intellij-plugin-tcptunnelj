package io.atasc.intellij.tcptunnelj.ui;

import io.atasc.intellij.tcptunnelj.net.Call;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author boruvka/atasc
 * @since
 */
public class CallStringFormatter {
  private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SS");

  public synchronized static String format(Call call) {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    sb.append(dateFormat.format(new Date(call.getStart())));

    if (call.getOutput() != null) {
      byte[] arr = call.getOutput().toByteArray();
      if (arr != null) {
        int len = (arr.length > Call.CMD_LENGTH) ? Call.CMD_LENGTH : arr.length;
        byte[] text = new byte[len];
        System.arraycopy(arr, 0, text, 0, len);
        sb.append("] ").append(new String(text)).append("...");
      }
    }

    sb.append("; from ");
    sb.append(call.getSrcHost());
    sb.append(":");
    sb.append(call.getSrcPort());
    sb.append(" to ");
    sb.append(call.getDestHost());
    sb.append(":");
    sb.append(call.getDestPort());
    //sb.append("; response: ");

    sb.append(", RQ: ");
    sb.append((call.getOutput() == null) ? " ? B" : formatSize(call.getOutput().toByteArray().length));

    sb.append(", RS: ");
    sb.append((call.getInput() == null) ? " ? B" : formatSize(call.getInput().toByteArray().length));

    if (call.getEnd() != -1) {
      long durationMs = call.getEnd() - call.getStart();
      sb.append(", duration: ").append(formatDuration(durationMs));
    }

    return sb.toString();
  }

  private static String formatSize(int bytes) {
    if (bytes >= 1024) {
      double kb = bytes / 1024.0;
      return String.format("%.2f KB", kb);
    } else {
      return bytes + " B";
    }
  }

  private static String formatDuration(long durationMs) {
    //Tracer.print("CallStringFormatter.formatDuration: " + durationMs);
    if (durationMs > 1000) {
      double seconds = durationMs / 1000.0;
      return String.format("%.2f seconds", seconds);
    } else {
      return durationMs + " ms";
    }
  }
}
