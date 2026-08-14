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
  /**
   * Longest line the viewers draw in one piece. Swing draws a line of text as a single run, and
   * past a few thousand characters the glyphs pile up on top of each other and the line turns into
   * an unreadable smear — which is exactly what a 50 KB JSON body on one line looks like, whether
   * it arrives that way or after the chunked framing is undone. Anything longer is therefore broken
   * up <em>for display only</em>; the copy actions below always work off the call itself, so what
   * lands on the clipboard never carries these breaks.
   */
  private static final int MAX_DISPLAY_LINE_LENGTH = 1000;

  private Call currentCall;

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
   * Shows the call as it was captured: both sides exactly as they went over the wire, chunk sizes
   * and all. Undoing the chunked framing and the content coding is left to the copy and save
   * actions — on screen it would only merge a whole JSON body into one unrenderable line, and it
   * would hide the framing this tool exists to show.
   */
  public void view(Call call) {
    if (call == null) {
      return;
    }

    currentCall = call;

    txtRQ.setText(forDisplay(call.getRequestText()));
    txtRQ.setCaretPosition(0);

    txtRS.setText(forDisplay(call.getRawResponseText()));
    txtRS.setCaretPosition(0);
  }

  /**
   * {@code text} with every line longer than {@link #MAX_DISPLAY_LINE_LENGTH} broken up, so that
   * Swing has no over-long line to draw. Text that has no such line is handed back untouched.
   */
  static String forDisplay(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }

    StringBuilder out = null;
    int lineStart = 0;

    for (int i = 0; i <= text.length(); i++) {
      boolean end = i == text.length();
      if (!end && text.charAt(i) != '\n') {
        continue;
      }

      if (i - lineStart > MAX_DISPLAY_LINE_LENGTH) {
        if (out == null) {
          out = new StringBuilder(text.length() + text.length() / MAX_DISPLAY_LINE_LENGTH);
          out.append(text, 0, lineStart);
        }
        for (int from = lineStart; from < i; from += MAX_DISPLAY_LINE_LENGTH) {
          if (from > lineStart) {
            out.append('\n');
          }
          out.append(text, from, Math.min(from + MAX_DISPLAY_LINE_LENGTH, i));
        }
      } else if (out != null) {
        out.append(text, lineStart, i);
      }

      if (!end && out != null) {
        out.append('\n');
      }
      lineStart = i + 1;
    }

    return out == null ? text : out.toString();
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
    currentCall = null;
    txtRQ.setText("");
    txtRS.setText("");
  }

  /**
   * The viewer shows the capture untouched, so copying a selection out of the response would carry
   * the chunk sizes with it. The response menu therefore also offers the decoded forms, which are
   * built from the call rather than from what is on screen.
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
