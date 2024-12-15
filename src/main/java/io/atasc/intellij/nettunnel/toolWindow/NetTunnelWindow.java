package io.atasc.intellij.nettunnel.toolWindow;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.nettunnel.NetTunnelPluginBundle;

import javax.swing.*;

public class NetTunnelWindow {
  private final ToolWindow toolWindow;
  //private NetTunnelProjectService service;

  public NetTunnelWindow(ToolWindow toolWindow) {
    //this.service = ServiceManager.getService(toolWindow.getProject(), NetTunnelProjectService.class);
    this.toolWindow = toolWindow;
  }

  public JPanel getContent() {
//    if(this.service==null) {
//      this.service = ServiceManager.getService(toolWindow.getProject(), NetTunnelProjectService.class);
//    }

    JBPanel<JBPanel<?>> panel = new JBPanel<>();
    JBLabel label = new JBLabel(NetTunnelPluginBundle.message("randomLabel", "?"));

    panel.add(label);
    panel.add(new JButton(NetTunnelPluginBundle.message("shuffle")) {{
      //addActionListener(e -> label.setText(NetTunnelPluginBundle.message("randomLabel", service.getRandomNumber())));
      addActionListener(e -> label.setText(NetTunnelPluginBundle.message("randomLabel", 1)));
    }});

    return panel;
  }
}
