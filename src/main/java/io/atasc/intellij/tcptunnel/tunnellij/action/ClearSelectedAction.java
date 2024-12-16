package io.atasc.intellij.tcptunnel.tunnellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.atasc.intellij.tcptunnel.tunnellij.TunnelPlugin;
import io.atasc.intellij.tcptunnel.tunnellij.ui.Icons;
import io.atasc.intellij.tcptunnel.tunnellij.ui.TunnelPanel;

/**
 * @author boruvka
 * @since
 */
public class ClearSelectedAction extends AnAction {

  public ClearSelectedAction() {
    super("Remove selected call", "Remove selected call", Icons.ICON_REMOVE);
  }

  public void actionPerformed(AnActionEvent event) {
    Project project = (Project) event.getDataContext().getData("project");
    TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);
    tunnelPanel.clearSelected();
  }
}
