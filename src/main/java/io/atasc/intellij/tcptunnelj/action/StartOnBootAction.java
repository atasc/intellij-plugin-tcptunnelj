package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.toolWindow.TcpTunnelWindow;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author atasc
 * @since
 */
public class StartOnBootAction extends BaseToggleAction {
  private final TcpTunnelConfig config;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public StartOnBootAction(TcpTunnelPlugin tunnelPlugin) {
    super("Start on Boot", "Start on Boot", Icons.ICON_START_ON_BOOT);
    this.tunnelPlugin = tunnelPlugin;
    this.config = tunnelPlugin.getTunnelConfig();

    this.selected = config.isStartOnBootEnabled();

    if (selected) {
      scheduleTunnelStart();
    }
  }

  @Override
  public boolean isSelected(AnActionEvent event) {
    return selected;
  }

  @Override
  public void setSelected(AnActionEvent event, boolean state) {
    selected = state;

    config.setStartOnBootEnabled(state);
    config.store();

    String message = state ? "Start on Boot enabled!" : "Start on Boot disabled!";
    ApplicationManager.getApplication().invokeLater(() -> {
//      Notifications.Bus.notify(new Notification(
//          TcpTunnelWindow.NOTIFICATION_ID,
//          "Setting Saved",
//          message,
//          NotificationType.INFORMATION
//      ));

      TcpTunnelWindow.showTemporaryNotification(
          TcpTunnelWindow.NOTIFICATION_ID,
          "Setting Saved",
          message,
          NotificationType.INFORMATION,
          1500
      );

    });

  }

  private void scheduleTunnelStart() {
    scheduler.schedule(() -> {
      TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

      try {
        tunnelPanel.start();

        ApplicationManager.getApplication().invokeLater(() -> {
//          Notifications.Bus.notify(new Notification(
//              TcpTunnelWindow.NOTIFICATION_ID,
//              "Tcp Tunnel Started",
//              "The TCP Tunnel has been started automatically on boot.",
//              NotificationType.INFORMATION
//          ));

          TcpTunnelWindow.showTemporaryNotification(
              TcpTunnelWindow.NOTIFICATION_ID,
              "Tcp Tunnel Started",
              "The TCP Tunnel has been started automatically on boot.",
              NotificationType.INFORMATION,
              3000
          );

        });
      } catch (Exception e) {
        ApplicationManager.getApplication().invokeLater(() -> {
//          Notifications.Bus.notify(new Notification(
//              TcpTunnelWindow.NOTIFICATION_ID,
//              "Error",
//              "Error when starting the TCP Tunnel on boot: " + e.getMessage(),
//              NotificationType.ERROR
//          ));

          TcpTunnelWindow.showTemporaryNotification(
              TcpTunnelWindow.NOTIFICATION_ID,
              "Error",
              "Error when starting the TCP Tunnel on boot: " + e.getMessage(),
              NotificationType.ERROR,
              3000
          );

        });
      }
    }, 3, TimeUnit.SECONDS);
  }

}
