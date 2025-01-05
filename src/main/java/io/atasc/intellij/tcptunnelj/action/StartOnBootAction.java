package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;

/**
 * @author atasc
 * @since
 */
public class StartOnBootAction extends BaseToggleAction {
  public StartOnBootAction(TcpTunnelPlugin tunnelPlugin) {
    super("Start on Boot", "Start on Boot", Icons.ICON_START_ON_BOOT);
    this.tunnelPlugin = tunnelPlugin;
  }

  @Override
  public boolean isSelected(AnActionEvent event) {
    return selected;
  }

  @Override
  public void setSelected(AnActionEvent event, boolean state) {
    selected = state;
    if (selected) {
      // Enable "Start on Boot" functionality
      ApplicationManager.getApplication().invokeLater(() -> {
        Notifications.Bus.notify(new Notification(
            "TcpTunnelJ Notifications",
            "Setting Saved",
            "Start on Boot enabled!",
            NotificationType.INFORMATION
        ));
      });

    } else {
      // Disable "Start on Boot" functionality
      ApplicationManager.getApplication().invokeLater(() -> {
        Notifications.Bus.notify(new Notification(
            "TcpTunnelJ Notifications",
            "Setting Saved",
            "Start on Boot disabled!",
            NotificationType.INFORMATION
        ));
      });

    }

    ApplicationManager.getApplication().invokeLater(() -> {
      try {

      } catch (Exception e) {
        Notifications.Bus.notify(new Notification(
            "TcpTunnelJ Notifications",
            "Error",
            "Error while saving log file: " + e.getMessage(),
            NotificationType.ERROR
        ));
      }
    });

  }
}
