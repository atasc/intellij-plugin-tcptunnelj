package io.atasc.intellij.tcptunnel.toolWindow;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnel.TcpTunnelPluginBundle;

import javax.swing.*;

public class TcpTunnelWindow {
  private final ToolWindow toolWindow;
  //private NetTunnelProjectService service;

  public TcpTunnelWindow(ToolWindow toolWindow) {
    //this.service = ServiceManager.getService(toolWindow.getProject(), NetTunnelProjectService.class);
    this.toolWindow = toolWindow;
  }

  public JPanel getContent() {
//    if(this.service==null) {
//      this.service = ServiceManager.getService(toolWindow.getProject(), NetTunnelProjectService.class);
//    }

    JBPanel<JBPanel<?>> panel = new JBPanel<>();
    JBLabel label = new JBLabel(TcpTunnelPluginBundle.message("randomLabel", "?"));

    panel.add(label);
    panel.add(new JButton(TcpTunnelPluginBundle.message("shuffle")) {{
      //addActionListener(e -> label.setText(NetTunnelPluginBundle.message("randomLabel", service.getRandomNumber())));
      addActionListener(e -> label.setText(TcpTunnelPluginBundle.message("randomLabel", 1)));
    }});

    return panel;
  }
}
