package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import io.atasc.intellij.tcptunnelj.net.Call;
import io.atasc.intellij.tcptunnelj.net.TunnelListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;

/**
 * @author boruvka/atasc
 * @since
 */
public class CallsPanel extends JBPanel implements TunnelListener {
  public static final int DIVIDER_SIZE = 2;

  private JBList listCalls;
  private DefaultListModel model;
  private ViewersPanel panelViewers;
  private OnePixelSplitter splitPaneTopBottom;

  public CallsPanel() {
    setBackground(UIManager.getColor("Tree.textBackground"));
    model = new DefaultListModel();
    listCalls = new JBList(model);
    listCalls.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    panelViewers = new ViewersPanel();
    listCalls.addListSelectionListener(new CallsListSelectionListener(panelViewers));

    listCalls.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
          clearSelected();
        }
      }
    });

    setLayout(new BorderLayout());
    initComponents();
  }

  protected void initComponents() {
    listCalls.setBackground(UIManager.getColor("Tree.textBackground"));
    listCalls.setVisibleRowCount(3);

    JBPanel topPanel = new JBPanel(new BorderLayout());
    topPanel.add(new JBScrollPane(listCalls), BorderLayout.CENTER);
    JBPanel bottomPanel = new JBPanel(new BorderLayout());
    bottomPanel.add(panelViewers, BorderLayout.CENTER);

    splitPaneTopBottom = new OnePixelSplitter(true, 0.2f); // true for vertical split, 0.2f for top weight
    splitPaneTopBottom.setFirstComponent(topPanel);
    splitPaneTopBottom.setSecondComponent(bottomPanel);

    add(splitPaneTopBottom, BorderLayout.CENTER);
  }

  @Override
  public void tunnelStarted() {
    // nothing yet
  }

  @Override
  public void tunnelStopped() {
    // nothing yet
  }

  @Override
  public synchronized void newCall(Call call) {
    model.addElement(call);
    ApplicationManager.getApplication().invokeLater(() -> {
      this.repaintViewers();
    });
  }

  @Override
  public synchronized void onDataReceived(Call call, String data) {
    ApplicationManager.getApplication().invokeLater(() -> {
//      if (data.startsWith("Request")) { // Esempio di identificazione richiesta/risposta
//        viewers.updateRequest(data);
//      } else {
//        viewers.updateResponse(data);
//      }

      this.repaintViewers();

      //check if call is in list -> if not add it
      int i = model.indexOf(call);
      if (i < 0) {
        this.newCall(call);
      }

      if (call == this.getSelectedCallFromList()) {
        panelViewers.view(call);
        panelViewers.scrollViewerToBottom();
      }

    });
  }

  @Override
  public synchronized void endCall(Call call) {
//    if (list.isVisible()) {
//      list.repaint();
//      viewers.repaint();
//    }

    this.repaintViewers();

    this.scrollToLastCall();
  }

  public void repaintViewers() {
    ApplicationManager.getApplication().invokeLater(() -> {
      if (listCalls.isVisible()) {
        listCalls.repaint();
        panelViewers.repaint();
      }
    });
  }

  public void scrollToLastCall() {
    int lastIndex = model.getSize() - 1;
    if (lastIndex >= 0) {
      listCalls.ensureIndexIsVisible(lastIndex);
    }
  }

  public void wrap() {
    panelViewers.wrap();
  }

  public void unwrap() {
    panelViewers.unwrap();
  }

  public synchronized void clear() {
    model.clear();
  }

  public synchronized void clearSelected() {
    int index = listCalls.getSelectedIndex();
    if (index != -1) {
      model.removeElementAt(index);
    }
  }

  public Call getSelectedCallFromList() {
    Call call = (Call) listCalls.getSelectedValue();
//    int index = listCalls.getSelectedIndex();
//    if (index != -1) {
//      model.removeElementAt(index);
//    }

    return call;
  }

  public int getCallListSize() {
    return listCalls.getModel().getSize();
  }

  public String getCallListToString() {
    String newLine = System.lineSeparator();
    StringBuilder builder = new StringBuilder();
    ListModel model = listCalls.getModel();

    int t = model.getSize();
    for (int i = 0; i < model.getSize(); i++) {
      Call call = (Call) model.getElementAt(i);
      String callString = "/************************* (" + (i + 1) + "/" + t + ") *************************/" + newLine + newLine;
      callString += this.getCallString(call) + newLine;
      callString += "/**************************************************/" + newLine + newLine;
      builder.append(callString);
    }

    return builder.toString();
  }

  public String getCallString(Call call) {
    StringBuilder requestTxt = new StringBuilder();
    StringBuilder responseTxt = new StringBuilder();
    boolean removeChunk = true;

    boolean asBytes = false;
    if (call == null) {
      return "";
    }

    ByteArrayOutputStream requestBaos = (ByteArrayOutputStream) call.getOutputLogger();
    if (requestBaos == null) {
      return "";
    }

    if (!asBytes) {
      requestTxt.append(requestBaos.toString());

    } else {
      byte[] bytes = requestBaos.toByteArray();
      for (byte b : bytes) {
        String s = Integer.toHexString(b).toUpperCase();
        if (s.length() == 1) {
          s = "0" + s;
        }
        requestTxt.append(s);
      }
    }

    ByteArrayOutputStream responseBaos = ((ByteArrayOutputStream) call.getInputLogger());

    if (removeChunk) {
      responseTxt.append(Call.removeChunkedEncoding(responseBaos.toString()));
    } else {
      responseTxt.append(responseBaos.toString());
    }

    String newLine = System.lineSeparator();
    String rq = requestTxt.toString();
    String rs = responseTxt.toString();

    String r = "CALL: " + newLine + call.toString() + newLine + newLine;
    r += "REQUEST:" + newLine + rq;
    r += "RESPONSE:" + newLine + rs;

    return r;
  }
}
