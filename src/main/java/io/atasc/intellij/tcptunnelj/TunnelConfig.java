package io.atasc.intellij.tcptunnelj;

import java.io.*;
import java.util.Properties;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelConfig {
  public static Properties PROPERTIES;
  private static final String PROPERTIES_FILE_NAME = ".tcptunnelj.properties";
  private static File PROPERTIES_FILE;

  public static final int BUFFER_LENGTH = 4096;
  private String SRC_PORT = ".tcptunnelj.src.port";
  private String DST_HOST = ".tcptunnelj.dst.hostname";
  private String DST_PORT = ".tcptunnelj.dst.port";

  private static String projectName;

  static {
    PROPERTIES_FILE = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
    PROPERTIES = new Properties();
  }

  public TunnelConfig(String projectName) {
    this.init();
    this.setProjectName(projectName);
  }

  public static String normalizeProjectName(String name) {
    if (name != null) {
      name = name.replace(" ", "_").toLowerCase();

    }
    return name;
  }

  private void setProjectName(String name) {
    if (name != null) {
      this.projectName = normalizeProjectName(name);

      this.SRC_PORT = projectName + ".tcptunnelj.src.port";
      this.DST_HOST = projectName + ".tcptunnelj.dst.hostname";
      this.DST_PORT = projectName + ".tcptunnelj.dst.port";
    }
  }

  public synchronized void init() {
    if (PROPERTIES_FILE.exists()) {
      try {
        InputStream is = new FileInputStream(PROPERTIES_FILE);
        PROPERTIES.load(is);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  public String getDestinationString() {
    return PROPERTIES.getProperty(DST_HOST, "localhost");
  }

  public void setDestinationString(String destination) {
    PROPERTIES.setProperty(DST_HOST, destination);
  }

  public String getDestinationPort() {
    return PROPERTIES.getProperty(DST_PORT, "6060");
  }

  public void setDestinationPort(String port) {
    PROPERTIES.setProperty(DST_PORT, port);
  }

  public String getSourcePort() {
    return PROPERTIES.getProperty(SRC_PORT, "4444");
  }

  public void setSourcePort(String port) {
    PROPERTIES.setProperty(SRC_PORT, port);
  }

  public synchronized void store() {
    try {
      OutputStream os = new FileOutputStream(PROPERTIES_FILE);
      PROPERTIES.store(os, "TcpTunnelJ Plugin");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
