package io.atasc.intellij.tcptunnelj.tunnellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.Icons;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.TunnelPanel;

/**
 * @author boruvka/atasc
 * @since
 */
public class ClearSelectedAction extends BaseAction {

  public ClearSelectedAction(TunnelPlugin tunnelPlugin) {
    super("Remove selected call", "Remove selected call",
        Icons.ICON_REMOVE, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    //Project project = (Project) event.getDataContext().getData("project");
    //TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);

    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();
    tunnelPanel.clearSelected();
  }
}
