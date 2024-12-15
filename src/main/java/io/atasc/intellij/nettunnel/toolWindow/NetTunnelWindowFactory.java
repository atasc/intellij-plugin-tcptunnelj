package io.atasc.intellij.nettunnel.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;

public class NetTunnelWindowFactory implements ToolWindowFactory {

  private static final Logger LOGGER = Logger.getInstance(NetTunnelWindowFactory.class);

  static {
    //LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    NetTunnelWindow netTunnelWindow = new NetTunnelWindow(toolWindow);
    var content = ContentFactory.getInstance().createContent(netTunnelWindow.getContent(), null, false);
    toolWindow.getContentManager().addContent(content);

//    CalendarToolWindowContent toolWindowContent = new CalendarToolWindowContent(toolWindow);
//    Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
//    toolWindow.getContentManager().addContent(content);
  }

  @Override
  public boolean shouldBeAvailable(Project project) {
    return true;
  }

//  public static class NetTunnelWindow {
//
//    private final NetTunnelProjectService service;
//
//    public NetTunnelWindow(ToolWindow toolWindow) {
//      this.service = ServiceManager.getService(toolWindow.getProject(), NetTunnelProjectService.class);
//    }
//
//    public JPanel getContent() {
//      JBPanel<JBPanel<?>> panel = new JBPanel<>();
//      JBLabel label = new JBLabel(NetTunnelPluginBundle.message("randomLabel", "?"));
//
//      panel.add(label);
//      panel.add(new JButton(NetTunnelPluginBundle.message("shuffle")) {{
//        addActionListener(e -> label.setText(NetTunnelPluginBundle.message("randomLabel", service.getRandomNumber())));
//      }});
//
//      return panel;
//    }
//  }
}
