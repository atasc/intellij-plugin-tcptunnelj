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
   * Longest line the viewers draw in one piece when line wrap is off. With wrap off Swing lays a
   * line out as a single run whose width the drawing pipeline addresses with 16-bit coordinates, so
   * somewhere past 32767 pixels the glyph positions overflow and the line turns into a smear — and
   * laying out a 50 KB JSON body costs that on every repaint. 4000 characters is roughly 28000
   * pixels at the default font, comfortably inside the limit.
   * <p>
   * This is a fallback for the unwrapped view only: {@link #wrapped} is on by default, and with
   * wrap on Swing breaks the lines itself and the viewer shows the response untouched.
   */
  static final int MAX_DISPLAY_LINE_LENGTH = 4000;

  private Call currentCall;

  /**
   * Whether the viewers wrap long lines, mirroring the "Wrap lines" toolbar toggle. When they do,
   * nothing has to be broken up for display and a selection copied out of the viewer is exactly
   * what came over the wire.
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
   * Puts the current call in the two viewers. Called again on every wrap toggle, because whether a
   * long line has to be broken up depends on it.
   */
  private void render() {
    if (currentCall == null) {
      return;
    }

    txtRQ.setText(forDisplay(currentCall.getRequestText(), wrapped));
    txtRQ.setCaretPosition(0);

    txtRS.setText(forDisplay(currentCall.getResponseText(), wrapped));
    txtRS.setCaretPosition(0);
  }

  /**
   * {@code text} as the viewers should show it: untouched when they wrap, since Swing then breaks
   * the lines itself, and broken up at {@link #MAX_DISPLAY_LINE_LENGTH} when they do not.
   */
  static String forDisplay(String text, boolean wrapped) {
    if (wrapped) {
      return text == null ? "" : text;
    }
    return forDisplay(text);
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
    setWrapped(true);
  }

  public void unwrap() {
    setWrapped(false);
  }

  /**
   * Turning the wrap off means the over-long lines now have to be broken up by hand, and turning it
   * back on means those breaks have to go, so the current call is drawn again either way.
   */
  private void setWrapped(boolean wrapped) {
    if (this.wrapped == wrapped) {
      return;
    }

    this.wrapped = wrapped;
    applyWrap();
    render();
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
   * What is on screen carries the line breaks {@link #forDisplay(String)} added, so the response
   * menu offers the payload built from the call itself — the body alone, or headers and body — plus
   * the wire form for when the chunk sizes are the point.
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
