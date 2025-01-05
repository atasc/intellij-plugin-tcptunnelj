package io.atasc.intellij.tcptunnelj.ui;

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
  //getCustomIcon("icons/pluginIcon.svg");
  public static final Icon ICON_TOOL = AllIcons.Nodes.Plugin;
  public static final Icon ICON_WATCH = AllIcons.Actions.Show;
  public static final Icon ICON_REMOVE = AllIcons.General.Remove;
  public static final Icon ICON_CLEAR = AllIcons.Actions.GC;
  public static final Icon ICON_WRAP = AllIcons.Actions.ToggleSoftWrap;
  public static final Icon ICON_START_ON_BOOT = AllIcons.Actions.Play_forward;

  public static final Icon ICON_START = AllIcons.Actions.Execute;
  public static final Icon ICON_STOP = AllIcons.Actions.Suspend;
  public static final Icon ICON_HELP = AllIcons.Actions.Help;
  public static final Icon ICON_SAVE = AllIcons.Actions.MenuSaveall;

  public static ImageIcon getCustomIcon(String file) {
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
