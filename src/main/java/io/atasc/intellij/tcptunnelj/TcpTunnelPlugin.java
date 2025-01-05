package io.atasc.intellij.tcptunnelj;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import io.atasc.intellij.tcptunnelj.action.*;
import io.atasc.intellij.tcptunnelj.listeners.TcpTunnelProjectManagerListener;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author boruvka/atasc
 * @since
 */
public class TcpTunnelPlugin implements Disposable, AutoCloseable {
  private static final String COMPONENT_NAME = "io.atasc.intellij.tcptunnelj.TunnelWindow";
  private static final String TOOL_WINDOW_ID = "TcpTunnelJ";

  //private ToolWindow tunnelWindow;
  private Project project;
  private TunnelPanel tunnelPanel;
  private TcpTunnelConfig tunnelConfig;
  private boolean initialized=false;

  public TunnelPanel getTunnelPanel() {
    return this.tunnelPanel;
  }

  public TcpTunnelConfig getTunnelConfig() {
    return tunnelConfig;
  }

  //  @Override
//  public String getComponentName() {
//    return COMPONENT_NAME;
//  }

  public TcpTunnelPlugin(Project project) {
    this.project = project;

    this.tunnelConfig = new TcpTunnelConfig(project.getName());
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

    //build ui
    this.getContent();

    this.initialized = true;
  }

  public TcpTunnelPlugin() {
    super();
  }

//  @Override
//  protected void finalize() throws Throwable {
//    try {
//      this.closeTheTunnel();
//      this.tunnelPanel = null;
//      System.out.println("Finalize called. Cleaning up resources.");
//    } finally {
//      super.finalize();
//    }
//  }

  @Override
  public void dispose() {
    this.closeTheTunnel();
    this.tunnelPanel = null;
  }

//  @Override
//  public void projectOpened() {
//    ProjectComponent.super.projectOpened();
//  }

  public void projectClosed() {
    this.closeTheTunnel();
    unregisterToolWindow();
  }

  public synchronized void initComponent() {
  }

  @Override
  public void close() {
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
    ToggleAction wrapAction = new WrapAction(this);
    AnAction saveAction = new SaveAction(this);
    ToggleAction startOnBootAction = new StartOnBootAction(this);
    //AnAction aboutAction = new AboutAction(this);

    actionGroup.add(startAction);
    actionGroup.add(stopAction);
    actionGroup.add(clearSelectedAction);
    actionGroup.add(clearAction);
    actionGroup.add(wrapAction);
    actionGroup.add(saveAction);

    // Add a separator
    actionGroup.addSeparator("Options");

    actionGroup.add(startOnBootAction);
    //actionGroup.add(aboutAction);

    return actionGroup;
  }

}
