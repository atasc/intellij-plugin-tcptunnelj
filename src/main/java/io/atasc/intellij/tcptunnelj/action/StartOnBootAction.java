package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.toolWindow.TcpTunnelWindow;
import io.atasc.intellij.tcptunnelj.ui.Icons;

/**
 * @author atasc
 * @since
 */
public class StartOnBootAction extends BaseToggleAction {
  private final TcpTunnelConfig config;

  public StartOnBootAction(TcpTunnelPlugin tunnelPlugin) {
    super("Start on Boot", "Start on Boot", Icons.ICON_START_ON_BOOT);
    this.tunnelPlugin = tunnelPlugin;
    this.config = tunnelPlugin.getTunnelConfig();

    this.selected = config.isStartOnBootEnabled();
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
      Notifications.Bus.notify(new Notification(
          TcpTunnelWindow.NOTIFICATION_ID,
          "Setting Saved",
          message,
          NotificationType.INFORMATION
      ));
    });

  }
}
