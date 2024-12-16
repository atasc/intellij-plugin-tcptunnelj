package io.atasc.intellij.tcptunnelj.tunnellij.ui;

import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * @author boruvka/atasc
 * @since
 */
//https://intellij-icons.jetbrains.design/#AllIcons-expui-codeWithMe-cwmPermissionView
public class Icons {
  //AllIcons.Expui.Actions.ShortcutFilter
  //AllIcons.Toolwindows.WebToolWindow;
  public static final Icon ICON_TOOL = AllIcons.General.Web;
  public static final Icon ICON_WATCH = AllIcons.CodeWithMe.CwmPermissionView;
  public static final Icon ICON_REMOVE = AllIcons.General.Remove;
  public static final Icon ICON_CLEAR = AllIcons.Actions.GC;
  public static final Icon ICON_WRAP = AllIcons.Actions.ShowCode;

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
