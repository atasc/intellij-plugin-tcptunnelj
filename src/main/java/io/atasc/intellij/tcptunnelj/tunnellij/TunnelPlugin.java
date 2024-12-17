package io.atasc.intellij.tcptunnelj.tunnellij;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindowManager;
import io.atasc.intellij.tcptunnelj.listeners.TcpTunnelProjectManagerListener;
import io.atasc.intellij.tcptunnelj.tunnellij.action.*;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.TunnelPanel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPlugin implements ProjectComponent, Disposable, AutoCloseable {
  public static Properties PROPERTIES;
  private static final String PROPERTIES_FILE_NAME = ".tcptunnelj.properties";
  private static File PROPERTIES_FILE;

  private static final String COMPONENT_NAME = "io.atasc.intellij.tcptunnelj.tunnellij.TunnelWindow";
  private static final String TOOL_WINDOW_ID = "TcpTunnelJ";

  //private ToolWindow tunnelWindow;
  private Project project;
  private TunnelPanel tunnelPanel;

  public TunnelPanel getTunnelPanel() {
    return this.tunnelPanel;
  }

  @Override
  public String getComponentName() {
    return COMPONENT_NAME;
  }

  static {
    PROPERTIES_FILE = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
    PROPERTIES = new Properties();
  }

  public TunnelPlugin(Project project) {
    this.project = project;
    TunnelConfig.setProjectName(project.getName());

    TcpTunnelProjectManagerListener.attachListener(project, new ProjectManagerListener() {
      @Override
      public void projectClosed(Project project) {
        System.out.println("Project closed: " + project.getName());
      }

      @Override
      public void projectClosing(Project project) {
        System.out.println("Project closing: " + project.getName());
        closeTheTunnel();
      }
    });
  }

  public TunnelPlugin() {
    super();
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  protected void finalize() throws Throwable {
    try {
      closeTheTunnel();
      this.tunnelPanel = null;
      System.out.println("Finalize called. Cleaning up resources.");
    } finally {
      super.finalize();
    }
  }

  @Override
  public void dispose() {
    //Called in TcpTunnelAppLifecycleListener
    //TunnelConfig.store();
    closeTheTunnel();
    this.tunnelPanel = null;
  }

  @Override
  public void projectOpened() {
    ProjectComponent.super.projectOpened();
  }

  public void projectClosed() {
    closeTheTunnel();
    unregisterToolWindow();
  }

  public synchronized void initComponent() {
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

  @Override
  public void close() throws Exception {
    //Called in TcpTunnelAppLifecycleListener
    //TunnelConfig.store();
    closeTheTunnel();
    this.tunnelPanel = null;
  }

  public synchronized void disposeComponent() {
    try {
      OutputStream os = new FileOutputStream(PROPERTIES_FILE);
      PROPERTIES.store(os, "TcpTunnel Plugin");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public TunnelPanel getContent() {
    initComponent();

    if (this.tunnelPanel == null) {
      this.tunnelPanel = createTunnelPanel();

      DefaultActionGroup actionGroup = initToolbarActionGroup();
      ActionToolbar toolBar = ActionManager.getInstance()
          .createActionToolbar("tcptunnelj.Toolbar", actionGroup, false);

      toolBar.setTargetComponent(this.tunnelPanel);
      this.tunnelPanel.add(toolBar.getComponent(), BorderLayout.WEST);
    }

    return this.tunnelPanel;
  }

  private void closeTheTunnel() {
    try {
      if (this.tunnelPanel != null) {
        this.tunnelPanel.stop();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void unregisterToolWindow() {
    ToolWindowManager toolWindowManager = ToolWindowManager
        .getInstance(this.project);
    toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
  }

  private static TunnelPanel createTunnelPanel() {
    TunnelPanel panel = new TunnelPanel();
    panel.setBackground(UIManager.getColor("Tree.textBackground"));
    return panel;
  }

  private DefaultActionGroup initToolbarActionGroup() {
    DefaultActionGroup actionGroup = new DefaultActionGroup();

    AnAction startAction = new StartAction(this);
    AnAction stopAction = new StopAction(this);
    AnAction clearAction = new ClearAction(this);
    AnAction clearSelectedAction = new ClearSelectedAction(this);
    //AnAction aboutAction = new AboutAction(this);
    ToggleAction wrapAction = new WrapAction(this);

    actionGroup.add(startAction);
    actionGroup.add(stopAction);
    actionGroup.add(clearSelectedAction);
    actionGroup.add(clearAction);
    actionGroup.add(wrapAction);
    //actionGroup.add(aboutAction);

    return actionGroup;
  }

  public static class TunnelConfig {
    private static String projectName;

    public static String normalizeProjectName(String name) {
      if (name != null) {
        name = name.replace(" ", "_").toLowerCase();

      }
      return name;
    }

    public static void setProjectName(String name) {
      if (name != null) {
        projectName = normalizeProjectName(name);

        SRC_PORT = projectName + ".tcptunnelj.src.port";
        DST_HOST = projectName + ".tcptunnelj.dst.hostname";
        DST_PORT = projectName + ".tcptunnelj.dst.port";
      }
    }

    public static final int BUFFER_LENGTH = 4096;

    private static String SRC_PORT = projectName + ".tcptunnelj.src.port";
    private static String DST_HOST = projectName + ".tcptunnelj.dst.hostname";
    private static String DST_PORT = projectName + ".tcptunnelj.dst.port";

    public static String getDestinationString() {
      return PROPERTIES.getProperty(DST_HOST, "localhost");
    }

    public static void setDestinationString(String destination) {
      PROPERTIES.setProperty(DST_HOST, destination);
    }

    public static String getDestinationPort() {
      return PROPERTIES.getProperty(DST_PORT, "6060");
    }

    public static void setDestinationPort(String port) {
      PROPERTIES.setProperty(DST_PORT, port);
    }

    public static String getSourcePort() {
      return PROPERTIES.getProperty(SRC_PORT, "4444");
    }

    public static void setSourcePort(String port) {
      PROPERTIES.setProperty(SRC_PORT, port);
    }

    public static synchronized void store() {
      try {
        OutputStream os = new FileOutputStream(PROPERTIES_FILE);
        PROPERTIES.store(os, "TcpTunnelJ Plugin");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
