package io.atasc.intellij.nettunnel.toolWindow;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.nettunnel.NetTunnelPluginBundle;
import io.atasc.intellij.nettunnel.services.NetTunnelProjectService;

import javax.swing.*;

public class NetTunnelWindow {

  private final NetTunnelProjectService service;

  public NetTunnelWindow(ToolWindow toolWindow) {
    this.service = ServiceManager.getService(toolWindow.getProject(), NetTunnelProjectService.class);
  }

  public JPanel getContent() {
    JBPanel<JBPanel<?>> panel = new JBPanel<>();
    JBLabel label = new JBLabel(NetTunnelPluginBundle.message("randomLabel", "?"));

    panel.add(label);
    panel.add(new JButton(NetTunnelPluginBundle.message("shuffle")) {{
      addActionListener(e -> label.setText(NetTunnelPluginBundle.message("randomLabel", service.getRandomNumber())));
    }});

    return panel;
  }
}
