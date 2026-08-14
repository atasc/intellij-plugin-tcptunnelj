package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnelj.TcpTunnelPluginBundle;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

/**
 * @author atasc
 * @since
 */
public class TcpTunnelWindow {
  public static final String NOTIFICATION_ID = "TcpTunnelJ Notifications";
  private final ToolWindow toolWindow;
  //private TcpTunnelProjectService service;

  public TcpTunnelWindow(ToolWindow toolWindow) {
    //this.service = ServiceManager.getService(toolWindow.getProject(), TcpTunnelProjectService.class);
    this.toolWindow = toolWindow;
  }

  public JBPanel getContent() {
//    if(this.service==null) {
//      this.service = ServiceManager.getService(toolWindow.getProject(), TcpTunnelProjectService.class);
//    }

    JBPanel<JBPanel<?>> panel = new JBPanel<>();
    JBLabel label = new JBLabel(TcpTunnelPluginBundle.message("randomLabel", "?"));

    panel.add(label);
    panel.add(new JButton(TcpTunnelPluginBundle.message("shuffle")) {{
      //addActionListener(e -> label.setText(TcpTunnelPluginBundle.message("randomLabel", service.getRandomNumber())));
      addActionListener(e -> label.setText(TcpTunnelPluginBundle.message("randomLabel", 1)));
    }});

    return panel;
  }

  /**
   * Shows a balloon and takes it down again after {@code timeoutMillis}.
   * <p>
   * The scheduling goes through the platform's EDT scheduler, which already exists and runs the task
   * on the EDT. Every call used to spin up a {@code ScheduledExecutorService} of its own and never
   * shut it down, so each notification left a live thread behind holding the plugin's classloader.
   */
  public static void showTemporaryNotification(String groupId, String title, String content,
                                               NotificationType type, int timeoutMillis) {
    Notification notification = new Notification(groupId, title, content, type);
    Notifications.Bus.notify(notification);

    EdtScheduledExecutorService.getInstance()
        .schedule(notification::expire, timeoutMillis, TimeUnit.MILLISECONDS);
  }
}
