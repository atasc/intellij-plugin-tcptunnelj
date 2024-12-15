package io.atasc.intellij.nettunnel.tunnellij.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import io.atasc.intellij.nettunnel.tunnellij.TunnelPlugin;
import io.atasc.intellij.nettunnel.tunnellij.ui.Icons;
import io.atasc.intellij.nettunnel.tunnellij.ui.TunnelPanel;

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
