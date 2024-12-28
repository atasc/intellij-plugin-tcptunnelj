package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.Messages;
import io.atasc.intellij.tcptunnelj.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

/**
 * @author atasc
 * @since
 */
public class SaveAction extends BaseAction {

  public SaveAction(TunnelPlugin tunnelPlugin) {
    super("Save calls", "Save calls",
        Icons.ICON_SAVE, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
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
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    Presentation p = event.getPresentation();
    p.setEnabled(tunnelPanel.isRunning());
    p.setVisible(true);
  }

}
