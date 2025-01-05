package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

/**
 * @author boruvka/atasc
 * @since
 */
public class WrapAction extends ToggleAction {
  private TcpTunnelPlugin tunnelPlugin;
  private boolean selected = false;

  public WrapAction(TcpTunnelPlugin tunnelPlugin) {
    super("Wrap lines", "Wrap lines", Icons.ICON_WRAP);
    this.tunnelPlugin = tunnelPlugin;
  }

  @Override
  public boolean isSelected(AnActionEvent event) {
    return selected;
  }

  @Override
  public void setSelected(AnActionEvent event, boolean b) {
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

  @Override
  public ActionUpdateThread getActionUpdateThread() {
    // Specify that this action must be executed on the Event Dispatch Thread (EDT).
    return ActionUpdateThread.EDT;
  }
}
