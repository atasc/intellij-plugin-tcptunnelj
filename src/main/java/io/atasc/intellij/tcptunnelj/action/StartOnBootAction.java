package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;

/**
 * @author atasc
 * @since
 */
public class StartOnBootAction extends BaseToggleAction {
  public StartOnBootAction(TcpTunnelPlugin tunnelPlugin) {
    super("Start on Boot", "Start on Boot", Icons.ICON_WRAP);
    this.tunnelPlugin = tunnelPlugin;
  }

  @Override
  public boolean isSelected(AnActionEvent event) {
    return selected;
  }

  @Override
  public void setSelected(AnActionEvent event, boolean b) {
    selected = b;


  }
}
