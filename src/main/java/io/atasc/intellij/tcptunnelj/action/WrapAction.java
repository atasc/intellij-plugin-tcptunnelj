package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;
import org.jetbrains.annotations.NotNull;

/**
 * @author boruvka/atasc
 * @since
 */
public class WrapAction extends BaseToggleAction {
  public WrapAction(TcpTunnelPlugin tunnelPlugin) {
    super("Wrap lines", "Wrap lines", Icons.ICON_WRAP);
    this.tunnelPlugin = tunnelPlugin;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent event) {
    return selected;
  }

  @Override
  public void setSelected(@NotNull AnActionEvent event, boolean b) {
    selected = b;

    //Project project = (Project) event.getDataContext().getData("project");
    //TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);

    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();
    if (selected) {
      tunnelPanel.wrap();
    } else {
      tunnelPanel.unwrap();
    }
  }
}
