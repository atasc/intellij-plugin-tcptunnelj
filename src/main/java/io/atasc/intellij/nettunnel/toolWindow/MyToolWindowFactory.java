package io.atasc.intellij.nettunnel.toolWindow;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.content.ContentFactory;
import io.atasc.intellij.nettunnel.NetTunnelPluginBundle;
import io.atasc.intellij.nettunnel.services.MyProjectService;

import javax.swing.*;

public class MyToolWindowFactory implements ToolWindowFactory {

  private static final Logger LOGGER = Logger.getInstance(MyToolWindowFactory.class);

  static {
    LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    MyToolWindow myToolWindow = new MyToolWindow(toolWindow);
    var content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false);
    toolWindow.getContentManager().addContent(content);
  }

  @Override
  public boolean shouldBeAvailable(Project project) {
    return true;
  }

  public static class MyToolWindow {

    private final MyProjectService service;

    public MyToolWindow(ToolWindow toolWindow) {
      this.service = ServiceManager.getService(toolWindow.getProject(), MyProjectService.class);
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
}
