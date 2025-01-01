package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnelj.TcpTunnelPluginBundle;

import javax.swing.*;

/**
 * @author atasc
 * @since
 */
public class TcpTunnelWindow {
  public static final String NOTIFICATION_ID="TcpTunnelJ Notifications";
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
}
