package io.atasc.intellij.tcptunnelj.listeners;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TcpTunnelAppLifecycleListener implements AppLifecycleListener {

  public TcpTunnelAppLifecycleListener() {
    super();
  }

  public void appFrameCreated(String[] commandLineArgs) {
    System.out.println("Application frame created.");
  }

  public void appStarting(Project projectFromCommandLine) {
    System.out.println("Application is starting.");
  }

  @Override
  public void appFrameCreated(@NotNull List<String> commandLineArgs) {
    AppLifecycleListener.super.appFrameCreated(commandLineArgs);
  }

  @Override
  public void welcomeScreenDisplayed() {
    AppLifecycleListener.super.welcomeScreenDisplayed();
  }

//  @Override
//  public void appStarted() {
//    AppLifecycleListener.super.appStarted();
//  }

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
    //TunnelPlugin.TunnelConfig.store();
  }

  @Override
  public void appWillBeClosed(boolean isRestart) {
    AppLifecycleListener.super.appWillBeClosed(isRestart);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
}
