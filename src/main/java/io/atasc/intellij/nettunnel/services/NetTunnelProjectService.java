package io.atasc.intellij.nettunnel.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.atasc.intellij.nettunnel.NetTunnelPluginBundle;

import java.util.Random;

@Service(Service.Level.PROJECT)
public class NetTunnelProjectService {

  private static final Logger LOGGER = Logger.getInstance(NetTunnelProjectService.class);
  private final Random random;

  public NetTunnelProjectService(Project project) {
    this.random = new Random();
//    LOGGER.info(NetTunnelPluginBundle.message("projectService", project.getName()));
//    LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  public int getRandomNumber() {
    return random.nextInt(100) + 1;
  }
}
