package io.atasc.intellij.tcptunnelj.tunnellij;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
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

  private static TunnelPanel tunnelPanel;

  public static Properties PROPERTIES;

  private static final String PROPERTIES_FILE_NAME = ".tcptunnelj.properties";

  private static File PROPERTIES_FILE;

  private static final String COMPONENT_NAME = "io.atasc.intellij.tcptunnelj.tunnellij.TunnelWindow";

  private static final String TOOL_WINDOW_ID = "TcpTunnelJ";

  static {
    PROPERTIES_FILE = new File(System.getProperty("user.home"), PROPERTIES_FILE_NAME);
    PROPERTIES = new Properties();
  }

  private ToolWindow tunnelWindow;

  private Project project;

  public TunnelPlugin(Project project) {
    this.project = project;
    TunnelConfig.setProjectName(project.getName());
  }

  public static TunnelPanel getTunnelPanel(Project project) {
    return tunnelPanel;
  }

  @Override
  public void projectOpened() {
    ProjectComponent.super.projectOpened();
  }

  public void projectClosed() {
    unregisterToolWindow();
  }

  public String getComponentName() {
    return COMPONENT_NAME;
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
  protected void finalize() throws Throwable {
    try {
      tunnelPanel = null;
      System.out.println("Finalize called. Cleaning up resources.");
    } finally {
      super.finalize();
    }
  }

  @Override
  public void dispose() {
    //Called in TcpTunnelAppLifecycleListener
    //TunnelConfig.store();
    tunnelPanel = null;
  }

  @Override
  public void close() throws Exception {
    //Called in TcpTunnelAppLifecycleListener
    //TunnelConfig.store();
    tunnelPanel = null;
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

    if (tunnelPanel == null) {
      tunnelPanel = createTunnelPanel();

      DefaultActionGroup actionGroup = initToolbarActionGroup();
      ActionToolbar toolBar = ActionManager.getInstance()
          .createActionToolbar("tcptunnelj.Toolbar", actionGroup, false);

      toolBar.setTargetComponent(tunnelPanel);
      tunnelPanel.add(toolBar.getComponent(), BorderLayout.WEST);
    }

    return tunnelPanel;
  }

  private void unregisterToolWindow() {
    ToolWindowManager toolWindowManager = ToolWindowManager
        .getInstance(project);
    toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
  }

  private static TunnelPanel createTunnelPanel() {
    TunnelPanel panel = new TunnelPanel();
    panel.setBackground(UIManager.getColor("Tree.textBackground"));
    return panel;
  }

  private DefaultActionGroup initToolbarActionGroup() {
    DefaultActionGroup actionGroup = new DefaultActionGroup();

    AnAction startAction = new StartAction();
    AnAction stopAction = new StopAction();
    AnAction clearAction = new ClearAction();
    AnAction clearSelectedAction = new ClearSelectedAction();
    //AnAction aboutAction = new AboutAction();
    ToggleAction wrapAction = new WrapAction();

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

    public static void setProjectName(String name) {
      if (name != null) {
        projectName = name.replace(" ", "_").toLowerCase();

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
