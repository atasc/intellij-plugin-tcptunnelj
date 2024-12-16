package io.atasc.intellij.tcptunnelj.tunnellij.ui;

import com.intellij.icons.AllIcons;

import javax.swing.*;

//https://intellij-icons.jetbrains.design/#AllIcons-expui-codeWithMe-cwmPermissionView
public class Icons {

  // My icons (personalizzati)
  public static final Icon ICON_WATCH = AllIcons.CodeWithMe.CwmPermissionView; // Sostituisce tunnellij.png
  public static final Icon ICON_REMOVE = AllIcons.General.Remove;   // Sostituisce remove.png
  public static final Icon ICON_CLEAR = AllIcons.Actions.GC;        // Sostituisce removeall.png
  public static final Icon ICON_WRAP = AllIcons.Actions.ShowCode;   // Sostituisce wrap.png


  // IntelliJ icons
  public static final Icon ICON_START = AllIcons.Actions.Execute;
  public static final Icon ICON_STOP = AllIcons.Actions.Suspend;
  public static final Icon ICON_HELP = AllIcons.Actions.Help;

  private static ImageIcon getCustomIcon(String file) {
    try {
      java.net.URL url = Icons.class.getResource("/" + file);
      return new ImageIcon(url);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Cannot find icon " + file);
      return null;
    }
  }
}
