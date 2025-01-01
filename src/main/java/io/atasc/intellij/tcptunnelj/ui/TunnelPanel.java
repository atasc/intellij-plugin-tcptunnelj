package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import io.atasc.intellij.tcptunnelj.TunnelConfig;
import io.atasc.intellij.tcptunnelj.net.Call;
import io.atasc.intellij.tcptunnelj.net.Tunnel;
import io.atasc.intellij.tcptunnelj.net.TunnelException;
import io.atasc.intellij.tcptunnelj.net.TunnelListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPanel extends JBPanel {
  private TunnelConfig tunnelConfig;

  private CallsPanel callsPanel;
  private ControlPanel controlPanel;
  private Tunnel tunnel;
  private PortNumberVerifier portNumberVerifier;

  public TunnelPanel(TunnelConfig tunnelConfig) {
    this.tunnelConfig = tunnelConfig;

    setLayout(new BorderLayout());

    callsPanel = new CallsPanel();
    controlPanel = new ControlPanel(tunnelConfig);

    add(callsPanel, BorderLayout.CENTER);
    add(controlPanel, BorderLayout.SOUTH);

    portNumberVerifier = new PortNumberVerifier();
  }

  public void start() throws Exception {
    Runnable r = new Runnable() {

      public void run() {
        tunnel = new Tunnel(controlPanel.getSrcPort(),
            controlPanel.getDestPort(), controlPanel.getDestHost());
        try {
          tunnel.addTunnelListener(callsPanel);
          tunnel.addTunnelListener(controlPanel);
          tunnel.start();

        } catch (TunnelException e) {
          showError(e);
          e.printStackTrace();
        }
      }

    };

    Thread t = new Thread(r);
    t.start();

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

  public void stop() throws Exception {
    Runnable r = new Runnable() {
      public void run() {
        tunnel.stop();
        // tunnellij.removeTunnelListener(list);
      }
    };
    Thread t = new Thread(r);
    t.start();
    repaint();
  }

  public void clear() {
    callsPanel.clear();
  }

  public void clearSelected() {
    callsPanel.clearSelected();
  }

  public int getCallListSize() {
    return callsPanel.getCallListSize();
  }

  public String getCallListToString() {
    return callsPanel.getCallListToString();
  }

  public void wrap() {
    callsPanel.wrap();
  }

  public void unwrap() {
    callsPanel.unwrap();
  }

  private boolean isRunning = false;

  public boolean isRunning() {
    return isRunning;
  }

  class ControlPanel extends JBPanel implements TunnelListener {
    TunnelConfig tunnelConfig;

    private JBPanel subPanelAddress;

    private JBTextField srcPort;

    private JBTextField destHost;

    private JBTextField destPort;

    public ControlPanel(TunnelConfig tunnelConfig) {
      super();
      this.tunnelConfig = tunnelConfig;
      initComponents();
    }

    protected void initComponents() {
      setLayout(new BorderLayout());

      subPanelAddress = new JBPanel();
      subPanelAddress.setBorder(new TitledBorder("Properties"));

      srcPort = new JBTextField(this.tunnelConfig.getSourcePort());
      srcPort.setInputVerifier(portNumberVerifier);
      srcPort.setHorizontalAlignment(JBTextField.RIGHT);
      srcPort.setColumns(5);

      destHost = new JBTextField(this.tunnelConfig.getDestinationString());
      destHost.setHorizontalAlignment(JBTextField.RIGHT);
      destHost.setColumns(24);

      destPort = new JBTextField(this.tunnelConfig.getDestinationPort());
      destPort.setInputVerifier(portNumberVerifier);
      destPort.setHorizontalAlignment(JBTextField.RIGHT);
      destPort.setColumns(5);

      this.tunnelConfig.setSourcePort(this.tunnelConfig.getSourcePort());
      this.tunnelConfig.setDestinationString(this.tunnelConfig.getDestinationString());
      this.tunnelConfig.setDestinationPort(this.tunnelConfig.getDestinationPort());

      subPanelAddress.add(new JBLabel("from port"));
      subPanelAddress.add(srcPort);
      subPanelAddress.add(new JBLabel("to"));
      subPanelAddress.add(destHost);
      subPanelAddress.add(new JBLabel(":"));
      subPanelAddress.add(destPort);

      add(subPanelAddress, BorderLayout.SOUTH);
    }

    public void setControlPanelEditable(boolean b) {
      this.tunnelConfig.setSourcePort(srcPort.getText());
      this.tunnelConfig.setDestinationString(destHost.getText());
      this.tunnelConfig.setDestinationPort(destPort.getText());
      this.tunnelConfig.store();

      srcPort.setEditable(b);
      destHost.setEditable(b);
      destPort.setEditable(b);

      srcPort.setEnabled(b);
      destHost.setEnabled(b);
      destPort.setEnabled(b);
    }

    public int getSrcPort() {
      return Integer.parseInt(srcPort.getText());
    }

    public int getDestPort() {
      return Integer.parseInt(destPort.getText());
    }

    public String getDestHost() {
      return destHost.getText();
    }

    public void newCall(Call call) {
      //
    }

    public void endCall(Call call) {
      //
    }

    public void tunnelStarted() {
      isRunning = true;
      setControlPanelEditable(false);
    }

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
