package io.atasc.intellij.tcptunnelj.tunnellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.tunnellij.net.Call;
import io.atasc.intellij.tcptunnelj.tunnellij.net.Tunnel;
import io.atasc.intellij.tcptunnelj.tunnellij.net.TunnelException;
import io.atasc.intellij.tcptunnelj.tunnellij.net.TunnelListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author boruvka/atasc
 * @since
 */
public class TunnelPanel extends JPanel {

  private CallsPanel list;

  private ControlPanel control;

  private Tunnel tunnel;

  private PortNumberVerifier portNumberVerifier;

  public TunnelPanel() {

    setLayout(new BorderLayout());

    list = new CallsPanel();
    control = new ControlPanel();
    add(list, BorderLayout.CENTER);
    add(control, BorderLayout.SOUTH);

    portNumberVerifier = new PortNumberVerifier();
  }

  public void start() throws Exception {
    Runnable r = new Runnable() {

      public void run() {
        tunnel = new Tunnel(control.getSrcPort(),
            control.getDestPort(), control.getDestHost());
        try {
          tunnel.addTunnelListener(list);
          tunnel.addTunnelListener(control);
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
    list.clear();
  }

  public void clearSelected() {
    list.clearSelected();
  }

  public void wrap() {
    list.wrap();
  }

  public void unwrap() {
    list.unwrap();
  }

  private boolean isRunning = false;

  public boolean isRunning() {
    return isRunning;
  }

  class ControlPanel extends JPanel implements TunnelListener {

    private JPanel subPanelAddress;

    private JTextField srcPort;

    private JTextField destHost;

    private JTextField destPort;

    public ControlPanel() {
      super();
      initComponents();
    }

    protected void initComponents() {
      setLayout(new BorderLayout());

      subPanelAddress = new JPanel();
      subPanelAddress.setBorder(new TitledBorder("Properties"));

      srcPort = new JTextField(TunnelPlugin.TunnelConfig.getSourcePort());
      srcPort.setInputVerifier(portNumberVerifier);
      srcPort.setHorizontalAlignment(JTextField.RIGHT);
      srcPort.setColumns(5);

      destPort = new JTextField(TunnelPlugin.TunnelConfig
          .getDestinationPort());
      destPort.setInputVerifier(portNumberVerifier);
      destPort.setHorizontalAlignment(JTextField.RIGHT);
      destPort.setColumns(5);

      destHost = new JTextField(TunnelPlugin.TunnelConfig
          .getDestinationString());
      destHost.setHorizontalAlignment(JTextField.RIGHT);
      destHost.setColumns(24);

      subPanelAddress.add(new JLabel("from"));
      subPanelAddress.add(srcPort);
      subPanelAddress.add(new JLabel("to"));
      subPanelAddress.add(destHost);
      subPanelAddress.add(new JLabel(":"));
      subPanelAddress.add(destPort);

      add(subPanelAddress, BorderLayout.SOUTH);
    }

    public void setControlPanelEditable(boolean b) {
      TunnelPlugin.TunnelConfig.setDestinationPort(destHost.getText());
      TunnelPlugin.TunnelConfig.setSourcePort(destPort.getText());
      TunnelPlugin.TunnelConfig.setSourcePort(srcPort.getText());
      TunnelPlugin.TunnelConfig.store();

//      TunnelPlugin.PROPERTIES.put(TunnelPlugin.TunnelConfig.DST_HOST,
//          destHost.getText());
//      TunnelPlugin.PROPERTIES.put(TunnelPlugin.TunnelConfig.DST_PORT,
//          destPort.getText());
//      TunnelPlugin.PROPERTIES.put(TunnelPlugin.TunnelConfig.SRC_PORT,
//          srcPort.getText());

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
    String text = ((JTextField) input).getText();
    try {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

}
