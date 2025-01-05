package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author atasc
 * @since
 */
public class BaseAction extends AnAction {
  protected TcpTunnelPlugin tunnelPlugin;

  public BaseAction(String text, String description,
                    Icon icon, TcpTunnelPlugin tunnelPlugin) {
    this(text, description, icon);
    this.tunnelPlugin = tunnelPlugin;
  }

  public BaseAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

  }

  @Override
  public ActionUpdateThread getActionUpdateThread() {
    // Specify that this action must be executed on the Event Dispatch Thread (EDT).
    return ActionUpdateThread.EDT;
  }
}
