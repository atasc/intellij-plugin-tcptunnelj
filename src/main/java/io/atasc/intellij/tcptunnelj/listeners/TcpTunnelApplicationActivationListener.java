package io.atasc.intellij.tcptunnelj.listeners;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author atasc
 * @since
 */
public class TcpTunnelApplicationActivationListener implements ApplicationActivationListener {

  private static final Logger LOGGER = Logger.getInstance(TcpTunnelApplicationActivationListener.class);

  public TcpTunnelApplicationActivationListener() {
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
  public void applicationActivated(IdeFrame ideFrame) {
//    LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
    ApplicationActivationListener.super.applicationDeactivated(ideFrame);
  }

  @Override
  public void delayedApplicationDeactivated(@NotNull Window ideFrame) {
    ApplicationActivationListener.super.delayedApplicationDeactivated(ideFrame);
  }
}
