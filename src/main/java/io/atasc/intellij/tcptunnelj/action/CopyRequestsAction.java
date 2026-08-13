package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.toolWindow.TcpTunnelWindow;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

import java.util.List;

/**
 * Copies the request lines of the selected calls to the clipboard,
 * one per line, in the form "GET /path?query HTTP/1.1".
 *
 * @author atasc
 * @since
 */
public class CopyRequestsAction extends BaseAction {

  public CopyRequestsAction(TcpTunnelPlugin tunnelPlugin) {
    super("Copy requests", "Copy the requests of the selected calls to the clipboard",
        Icons.ICON_COPY, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();
    if (tunnelPanel == null) {
      return;
    }

    List<String> copied = tunnelPanel.copySelectedRequestsToClipboard();

    if (copied.isEmpty()) {
      Notifications.Bus.notify(new Notification(
          TcpTunnelWindow.NOTIFICATION_ID,
          "Nothing copied",
          "No HTTP request found in the selected calls.",
          NotificationType.WARNING
      ));
      return;
    }

    Notifications.Bus.notify(new Notification(
        TcpTunnelWindow.NOTIFICATION_ID,
        "Requests copied",
        copied.size() + (copied.size() == 1 ? " request" : " requests") + " copied to the clipboard.",
        NotificationType.INFORMATION
    ));
  }

  @Override
  public void update(AnActionEvent event) {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    Presentation p = event.getPresentation();
    p.setEnabled(tunnelPanel != null && tunnelPanel.getSelectedCallsSize() > 0);
    p.setVisible(true);
  }
}
