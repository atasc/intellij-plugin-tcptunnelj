package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;

/**
 * @author boruvka/atasc
 * @since
 */
public class CallsPanel extends JBPanel implements TunnelListener {
  public static final int DIVIDER_SIZE = 2;

  private JBList list;
  private DefaultListModel model;
  private ViewersPanel viewers;
  private OnePixelSplitter splitPaneTopBottom;

  public CallsPanel() {
    setBackground(UIManager.getColor("Tree.textBackground"));
    model = new DefaultListModel();
    list = new JBList(model);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    viewers = new ViewersPanel();
    list.addListSelectionListener(new CallsListSelectionListener(viewers));

    list.addKeyListener(new KeyAdapter() {
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
    list.setBackground(UIManager.getColor("Tree.textBackground"));
    list.setVisibleRowCount(3);

    JBPanel topPanel = new JBPanel(new BorderLayout());
    topPanel.add(new JBScrollPane(list), BorderLayout.CENTER);
    JBPanel bottomPanel = new JBPanel(new BorderLayout());
    bottomPanel.add(viewers, BorderLayout.CENTER);

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
  public synchronized void endCall(Call call) {
//    if (list.isVisible()) {
//      list.repaint();
//      viewers.repaint();
//    }

    this.repaintViewers();
  }

  public void repaintViewers(){
    ApplicationManager.getApplication().invokeLater(() -> {
      if (list.isVisible()) {
        list.repaint();
        viewers.repaint();
      }
    });
  }

  public void wrap() {
    viewers.wrap();
  }

  public void unwrap() {
    viewers.unwrap();
  }

  public synchronized void clear() {
    model.clear();
  }

  public synchronized void clearSelected() {
    int index = list.getSelectedIndex();
    if (index != -1) {
      model.removeElementAt(index);
    }
  }

  public int getCallListSize() {
    return list.getModel().getSize();
  }

  public String getCallListToString() {
    String newLine = System.lineSeparator();
    StringBuilder builder = new StringBuilder();
    ListModel model = list.getModel();

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

  private JBTextArea requestTxt;
  private JBTextArea responseTxt;
  private JBScrollPane requestScroll;
  private JBScrollPane responseScroll;
  private OnePixelSplitter splitPaneLeftRight;

  public ViewersPanel() {
    initComponents();
  }

  protected void initComponents() {
    setLayout(new BorderLayout());

    setBackground(UIManager.getColor("Tree.textBackground"));

    requestTxt = new JBTextArea();
    responseTxt = new JBTextArea();

    requestTxt.setEditable(true);
    responseTxt.setEditable(true);

    requestTxt.setBackground(UIManager.getColor("Tree.textBackground"));
    responseTxt.setBackground(UIManager.getColor("Tree.textBackground"));

    requestScroll = new JBScrollPane(requestTxt);
    responseScroll = new JBScrollPane(responseTxt);

    splitPaneLeftRight = new OnePixelSplitter(false, 0.5f); // false for horizontal split
    splitPaneLeftRight.setFirstComponent(requestScroll);
    splitPaneLeftRight.setSecondComponent(responseScroll);

    add(splitPaneLeftRight, BorderLayout.CENTER);
  }

  public void view(Call call) {
    if (call == null) {
      return;
    }

    ByteArrayOutputStream requestBaos = (ByteArrayOutputStream) call.getOutputLogger();
    if (requestBaos == null) {
      return;
    }

    requestTxt.setText(requestBaos.toString());
    requestTxt.setCaretPosition(0);

    ByteArrayOutputStream responseBaos = ((ByteArrayOutputStream) call.getInputLogger());

    if (responseBaos != null) {
      if (removeChunk) {
        responseTxt.setText(Call.removeChunkedEncoding(responseBaos.toString()));
      } else {
        responseTxt.setText(responseBaos.toString());
      }
      responseTxt.setCaretPosition(0);
    }
  }

  public void wrap() {
    requestTxt.setLineWrap(true);
    requestTxt.setWrapStyleWord(true);
    responseTxt.setLineWrap(true);
    responseTxt.setWrapStyleWord(true);
  }

  public void unwrap() {
    requestTxt.setLineWrap(false);
    responseTxt.setLineWrap(false);
  }

  public void clear() {
    requestTxt.setText("");
    responseTxt.setText("");
  }
}
