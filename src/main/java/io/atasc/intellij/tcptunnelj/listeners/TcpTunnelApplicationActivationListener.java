package io.atasc.intellij.tcptunnelj.listeners;

import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;

public class TcpTunnelApplicationActivationListener implements ApplicationActivationListener {

  private static final Logger LOGGER = Logger.getInstance(TcpTunnelApplicationActivationListener.class);

  @Override
  public void applicationActivated(IdeFrame ideFrame) {
//    LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }
}
