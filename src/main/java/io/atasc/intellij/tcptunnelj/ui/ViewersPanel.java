package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import io.atasc.intellij.tcptunnelj.net.Call;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class ViewersPanel extends JBPanel {
  private Call currentCall;

  /**
   * Whether the viewers wrap long lines, mirroring the "Wrap lines" toolbar toggle. On by default,
   * because a JSON body is one line of tens of thousands of characters and unwrapped it scrolls off
   * to the right — and wrapping is the only way to make it readable that leaves the text alone.
   * <p>
   * Breaking the over-long lines up for display was tried instead and reverted: "Copy" and Ctrl+C in
   * a text area copy what is on screen, so every break landed in the clipboard, cutting JSON in the
   * middle of a token — {@code nul\nl} — and the pasted body would not parse.
   */
  private boolean wrapped = true;

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
    addContextMenu(txtRQ, false);

    txtRS = new JBTextArea();
    txtRS.setEditable(false);
    txtRS.setBackground(UIManager.getColor("Tree.textBackground"));
    addContextMenu(txtRS, true);

    applyWrap();

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

  /**
   * Shows the request as it was captured and the response with its chunked framing undone and its
   * payload decompressed, which is what makes a JSON body readable: the chunk sizes no longer sit
   * in the middle of the text. The wire form stays one context-menu entry away.
   */
  public void view(Call call) {
    if (call == null) {
      return;
    }

    currentCall = call;
    render();
  }

  /**
   * Puts the current call in the two viewers, exactly as {@link Call} hands it over. Nothing is
   * reformatted for display: a selection copied out of a viewer has to paste as valid JSON.
   */
  private void render() {
    if (currentCall == null) {
      return;
    }

    txtRQ.setText(currentCall.getRequestText());
    txtRQ.setCaretPosition(0);

    txtRS.setText(currentCall.getResponseText());
    txtRS.setCaretPosition(0);
  }

  /**
   * Puts {@code text} on the clipboard, unless the call carried nothing to copy.
   */
  private static void copyToClipboard(String text) {
    if (text != null && !text.isEmpty()) {
      CopyPasteManager.getInstance().setContents(new StringSelection(text));
    }
  }

  public void wrap() {
    setWrapped(true);
  }

  public void unwrap() {
    setWrapped(false);
  }

  private void setWrapped(boolean wrapped) {
    this.wrapped = wrapped;
    applyWrap();
  }

  private void applyWrap() {
    txtRQ.setLineWrap(wrapped);
    txtRQ.setWrapStyleWord(wrapped);
    txtRS.setLineWrap(wrapped);
    txtRS.setWrapStyleWord(wrapped);
  }

  public void clear() {
    currentCall = null;
    txtRQ.setText("");
    txtRS.setText("");
  }

  /**
   * "Copy" copies the selection as it is on screen, which is the decoded response. The response menu
   * adds the payload built from the call itself — the body alone, without the headers — and the wire
   * form for when the chunk sizes are the point.
   */
  private void addContextMenu(JBTextArea textArea, boolean isResponse) {
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

    if (isResponse) {
      popupMenu.add(new AbstractAction("Copy body (decoded)") {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (currentCall != null) {
            copyToClipboard(currentCall.getResponseBody());
          }
        }
      });

      popupMenu.add(new AbstractAction("Copy response (decoded)") {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (currentCall != null) {
            copyToClipboard(currentCall.getResponseText());
          }
        }
      });

      popupMenu.add(new AbstractAction("Copy response (raw)") {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (currentCall != null) {
            copyToClipboard(currentCall.getRawResponseText());
          }
        }
      });
    }

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
