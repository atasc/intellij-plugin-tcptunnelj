package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import io.atasc.intellij.tcptunnelj.net.Call;
import io.atasc.intellij.tcptunnelj.net.TunnelListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author boruvka/atasc
 * @since
 */
public class CallsPanel extends JBPanel implements TunnelListener {
  public static final int DIVIDER_SIZE = 2;

  /**
   * How often the list and the viewers are refreshed while traffic is flowing. Tunnel threads only
   * flag what changed; a single timer on the EDT does the painting, so a page firing dozens of
   * requests cannot flood the EDT with one task per 8 KB chunk.
   */
  private static final int REFRESH_INTERVAL_MS = 150;

  private JBList listCalls;
  private DefaultListModel model;
  private ViewersPanel panelViewers;
  private OnePixelSplitter splitPaneTopBottom;

  private final AtomicBoolean dirty = new AtomicBoolean(false);
  private final Set<Call> callsWithNewData = ConcurrentHashMap.newKeySet();
  private final Timer refreshTimer;

  public CallsPanel() {
    setBackground(UIManager.getColor("Tree.textBackground"));
    model = new DefaultListModel();
    listCalls = new JBList(model);
    listCalls.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    panelViewers = new ViewersPanel();
    listCalls.addListSelectionListener(new CallsListSelectionListener(panelViewers));

    listCalls.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
          clearSelected();
        }
      }
    });

    refreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> {
      if (dirty.get()) {
        refreshNow();
      }
    });
    refreshTimer.setRepeats(true);

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
    runOnEdt(refreshTimer::start);
  }

  @Override
  public void tunnelStopped() {
    runOnEdt(() -> {
      refreshTimer.stop();
      // last tick: durations and sizes of the calls that were still open
      refreshNow();
    });
  }

  @Override
  public void newCall(Call call) {
    // the model is Swing state: it may only be touched on the EDT, otherwise the JList layout
    // cache goes out of sync with it and the list paints blank or scrolls to nowhere
    runOnEdt(() -> {
      model.addElement(call);
      scrollToLastCall();
      if (!refreshTimer.isRunning()) {
        // in case tunnelStarted() was never delivered to this panel
        refreshTimer.start();
      }
    });
  }

  @Override
  public void onDataReceived(Call call, String data) {
    markDirty(call);
  }

  @Override
  public void endCall(Call call) {
    markDirty(call);
  }

  /**
   * Records that {@code call} changed and lets the refresh timer paint it. Called on tunnel
   * threads, so it must not touch Swing.
   */
  private void markDirty(Call call) {
    if (call != null) {
      callsWithNewData.add(call);
    }
    dirty.set(true);
  }

  /**
   * EDT only: pushes everything accumulated since the last tick into the UI.
   */
  private void refreshNow() {
    dirty.set(false);

    Call selected = getSelectedCallFromList();
    boolean selectionChanged = selected != null && callsWithNewData.contains(selected);
    callsWithNewData.clear();

    if (listCalls.isVisible()) {
      listCalls.repaint();
      panelViewers.repaint();
    }

    if (selectionChanged) {
      panelViewers.view(selected);
      panelViewers.scrollViewerToBottom();
    }
  }

  public void repaintViewers() {
    runOnEdt(() -> {
      if (listCalls.isVisible()) {
        listCalls.repaint();
        panelViewers.repaint();
      }
    });
  }

  /**
   * Tunnel callbacks arrive on tunnel threads. {@link ModalityState#any()} keeps the UI updating
   * even while a modal dialog is open, instead of queueing every call until it is closed.
   */
  private static void runOnEdt(Runnable runnable) {
    ApplicationManager.getApplication().invokeLater(runnable, ModalityState.any());
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
    callsWithNewData.clear();
    model.clear();
  }

  public synchronized void clearSelected() {
    int[] indices = listCalls.getSelectedIndices();
    // walk backwards so that the remaining indices stay valid while removing
    for (int i = indices.length - 1; i >= 0; i--) {
      model.removeElementAt(indices[i]);
    }
  }

  public List<Call> getSelectedCalls() {
    List<Call> calls = new ArrayList<>();
    for (Object value : listCalls.getSelectedValuesList()) {
      calls.add((Call) value);
    }

    return calls;
  }

  public int getSelectedCallsSize() {
    return listCalls.getSelectedIndices().length;
  }

  /**
   * The calls list itself, used to scope keyboard shortcuts to it: an action registered on the whole
   * panel would also steal the keystroke from the request/response viewers.
   */
  public JComponent getCallsListComponent() {
    return listCalls;
  }

  /**
   * Puts the request lines of the selected calls on the clipboard, one per line,
   * in the form "GET /path?query HTTP/1.1". Returns the copied lines.
   */
  public List<String> copySelectedRequestsToClipboard() {
    List<String> requestLines = new ArrayList<>();
    for (Call call : getSelectedCalls()) {
      requestLines.addAll(call.getRequestLines());
    }

    if (requestLines.isEmpty()) {
      return requestLines;
    }

    String text = String.join(System.lineSeparator(), requestLines);
    CopyPasteManager.getInstance().setContents(new StringSelection(text));

    return requestLines;
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
      requestTxt.append(call.getRequestText());

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

    if (removeChunk) {
      responseTxt.append(call.getResponseText());
    } else {
      responseTxt.append(call.getRawResponseText());
    }

    String newLine = System.lineSeparator();
    String rq = requestTxt.toString();
    String rs = responseTxt.toString();

    String r = "CALL: " + newLine + call.toString() + newLine + newLine;
    r += "REQUEST:" + newLine + rq + newLine;
    r += "RESPONSE:" + newLine + rs + newLine;

    return r;
  }
}
