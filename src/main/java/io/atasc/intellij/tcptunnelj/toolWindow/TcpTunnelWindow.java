package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnelj.TcpTunnelPluginBundle;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

  public static void showTemporaryNotification(String groupId, String title, String content,
                                               NotificationType type, int timeoutSeconds) {
    Notification notification = new Notification(groupId, title, content, type);
    Notifications.Bus.notify(notification);

    // Utilizzare uno scheduler per chiudere la notifica dopo il timeout
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.schedule(() -> ApplicationManager.getApplication()
        .invokeLater(notification::expire), timeoutSeconds, TimeUnit.MILLISECONDS);
  }
}
