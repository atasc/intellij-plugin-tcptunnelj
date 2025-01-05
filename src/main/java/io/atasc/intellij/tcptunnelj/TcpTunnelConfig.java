package io.atasc.intellij.tcptunnelj;

import java.io.*;
import java.util.Properties;

/**
 * @author boruvka/atasc
 * @since
 */
public class TcpTunnelConfig {
  public static Properties PROPERTIES;
  private static final String PROPERTIES_FILE_NAME = ".tcptunnelj.properties";
  private static File PROPERTIES_FILE;
  public static final int BUFFER_LENGTH = 4096;

  private String srcPort = ".tcptunnelj.src.port";
  private String dstHost = ".tcptunnelj.dst.hostname";
  private String dstPort = ".tcptunnelj.dst.port";
  private String startOnBoot = ".tcptunnelj.start.on.boot";

  private static String projectName;

  public String getDestinationString() {
    return PROPERTIES.getProperty(dstHost, "localhost");
  }

  public void setDestinationString(String destination) {
    PROPERTIES.setProperty(dstHost, destination);
  }

  public String getDestinationPort() {
    return PROPERTIES.getProperty(dstPort, "6060");
  }

  public void setDestinationPort(String port) {
    PROPERTIES.setProperty(dstPort, port);
  }

  public String getSourcePort() {
    return PROPERTIES.getProperty(srcPort, "4444");
  }

  public void setSourcePort(String port) {
    PROPERTIES.setProperty(srcPort, port);
  }

  // Getter and Setter for Start on Boot
  public boolean isStartOnBootEnabled() {
    return Boolean.parseBoolean(PROPERTIES.getProperty(startOnBoot, "false"));
  }

  public void setStartOnBootEnabled(boolean enabled) {
    PROPERTIES.setProperty(startOnBoot, Boolean.toString(enabled));
  }

  static {
    PROPERTIES_FILE = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
    PROPERTIES = new Properties();
  }

  public TcpTunnelConfig(String projectName) {
    this.init();
    this.setProjectName(projectName);
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

  private void setProjectName(String name) {
    if (name != null) {
      this.projectName = normalizeProjectName(name);

      this.srcPort = projectName + ".tcptunnelj.src.port";
      this.dstHost = projectName + ".tcptunnelj.dst.hostname";
      this.dstPort = projectName + ".tcptunnelj.dst.port";
      this.startOnBoot = projectName + ".tcptunnelj.start.on.boot"; // Update for project-specific key
    }
  }

  public synchronized void store() {
    try {
      OutputStream os = new FileOutputStream(PROPERTIES_FILE);
      PROPERTIES.store(os, "TcpTunnelJ Plugin");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String normalizeProjectName(String name) {
    if (name != null) {
      name = name.replace(" ", "_").toLowerCase();

    }
    return name;
  }
}
