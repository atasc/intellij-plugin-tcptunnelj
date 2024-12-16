package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;

/**
 * @author atasc
 * @since
 */
public class TcpTunnelWindowFactory implements ToolWindowFactory {

  private static final Logger LOGGER = Logger.getInstance(TcpTunnelWindowFactory.class);

  static {
    //LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    int windowToLoad = 3;

    switch (windowToLoad) {
      case 1 -> {
        TcpTunnelWindow netTunnelWindow = new TcpTunnelWindow(toolWindow);
        var content = ContentFactory.getInstance().createContent(netTunnelWindow.getContent(), null, false);
        toolWindow.getContentManager().addContent(content);
      }
      case 2 -> {
      }
      case 3 -> {
        TunnelPlugin tunnelPlugin = new TunnelPlugin(project);
        var content = ContentFactory.getInstance().createContent(tunnelPlugin.getContent(), null, false);
        toolWindow.getContentManager().addContent(content);
      }
    }

  }

  @Override
  public boolean shouldBeAvailable(Project project) {
    return true;
  }
}
