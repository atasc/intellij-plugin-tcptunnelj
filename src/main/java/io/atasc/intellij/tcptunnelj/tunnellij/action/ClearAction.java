package io.atasc.intellij.tcptunnelj.tunnellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.Icons;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.TunnelPanel;

/**
 * @author boruvka
 * @since
 */
public class ClearAction extends AnAction {
  public ClearAction() {
    super("Remove all calls from list", "Remove all calls from list",
        Icons.ICON_CLEAR);
  }

  public void actionPerformed(AnActionEvent event) {
    Project project = (Project) event.getDataContext().getData("project");
    TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);
    tunnelPanel.clear();
  }
}
