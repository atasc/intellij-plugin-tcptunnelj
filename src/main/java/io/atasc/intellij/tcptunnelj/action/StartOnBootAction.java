package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Alarm;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.toolWindow.TcpTunnelWindow;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

/**
 * @author atasc
 * @since
 */
public class StartOnBootAction extends BaseToggleAction {
  /**
   * How long to wait before starting the tunnel by itself, to let the tool window finish building.
   */
  private static final int START_DELAY_MS = 1500;

  private final TcpTunnelConfig config;

  /**
   * Owned by the plugin, so a tool window disposed inside the delay cancels the pending start. The
   * plain {@code ScheduledExecutorService} this replaces was never shut down: one live non-daemon
   * thread per tool window, which also pinned the plugin classloader.
   */
  private final Alarm alarm;

  public StartOnBootAction(TcpTunnelPlugin tunnelPlugin) {
    super("Start on Boot", "Start on Boot", Icons.ICON_START_ON_BOOT);
    this.tunnelPlugin = tunnelPlugin;
    this.config = tunnelPlugin.getTunnelConfig();
    this.alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, tunnelPlugin);

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
    alarm.addRequest(() -> {
      TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();
      if (tunnelPanel == null) {
        // the tool window was disposed inside the delay: nothing left to start
        return;
      }

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
    }, START_DELAY_MS);
  }

}
