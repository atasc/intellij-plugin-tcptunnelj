package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.net.Call;
import io.atasc.intellij.tcptunnelj.net.Tunnel;
import io.atasc.intellij.tcptunnelj.net.TunnelException;
import io.atasc.intellij.tcptunnelj.net.TunnelListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPanel extends JBPanel {
  private TcpTunnelConfig tunnelConfig;

  private CallsPanel panelCalls;
  private ControlPanel panelControl;
  private Tunnel tunnel;
  private PortNumberVerifier portNumberVerifier;

  public TunnelPanel(TcpTunnelConfig tunnelConfig) {
    this.tunnelConfig = tunnelConfig;

    setLayout(new BorderLayout());

    panelCalls = new CallsPanel();
    panelControl = new ControlPanel(tunnelConfig);

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

  private boolean isRunning = false;

  public boolean isRunning() {
    return isRunning;
  }

  class ControlPanel extends JBPanel implements TunnelListener {
    TcpTunnelConfig tunnelConfig;
    private JBPanel panelAddress;
    private JBTextField txtSrcPort;
    private JBTextField txtDestHost;
    private JBTextField txtDestPort;

    public ControlPanel(TcpTunnelConfig tunnelConfig) {
      super();
      this.tunnelConfig = tunnelConfig;
      initComponents();
    }

    protected void initComponents() {
      setLayout(new BorderLayout());

      panelAddress = new JBPanel();
      panelAddress.setBorder(new TitledBorder("Properties"));

      txtSrcPort = new JBTextField(this.tunnelConfig.getSourcePort());
      txtSrcPort.setInputVerifier(portNumberVerifier);
      txtSrcPort.setHorizontalAlignment(JBTextField.RIGHT);
      txtSrcPort.setColumns(5);

      txtDestHost = new JBTextField(this.tunnelConfig.getDestinationString());
      txtDestHost.setHorizontalAlignment(JBTextField.RIGHT);
      txtDestHost.setColumns(24);

      txtDestPort = new JBTextField(this.tunnelConfig.getDestinationPort());
      txtDestPort.setInputVerifier(portNumberVerifier);
      txtDestPort.setHorizontalAlignment(JBTextField.RIGHT);
      txtDestPort.setColumns(5);

      this.tunnelConfig.setSourcePort(this.tunnelConfig.getSourcePort());
      this.tunnelConfig.setDestinationString(this.tunnelConfig.getDestinationString());
      this.tunnelConfig.setDestinationPort(this.tunnelConfig.getDestinationPort());

      panelAddress.add(new JBLabel("from port"));
      panelAddress.add(txtSrcPort);
      panelAddress.add(new JBLabel("to"));
      panelAddress.add(txtDestHost);
      panelAddress.add(new JBLabel(":"));
      panelAddress.add(txtDestPort);

      add(panelAddress, BorderLayout.SOUTH);
    }

    public void setControlPanelEditable(boolean b) {
      this.tunnelConfig.setSourcePort(txtSrcPort.getText());
      this.tunnelConfig.setDestinationString(txtDestHost.getText());
      this.tunnelConfig.setDestinationPort(txtDestPort.getText());
      this.tunnelConfig.store();

      txtSrcPort.setEditable(b);
      txtDestHost.setEditable(b);
      txtDestPort.setEditable(b);

      txtSrcPort.setEnabled(b);
      txtDestHost.setEnabled(b);
      txtDestPort.setEnabled(b);
    }

    public int getSrcPort() {
      return Integer.parseInt(txtSrcPort.getText());
    }

    public int getDestPort() {
      return Integer.parseInt(txtDestPort.getText());
    }

    public String getDestHost() {
      return txtDestHost.getText();
    }

    @Override
    public void newCall(Call call) {
      //
    }

    @Override
    public void endCall(Call call) {
      //
    }

    @Override
    public void tunnelStarted() {
      isRunning = true;
      setControlPanelEditable(false);
    }

    @Override
    public void tunnelStopped() {
      isRunning = false;
      setControlPanelEditable(true);
    }

  }

}

class PortNumberVerifier extends InputVerifier {

  public boolean verify(JComponent input) {
    String text = ((JBTextField) input).getText();
    try {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

}
