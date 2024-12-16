package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.project.Project;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;

public class TcpTunnelAppLifecycleListener implements AppLifecycleListener {

  public void appFrameCreated(String[] commandLineArgs) {
    System.out.println("Application frame created.");
  }

  public void appStarting(Project projectFromCommandLine) {
    System.out.println("Application is starting.");
  }

  @Override
  public void projectFrameClosed() {
    System.out.println("All project frames are closed.");
  }

  @Override
  public void projectOpenFailed() {
    System.out.println("A project failed to open.");
  }

  @Override
  public void appClosing() {
    System.out.println("Application is closing.");
    TunnelPlugin.TunnelConfig.store();
  }
}
