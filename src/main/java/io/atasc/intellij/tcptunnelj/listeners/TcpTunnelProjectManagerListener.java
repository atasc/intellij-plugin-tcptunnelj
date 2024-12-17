package io.atasc.intellij.tcptunnelj.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class TcpTunnelProjectManagerListener implements ProjectManagerListener {
//  @Override
//  public void projectOpened(@NotNull Project project) {
//    ProjectManagerListener.super.projectOpened(project);
//  }
//
//  @Override
//  public boolean canCloseProject(@NotNull Project project) {
//    return ProjectManagerListener.super.canCloseProject(project);
//  }

  public TcpTunnelProjectManagerListener() {
    super();
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

  @Override
  public void projectClosed(@NotNull Project project) {
    ProjectManagerListener.super.projectClosed(project);
  }

  @Override
  public void projectClosing(@NotNull Project project) {
    ProjectManagerListener.super.projectClosing(project);
  }

  @Override
  public void projectClosingBeforeSave(@NotNull Project project) {
    ProjectManagerListener.super.projectClosingBeforeSave(project);
  }

  public static void attachListener(Project project, ProjectManagerListener listener) {
    ApplicationManager.getApplication()
        .getMessageBus()
        .connect(project) // Connessione al progetto corrente
        .subscribe(ProjectManager.TOPIC, listener);
  }

}
