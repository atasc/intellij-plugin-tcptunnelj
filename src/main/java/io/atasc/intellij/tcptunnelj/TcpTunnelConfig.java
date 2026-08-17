package io.atasc.intellij.tcptunnelj;

import java.io.*;
import java.util.Properties;

/**
 * @author boruvka/atasc
 * @since
 */
public class TcpTunnelConfig {
  private final Properties properties;
  private static final String PROPERTIES_FILE_NAME = ".tcptunnelj.properties";
  private static final File PROPERTIES_FILE = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
  public static final int BUFFER_LENGTH = 4096;

  private String srcPort = ".tcptunnelj.src.port";
  private String dstHost = ".tcptunnelj.dst.hostname";
  private String dstPort = ".tcptunnelj.dst.port";
  private String startOnBoot = ".tcptunnelj.start.on.boot";

  private String projectName;

  public String getDestinationString() {
    return properties.getProperty(dstHost, "localhost");
  }

  public void setDestinationString(String destination) {
    properties.setProperty(dstHost, destination);
  }

  public String getDestinationPort() {
    return properties.getProperty(dstPort, "6061");
  }

  public void setDestinationPort(String port) {
    properties.setProperty(dstPort, port);
  }

  public String getSourcePort() {
    return properties.getProperty(srcPort, "4445");
  }

  public void setSourcePort(String port) {
    properties.setProperty(srcPort, port);
  }

  // Getter and Setter for Start on Boot
  public boolean isStartOnBootEnabled() {
    return Boolean.parseBoolean(properties.getProperty(startOnBoot, "false"));
  }

  public void setStartOnBootEnabled(boolean enabled) {
    properties.setProperty(startOnBoot, Boolean.toString(enabled));
  }

  public TcpTunnelConfig(String projectName) {
    this.properties = new Properties();
    this.init();
    this.setProjectName(projectName);
  }

  public synchronized void init() {
    if (PROPERTIES_FILE.exists()) {
      try (InputStream is = new FileInputStream(PROPERTIES_FILE)) {
        properties.load(is);
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
    try (OutputStream os = new FileOutputStream(PROPERTIES_FILE)) {
      properties.store(os, "TcpTunnelJ Plugin");
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
