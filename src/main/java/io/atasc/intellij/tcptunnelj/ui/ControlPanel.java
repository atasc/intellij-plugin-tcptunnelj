package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import io.atasc.intellij.tcptunnelj.TcpTunnelConfig;
import io.atasc.intellij.tcptunnelj.net.Call;
import io.atasc.intellij.tcptunnelj.net.TunnelListener;
import io.atasc.intellij.tcptunnelj.util.PortNumberVerifier;

import javax.swing.border.TitledBorder;
import java.awt.*;

public class ControlPanel extends JBPanel implements TunnelListener {
  TunnelPanel tunnelPanel;
  TcpTunnelConfig tunnelConfig;
  private JBPanel panelAddress;
  private JBTextField txtSrcPort;
  private JBTextField txtDestHost;
  private JBTextField txtDestPort;

  public ControlPanel(TunnelPanel tunnelPanel) {
    super();
    this.tunnelPanel = tunnelPanel;
    this.tunnelConfig = tunnelPanel.getTunnelConfig();
    initComponents();
  }

  protected void initComponents() {
    PortNumberVerifier portNumberVerifier = new PortNumberVerifier();
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
  public void onDataReceived(Call call, String data) {

  }

  @Override
  public void tunnelStarted() {
    //isRunning = true;
    this.tunnelPanel.setRunning(true);
    setControlPanelEditable(false);
  }

  @Override
  public void tunnelStopped() {
    //isRunning = false;
    this.tunnelPanel.setRunning(false);
    setControlPanelEditable(true);
  }
}
