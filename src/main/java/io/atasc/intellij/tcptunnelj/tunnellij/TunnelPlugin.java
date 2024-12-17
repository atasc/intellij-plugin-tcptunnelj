package io.atasc.intellij.tcptunnelj.tunnellij;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindowManager;
import io.atasc.intellij.tcptunnelj.TunnelConfig;
import io.atasc.intellij.tcptunnelj.listeners.TcpTunnelProjectManagerListener;
import io.atasc.intellij.tcptunnelj.tunnellij.action.*;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.TunnelPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPlugin implements ProjectComponent, Disposable, AutoCloseable {
  private static final String COMPONENT_NAME = "io.atasc.intellij.tcptunnelj.tunnellij.TunnelWindow";
  private static final String TOOL_WINDOW_ID = "TcpTunnelJ";

  //private ToolWindow tunnelWindow;
  private Project project;
  private TunnelPanel tunnelPanel;
  private TunnelConfig tunnelConfig;

  public TunnelPanel getTunnelPanel() {
    return this.tunnelPanel;
  }

  @Override
  public String getComponentName() {
    return COMPONENT_NAME;
  }
  public TunnelPlugin(Project project) {
    this.project = project;

    this.tunnelConfig = new TunnelConfig(project.getName());
    //tunnelConfig.setProjectName();

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
      this.closeTheTunnel();
      this.tunnelPanel = null;
      System.out.println("Finalize called. Cleaning up resources.");
    } finally {
      super.finalize();
    }
  }

  @Override
  public void dispose() {
    this.closeTheTunnel();
    this.tunnelPanel = null;
  }

  @Override
  public void projectOpened() {
    ProjectComponent.super.projectOpened();
  }

  public void projectClosed() {
    this.closeTheTunnel();
    unregisterToolWindow();
  }

  public synchronized void initComponent() {
  }

  @Override
  public void close() throws Exception {
    this.closeTheTunnel();
    this.tunnelPanel = null;
  }

  public synchronized void disposeComponent() {
    this.tunnelConfig.store();
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
//    ToolWindowManager toolWindowManager = ToolWindowManager
//        .getInstance(this.project);
//    toolWindowManager.unregisterToolWindow(TOOL_WINDOW_ID);
  }

  private TunnelPanel createTunnelPanel() {
    TunnelPanel panel = new TunnelPanel(this.tunnelConfig);
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

}
