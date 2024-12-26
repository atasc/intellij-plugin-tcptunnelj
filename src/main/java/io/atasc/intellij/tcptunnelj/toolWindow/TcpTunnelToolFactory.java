package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;

/**
 * @author atasc
 * @since
 */
public class TcpTunnelToolFactory implements ToolWindowFactory {
  private static final Logger LOGGER = Logger.getInstance(TcpTunnelToolFactory.class);

  static {
    //LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    TunnelPlugin tunnelPlugin = new TunnelPlugin(project);
    //tunnelPlugin.projectOpened();
  }

  @Override
  public boolean shouldBeAvailable(Project project) {
    return true;
  }
}
