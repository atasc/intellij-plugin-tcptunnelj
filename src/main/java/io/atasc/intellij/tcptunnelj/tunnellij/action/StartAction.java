package io.atasc.intellij.tcptunnelj.tunnellij.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.Icons;
import io.atasc.intellij.tcptunnelj.tunnellij.ui.TunnelPanel;

/**
 * @author boruvka
 * @since
 */
public class StartAction extends BaseAction {
  public StartAction(TunnelPlugin tunnelPlugin) {
    super("Start tunnellij", "Start tunnellij", Icons.ICON_START, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    Project project = (Project) event.getDataContext().getData("project");
    TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);
    try {
      tunnelPanel.start();
    } catch (Exception e) {
      Messages.showMessageDialog("Error when starting server: "
          + e.getMessage(), "Error", Messages.getErrorIcon());
    }

  }

  @Override
  public void update(AnActionEvent event) {
    Project project = (Project) event.getDataContext().getData("project");
    TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);
    Presentation p = event.getPresentation();
    p.setEnabled(!tunnelPanel.isRunning());
    p.setVisible(true);
  }
}
