package io.atasc.intellij.tcptunnelj.util;

public class Tracer {
  private static final int TERMINAL_WIDTH = 120; // Definisci una larghezza approssimativa del terminale

  public static void println(String src) {
    System.out.println(src + StringUtil.newLine());
  }

  public static void println(String format, Object... args) {
    String src = String.format(format, args);
    System.out.println(src + StringUtil.newLine());
  }

  public static void println(AnsiColor color, String src) {
    System.out.println(color.getCode() + src + AnsiColor.RESET.getCode() + StringUtil.newLine());
  }

  public static void println(AnsiColor color, String format, Object... args) {
    String src = String.format(format, args);
    System.out.println(color.getCode() + src + AnsiColor.RESET.getCode() + StringUtil.newLine());
  }

//  public static void printlnQuery(Query query) {
//    Tracer.println(query.unwrap(org.hibernate.Query.class).getQueryString());
//  }

  public static void print(String format, Object... args) {
    String src = String.format(format, args);
    int remainingLength = TERMINAL_WIDTH - src.length();
    String padding = remainingLength > 0 ? " ".repeat(remainingLength) : "";
    System.out.print("\r" + src + padding); // Aggiungi "\r" all'inizio e riempi con spazi bianchi
  }

}
