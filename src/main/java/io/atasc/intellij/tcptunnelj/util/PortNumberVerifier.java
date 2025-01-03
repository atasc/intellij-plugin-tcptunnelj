package io.atasc.intellij.tcptunnelj.util;

import com.intellij.ui.components.JBTextField;

import javax.swing.*;

public class PortNumberVerifier extends InputVerifier {

  public boolean verify(JComponent input) {
    String text = ((JBTextField) input).getText();
    try {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return false;
    }
  }

}
