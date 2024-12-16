package io.atasc.intellij.tcptunnelj.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.Random;

/**
 * @author atasc
 * @since
 */
@Service(Service.Level.PROJECT)
public class TcpTunnelProjectService {

  private static final Logger LOGGER = Logger.getInstance(TcpTunnelProjectService.class);
  private final Random random;

  public TcpTunnelProjectService(Project project) {
    this.random = new Random();
//    LOGGER.info(TcpTunnelPluginBundle.message("projectService", project.getName()));
//    LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  public int getRandomNumber() {
    return random.nextInt(100) + 1;
  }
}
