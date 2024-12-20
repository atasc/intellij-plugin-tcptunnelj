package io.atasc.intellij.tcptunnelj.tunnellij.ui;

import io.atasc.intellij.tcptunnelj.tunnellij.net.Call;
import io.atasc.intellij.tcptunnelj.tunnellij.net.TunnelListener;

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
public class CallsPanel extends JPanel implements TunnelListener {
  private JList list;
  private DefaultListModel model;
  private ViewersPanel viewers;
  private JSplitPane splitPaneTopBottom;

  public static final int DIVIDER_SIZE = 2;

  public CallsPanel() {
    setBackground(UIManager.getColor("Tree.textBackground"));
    model = new DefaultListModel();
    list = new JList(model);
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

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(new JScrollPane(list), BorderLayout.CENTER);
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(viewers, BorderLayout.CENTER);

    splitPaneTopBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPaneTopBottom.setDividerLocation(0.20d);
    splitPaneTopBottom.setResizeWeight(0.20d);
    splitPaneTopBottom.setDividerSize(DIVIDER_SIZE);

    splitPaneTopBottom.add(topPanel, JSplitPane.TOP);
    splitPaneTopBottom.add(bottomPanel, JSplitPane.BOTTOM);

    add(splitPaneTopBottom, BorderLayout.CENTER);
  }

  public void tunnelStarted() {
    // nothing yet
  }

  public void tunnelStopped() {
    // nothing yet
  }

  public synchronized void newCall(Call call) {
    model.addElement(call);
  }

  public synchronized void endCall(Call call) {
    if (list.isVisible()) {
      list.repaint();
      viewers.repaint();
    }

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

}

class CallsListSelectionListener implements ListSelectionListener {
  private ViewersPanel viewersPanel;

  public CallsListSelectionListener(ViewersPanel v) {
    this.viewersPanel = v;
  }

  public void valueChanged(ListSelectionEvent e) {
    JList list = (JList) e.getSource();
    Call call = (Call) list.getSelectedValue();

    if (call != null) {
      viewersPanel.view(call);
    } else {
      viewersPanel.clear();
    }

  }
}

class ViewersPanel extends JPanel {
  private boolean removeChunk = true;

  private JTextArea requestTxt;
  private JTextArea responseTxt;
  private JScrollPane requestScroll;
  private JScrollPane responseScroll;
  private JSplitPane splitPaneLeftRight;

  public ViewersPanel() {
    initComponents();
  }

  protected void initComponents() {
    setLayout(new BorderLayout());

    setBackground(UIManager.getColor("Tree.textBackground"));

    requestTxt = new JTextArea();
    responseTxt = new JTextArea();

    requestTxt.setEditable(true);
    responseTxt.setEditable(true);

    requestTxt.setBackground(UIManager.getColor("Tree.textBackground"));
    responseTxt.setBackground(UIManager.getColor("Tree.textBackground"));

    requestScroll = new JScrollPane(requestTxt);
    responseScroll = new JScrollPane(responseTxt);

    splitPaneLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPaneLeftRight.setDividerSize(CallsPanel.DIVIDER_SIZE);
    splitPaneLeftRight.setDividerLocation(0.50d);
    splitPaneLeftRight.setResizeWeight(0.50d);

    splitPaneLeftRight.add(requestScroll, JSplitPane.LEFT);
    splitPaneLeftRight.add(responseScroll, JSplitPane.RIGHT);

    add(splitPaneLeftRight, BorderLayout.CENTER);
  }

  public void view(Call call) {

    boolean asBytes = false;
    if (call == null) {
      return;
    }

    ByteArrayOutputStream requestBaos = (ByteArrayOutputStream) call.getOutputLogger();
    if (requestBaos == null) {
      return;
    }

    requestTxt.setText("");

    if (!asBytes) {
      requestTxt.setText(requestBaos.toString());

    } else {
      //request text
      byte[] bytes = requestBaos.toByteArray();
      for (int i = 0; i < bytes.length; i++) {
        byte b = bytes[i];
        String s = Integer.toHexString(b).toUpperCase();
        if (s.length() == 1) {
          s = "0" + s;
        }
        requestTxt.append(s);
      }
    }
    requestTxt.setCaretPosition(0);

    //response text
    ByteArrayOutputStream responseBaos = ((ByteArrayOutputStream) call.getInputLogger());

    if (responseBaos == null) {
      return;
    }

    if (removeChunk) {
      responseTxt.setText(
          removeChunkedEncoding(responseBaos.toString())
      );
    } else {
      responseTxt.setText(responseBaos.toString());
    }
    responseTxt.setCaretPosition(0);
  }

  public String removeChunkedEncoding(String response) {
    // Usa una regex per identificare i chunk (numeri esadecimali seguiti da newline)
    return response.replaceAll("(?m)^[0-9a-fA-F]+\\r?\\n", "");
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
