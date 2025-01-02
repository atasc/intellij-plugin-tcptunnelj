package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.Messages;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

/**
 * @author boruvka/atasc
 * @since
 */
public class StopAction extends BaseAction {

  public StopAction(TcpTunnelPlugin tunnelPlugin) {
    super("Stop tcp tunnel", "Stop tcp tunnel",
        Icons.ICON_STOP, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    //Project project = (Project) event.getDataContext().getData("project");
    //TunnelPanel tunnelPanel = TunnelPlugin.getTunnelPanel(project);

    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    try {
      tunnelPanel.stop();
    } catch (Exception e) {
      e.printStackTrace();
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
    p.setEnabled(tunnelPanel.isRunning());
    p.setVisible(true);
  }

}
