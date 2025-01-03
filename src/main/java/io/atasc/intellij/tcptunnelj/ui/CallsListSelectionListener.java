package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.ui.components.JBList;
import io.atasc.intellij.tcptunnelj.net.Call;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class CallsListSelectionListener implements ListSelectionListener {
  private ViewersPanel viewersPanel;

  public CallsListSelectionListener(ViewersPanel viewersPanel) {
    this.viewersPanel = viewersPanel;
  }

  public void valueChanged(ListSelectionEvent event) {
    JBList list = (JBList) event.getSource();
    Call call = (Call) list.getSelectedValue();

    if (call != null) {
      viewersPanel.view(call);
    } else {
      viewersPanel.clear();
    }
  }
}
