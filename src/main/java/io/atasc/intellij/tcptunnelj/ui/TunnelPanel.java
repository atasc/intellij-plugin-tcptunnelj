package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.net.Tunnel;
import io.atasc.intellij.tcptunnelj.net.TunnelException;
import io.atasc.intellij.tcptunnelj.util.PortNumberVerifier;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPanel extends JBPanel implements Disposable {
  private CallsPanel panelCalls;
  private ControlPanel panelControl;
  private Tunnel tunnel;
  private PortNumberVerifier portNumberVerifier;

  public TcpTunnelConfig getTunnelConfig() {
    return tunnelConfig;
  }

  private TcpTunnelConfig tunnelConfig;
  private boolean isRunning = false;

  public boolean isRunning() {
    return isRunning;
  }

  public void setRunning(boolean running) {
    isRunning = running;
  }

  public TunnelPanel(TcpTunnelConfig tunnelConfig) {
    this.tunnelConfig = tunnelConfig;

    setLayout(new BorderLayout());

    panelCalls = new CallsPanel();
    panelControl = new ControlPanel(this);

    add(panelCalls, BorderLayout.CENTER);
    add(panelControl, BorderLayout.SOUTH);

    portNumberVerifier = new PortNumberVerifier();
  }

  /**
   * Opens the tunnel on a pooled thread — {@link Tunnel#start()} blocks in an accept loop, so it can
   * never run on the EDT. The platform's pool is used rather than an executor of our own: those were
   * created per call and their threads, being neither daemon nor shut down on every path, kept the
   * plugin's classloader alive.
   */
  public void start() {
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        tunnel = new Tunnel(panelControl.getSrcPort(),
            panelControl.getDestPort(),
            panelControl.getDestHost());

        tunnel.addTunnelListener(panelCalls);
        tunnel.addTunnelListener(panelControl);

        tunnel.start(); // Start the tunnel
      } catch (TunnelException e) {
        showError(e); // Display error to the user
        e.printStackTrace();
      } finally {
        if (tunnel != null) {
          try {
            tunnel.stop(); // Ensure tunnel is stopped in case of failure
          } catch (Exception stopException) {
            stopException.printStackTrace();
          }
        }
      }
    });
  }

  public void showError(Exception e) {
    ApplicationManager.getApplication().invokeLater(() -> {
      Messages.showMessageDialog(
          "Error starting server: " + e.getMessage(),
          "Error",
          Messages.getErrorIcon()
      );
    });
  }

  /**
   * Closes the tunnel here and now, rather than on a thread of its own: {@link Tunnel#stop()} only
   * closes sockets, and {@link #dispose()} needs them shut before the panel goes away.
   */
  public void stop() {
    try {
      if (tunnel != null) {
        tunnel.stop();
      }
    } catch (Exception e) {
      e.printStackTrace(); // Log the exception
    }

    repaint(); // Update the UI after stopping the tunnel
  }

  /**
   * Closes the tunnel and hands the disposal on to the calls panel, which owns the editors.
   */
  @Override
  public void dispose() {
    stop();
    panelCalls.dispose();
  }

  public void clear() {
    panelCalls.clear();
  }

  public void clearSelected() {
    panelCalls.clearSelected();
  }

  public int getCallListSize() {
    return panelCalls.getCallListSize();
  }

  public int getSelectedCallsSize() {
    return panelCalls.getSelectedCallsSize();
  }

  public List<String> copySelectedRequestsToClipboard() {
    return panelCalls.copySelectedRequestsToClipboard();
  }

  public JComponent getCallsListComponent() {
    return panelCalls.getCallsListComponent();
  }

  public String getCallListToString() {
    return panelCalls.getCallListToString();
  }

  public void wrap() {
    panelCalls.wrap();
  }

  public void unwrap() {
    panelCalls.unwrap();
  }
}
