package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import io.atasc.intellij.tcptunnelj.net.Call;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;

public class ViewersPanel extends JBPanel {
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
