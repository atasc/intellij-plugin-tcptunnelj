package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.net.Tunnel;
import io.atasc.intellij.tcptunnelj.net.TunnelException;
import io.atasc.intellij.tcptunnelj.util.PortNumberVerifier;

import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPanel extends JBPanel {
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

  public void start() {
    ExecutorService executor = Executors.newSingleThreadExecutor(); // Use a single-thread executor for thread management

    executor.execute(() -> {
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
        executor.shutdown(); // Shut down the executor to release resources
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

  public void stop() {
    ExecutorService executor = Executors.newSingleThreadExecutor(); // Use a single-thread executor

    executor.execute(() -> {
      try {
        if (tunnel != null) {
          tunnel.stop(); // Stop the tunnel
        }
        // Optional: Remove listeners if necessary
        // tunnellij.removeTunnelListener(list);
      } catch (Exception e) {
        e.printStackTrace(); // Log the exception
      } finally {
        executor.shutdown(); // Ensure the executor is properly shut down
      }
    });

    repaint(); // Update the UI after stopping the tunnel
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
