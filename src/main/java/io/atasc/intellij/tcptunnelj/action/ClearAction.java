package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import io.atasc.intellij.tcptunnelj.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

/**
 * @author boruvka/atasc
 * @since
 */
public class ClearAction extends BaseAction {
  public ClearAction(TunnelPlugin tunnelPlugin) {
    super("Remove all calls from list", "Remove all calls from list",
        Icons.ICON_CLEAR, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    //Project project = (Project) event.getDataContext().getData("project");
    //TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);

    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();
    tunnelPanel.clear();
  }
}
