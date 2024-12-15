package io.atasc.intellij.nettunnel.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import io.atasc.intellij.nettunnel.tunnellij.TunnelPlugin;

public class NetTunnelToolFactory implements ToolWindowFactory {

  private static final Logger LOGGER = Logger.getInstance(NetTunnelToolFactory.class);

  static {
    //LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    TunnelPlugin tunnelPlugin = new TunnelPlugin(project);
    tunnelPlugin.projectOpened();
  }

  @Override
  public boolean shouldBeAvailable(Project project) {
    return true;
  }
}
