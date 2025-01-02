package io.atasc.intellij.tcptunnelj.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class StringUtil {
  public static String fromInputStream(InputStream in) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringBuilder out = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    String line;
    while ((line = reader.readLine()) != null) {
      out.append(line);
      out.append(newLine);
    }
    return out.toString();
  }

  public static String clear4ByteUtf8Chars(String src) {
    if (src != null) {
      src = src.replaceAll("[^\\u0000-\\uFFFF]", "");
    }

    return src;
  }

  public static String newLine() {
    return System.getProperty("line.separator");
  }

  public static boolean isNullOrEmpty(String src) {
    return (src == null || src == "");
  }

  public static boolean isNotNullOrEmpty(String src) {
    return (src != null && src != "");
  }

  public static String formatDate(Date date, String format) {
    if (date == null || StringUtil.isNullOrEmpty(format)) {
      return null;
    }

    String dateFormatted = new SimpleDateFormat(format).format(date);

    return dateFormatted;
  }

  public static String formatDecimal(BigDecimal value) {
    if (value == null) {
      return null;
    }

    NumberFormat formatter = new DecimalFormat("#.00");
    String formatted = formatter.format(value);

    return formatted;
  }

  public static String encodeBase64(String originalInput) {
    String encodedString = null;

    if (originalInput != null) {
      encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());
    }

    return encodedString;
  }

  public static String dencodeBase64(String encodedString) {
    String decodedString = null;

    if (encodedString != null) {
      byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
      decodedString = new String(decodedBytes);
    }

    return decodedString;
  }

  public static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      double d = Double.parseDouble(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

}
