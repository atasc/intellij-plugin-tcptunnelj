package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.TunnelPlugin;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author atasc
 * @since
 */
public class BaseAction extends AnAction {
  protected TunnelPlugin tunnelPlugin;

  public BaseAction(String text, String description,
                    Icon icon, TunnelPlugin tunnelPlugin) {
    this(text, description, icon);
    this.tunnelPlugin = tunnelPlugin;
  }

  public BaseAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

  }
}
