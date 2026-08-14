package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBPanel;
import io.atasc.intellij.tcptunnelj.net.Call;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * The two viewers, request on the left and response on the right.
 * <p>
 * They are platform editors rather than {@code JBTextArea}s on purpose. A JSON body is one line of
 * tens of thousands of characters, and Swing lays such a line out in a single run: with the wrap off
 * the glyph positions collapse and the line renders as an unreadable strip. The platform editor only
 * paints the part of the line that is on screen, so it stays readable wrapped or not, and the text
 * never has to be altered to make it drawable — which matters because "Copy" copies what is on
 * screen, and a body that has been broken up for display pastes as invalid JSON.
 */
public class ViewersPanel extends JBPanel implements Disposable {
  private Call currentCall;

  /**
   * Whether the viewers soft-wrap, mirroring the "Wrap lines" toolbar toggle. On by default: a JSON
   * body unwrapped is one line running off to the right, and soft wrap is the editor's own, so it
   * costs nothing and changes no text.
   */
  private boolean wrapped = true;

  private EditorEx editorRq;
  private EditorEx editorRs;
  private OnePixelSplitter splitPaneLeftRight;

  public ViewersPanel() {
    initComponents();
  }

  protected void initComponents() {
    setLayout(new BorderLayout());
    setBackground(UIManager.getColor("Tree.textBackground"));

    editorRq = createViewer(false);
    editorRs = createViewer(true);

    splitPaneLeftRight = new OnePixelSplitter(false, 0.5f); // false for horizontal split
    splitPaneLeftRight.setFirstComponent(editorRq.getComponent());
    splitPaneLeftRight.setSecondComponent(editorRs.getComponent());

    add(splitPaneLeftRight, BorderLayout.CENTER);
  }

  /**
   * A read-only editor over an empty document, stripped of everything that only makes sense when
   * editing a file: no gutter, no line numbers, no folding, no right margin.
   */
  private EditorEx createViewer(boolean isResponse) {
    Document document = EditorFactory.getInstance().createDocument("");
    EditorEx editor = (EditorEx) EditorFactory.getInstance().createViewer(document);

    EditorSettings settings = editor.getSettings();
    settings.setUseSoftWraps(wrapped);
    settings.setLineNumbersShown(false);
    settings.setLineMarkerAreaShown(false);
    settings.setFoldingOutlineShown(false);
    settings.setIndentGuidesShown(false);
    settings.setRightMarginShown(false);
    settings.setCaretRowShown(false);
    settings.setVirtualSpace(false);
    settings.setAdditionalColumnsCount(0);
    settings.setAdditionalLinesCount(0);

    installPopupMenu(editor, isResponse);

    return editor;
  }

  /**
   * Shows the request as it was captured and the response with its chunked framing undone and its
   * payload decompressed, which is what makes a JSON body readable: the chunk sizes no longer sit in
   * the middle of the text. The wire form stays one context-menu entry away.
   */
  public void view(Call call) {
    if (call == null) {
      return;
    }

    currentCall = call;
    render();
  }

  private void render() {
    if (currentCall == null) {
      return;
    }

    setText(editorRq, currentCall.getRequestText());
    setText(editorRs, currentCall.getResponseText());
  }

  /**
   * Replaces what {@code editor} shows and scrolls back to the top.
   * <p>
   * A {@link Document} may not hold a carriage return, so the CRLFs of the capture are normalized to
   * LF — the only change the viewers make to the text. It is invisible in a body, which carries no
   * line breaks of its own once the chunked framing is gone; "Copy response (raw)" is there for when
   * the exact bytes of the headers are what matters.
   */
  private static void setText(EditorEx editor, String text) {
    String normalized = StringUtil.convertLineSeparators(text == null ? "" : text);

    ApplicationManager.getApplication().runWriteAction(
        () -> editor.getDocument().setText(normalized));

    editor.getCaretModel().moveToOffset(0);
    editor.getScrollingModel().scrollHorizontally(0);
    editor.getScrollingModel().scrollVertically(0);
  }

  public void scrollViewerToBottom() {
    scrollToBottom(editorRq);
    scrollToBottom(editorRs);
  }

  private static void scrollToBottom(EditorEx editor) {
    editor.getCaretModel().moveToOffset(editor.getDocument().getTextLength());
    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
  }

  public void wrap() {
    setWrapped(true);
  }

  public void unwrap() {
    setWrapped(false);
  }

  private void setWrapped(boolean wrapped) {
    this.wrapped = wrapped;
    editorRq.getSettings().setUseSoftWraps(wrapped);
    editorRs.getSettings().setUseSoftWraps(wrapped);
  }

  public void clear() {
    currentCall = null;
    setText(editorRq, "");
    setText(editorRs, "");
  }

  /**
   * Editors handle the right click themselves, so the menu goes in through a popup handler rather
   * than {@code setComponentPopupMenu}. "Copy" hands over the selection as it is on screen, which is
   * the decoded response; the response menu adds the payload built from the call itself — the body
   * alone, without the headers — and the wire form for when the chunk sizes are the point.
   */
  private void installPopupMenu(EditorEx editor, boolean isResponse) {
    JBPopupMenu popupMenu = new JBPopupMenu();

    popupMenu.add(new AbstractAction("Copy") {
      @Override
      public void actionPerformed(ActionEvent e) {
        copyToClipboard(editor.getSelectionModel().getSelectedText());
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

    popupMenu.add(new AbstractAction("Select All") {
      @Override
      public void actionPerformed(ActionEvent e) {
        editor.getSelectionModel().setSelection(0, editor.getDocument().getTextLength());
      }
    });

    popupMenu.add(new AbstractAction("Clear") {
      @Override
      public void actionPerformed(ActionEvent e) {
        setText(editor, "");
      }
    });

    editor.installPopupHandler(event -> {
      MouseEvent mouseEvent = event.getMouseEvent();
      popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
      return true;
    });
  }

  /**
   * Puts {@code text} on the clipboard, unless there was nothing to copy.
   */
  private static void copyToClipboard(String text) {
    if (text != null && !text.isEmpty()) {
      CopyPasteManager.getInstance().setContents(new StringSelection(text));
    }
  }

  /**
   * Editors are not garbage: the factory keeps every one it hands out until it is released, so
   * failing to do this leaks an editor per tool window.
   */
  @Override
  public void dispose() {
    currentCall = null;
    editorRq = release(editorRq);
    editorRs = release(editorRs);
  }

  private static EditorEx release(Editor editor) {
    if (editor != null && !editor.isDisposed()) {
      EditorFactory.getInstance().releaseEditor(editor);
    }
    return null;
  }
}
