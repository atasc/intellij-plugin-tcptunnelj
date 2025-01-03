package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import io.atasc.intellij.tcptunnelj.net.Call;
import io.atasc.intellij.tcptunnelj.net.TunnelListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
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

class CallsListSelectionListener implements ListSelectionListener {
  private ViewersPanel viewersPanel;

  public CallsListSelectionListener(ViewersPanel v) {
    this.viewersPanel = v;
  }

  public void valueChanged(ListSelectionEvent e) {
    JBList list = (JBList) e.getSource();
    Call call = (Call) list.getSelectedValue();

    if (call != null) {
      viewersPanel.view(call);
    } else {
      viewersPanel.clear();
    }
  }
}

class ViewersPanel extends JBPanel {
  private boolean removeChunk = true;

  private JBTextArea txtRQ;
  private JBTextArea txtRS;
  private JBScrollPane scrollRQ;
  private JBScrollPane scrollRS;
  private OnePixelSplitter splitPaneLeftRight;

  public ViewersPanel() {
    initComponents();
  }

  protected void initComponents() {
    setLayout(new BorderLayout());
    setBackground(UIManager.getColor("Tree.textBackground"));

    txtRQ = new JBTextArea();
    txtRQ.setEditable(false);
    txtRQ.setBackground(UIManager.getColor("Tree.textBackground"));
    addContextMenu(txtRQ);

    txtRS = new JBTextArea();
    txtRS.setEditable(false);
    txtRS.setBackground(UIManager.getColor("Tree.textBackground"));
    addContextMenu(txtRS);

    scrollRQ = new JBScrollPane(txtRQ);
    scrollRS = new JBScrollPane(txtRS);

    splitPaneLeftRight = new OnePixelSplitter(false, 0.5f); // false for horizontal split
    splitPaneLeftRight.setFirstComponent(scrollRQ);
    splitPaneLeftRight.setSecondComponent(scrollRS);

    add(splitPaneLeftRight, BorderLayout.CENTER);
  }

  public void updateRequest(String data) {
    txtRQ.append(data);
    txtRQ.setCaretPosition(txtRQ.getText().length());
  }

  public void updateResponse(String data) {
    txtRS.append(data);
    txtRS.setCaretPosition(txtRS.getText().length());
  }

  public void scrollViewerToBottom() {
    txtRQ.setCaretPosition(txtRQ.getText().length());
    txtRS.setCaretPosition(txtRS.getText().length());
  }

  public void view(Call call) {
    if (call == null) {
      return;
    }

    ByteArrayOutputStream requestBaos = (ByteArrayOutputStream) call.getOutputLogger();
    if (requestBaos == null) {
      return;
    }

    txtRQ.setText(requestBaos.toString());
    txtRQ.setCaretPosition(0);

    ByteArrayOutputStream responseBaos = ((ByteArrayOutputStream) call.getInputLogger());

    if (responseBaos != null) {
      if (removeChunk) {
        txtRS.setText(Call.removeChunkedEncoding(responseBaos.toString()));
      } else {
        txtRS.setText(responseBaos.toString());
      }
      txtRS.setCaretPosition(0);
    }
  }

  public void wrap() {
    txtRQ.setLineWrap(true);
    txtRQ.setWrapStyleWord(true);
    txtRS.setLineWrap(true);
    txtRS.setWrapStyleWord(true);
  }

  public void unwrap() {
    txtRQ.setLineWrap(false);
    txtRS.setLineWrap(false);
  }

  public void clear() {
    txtRQ.setText("");
    txtRS.setText("");
  }

  private void addContextMenu(JBTextArea textArea) {
    JBPopupMenu popupMenu = new JBPopupMenu();

//    popupMenu.add(new AbstractAction("Cut") {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        textArea.cut();
//      }
//    });

    popupMenu.add(new AbstractAction("Copy") {
      @Override
      public void actionPerformed(ActionEvent e) {
        textArea.copy();
      }
    });

//    popupMenu.add(new AbstractAction("Paste") {
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        textArea.paste();
//      }
//    });

    popupMenu.add(new AbstractAction("Select All") {
      @Override
      public void actionPerformed(ActionEvent e) {
        textArea.selectAll();
      }
    });

    popupMenu.add(new AbstractAction("Clear") {
      @Override
      public void actionPerformed(ActionEvent e) {
        textArea.setText("");
      }
    });

    textArea.setComponentPopupMenu(popupMenu);
  }
}
