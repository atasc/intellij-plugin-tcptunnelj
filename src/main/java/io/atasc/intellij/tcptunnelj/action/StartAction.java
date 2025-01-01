package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.Messages;
import io.atasc.intellij.tcptunnelj.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

/**
 * @author boruvka/atasc
 * @since
 */
public class StartAction extends BaseAction {
  public StartAction(TunnelPlugin tunnelPlugin) {
    super("Start tcp tunnel", "Start tcp tunnel",
        Icons.ICON_START, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    //Project project = (Project) event.getDataContext().getData("project");
    //TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);

    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    try {
      tunnelPanel.start();
    } catch (Exception e) {
      Messages.showMessageDialog("Error when starting server: "
          + e.getMessage(), "Error", Messages.getErrorIcon());
    }

  }

  @Override
  public void update(AnActionEvent event) {
    //Project project = (Project) event.getDataContext().getData("project");
    //TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);

    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    Presentation p = event.getPresentation();
    p.setEnabled(!tunnelPanel.isRunning());
    p.setVisible(true);
  }
}
