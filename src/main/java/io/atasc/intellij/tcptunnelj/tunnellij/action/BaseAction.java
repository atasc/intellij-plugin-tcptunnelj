package io.atasc.intellij.tcptunnelj.tunnellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.Icons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class BaseAction extends AnAction {
  private TunnelPlugin tunnelPlugin;

  public BaseAction(String text, String description, Icon icon, TunnelPlugin tunnelPlugin) {
    this( text,  description,  icon);
    this.tunnelPlugin = tunnelPlugin;
  }

  public BaseAction(String text, String description, Icon icon) {
    super(text, description, icon);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

  }
}
