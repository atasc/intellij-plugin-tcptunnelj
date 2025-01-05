package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class BaseToggleAction extends ToggleAction {
  protected TcpTunnelPlugin tunnelPlugin;
  protected boolean selected = false;

  public BaseToggleAction(String text, String description,
                          Icon icon, TcpTunnelPlugin tunnelPlugin) {
    this(text, description, icon);
    this.tunnelPlugin = tunnelPlugin;
  }

  public BaseToggleAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
    return false;
  }

  @Override
  public void setSelected(@NotNull AnActionEvent anActionEvent, boolean b) {

  }

  @Override
  public ActionUpdateThread getActionUpdateThread() {
    // Specify that this action must be executed on the Event Dispatch Thread (EDT).
    return ActionUpdateThread.EDT;
  }
}
