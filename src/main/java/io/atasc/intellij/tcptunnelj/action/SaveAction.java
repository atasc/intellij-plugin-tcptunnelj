package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import io.atasc.intellij.tcptunnelj.TcpTunnelPlugin;
import io.atasc.intellij.tcptunnelj.toolWindow.TcpTunnelWindow;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;
import io.atasc.intellij.tcptunnelj.util.SystemDirectories;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author atasc
 * @since
 */
public class SaveAction extends BaseAction {
  private static final String DEFAULT_FILE_NAME = "tcptunnelj.log";

  public SaveAction(TcpTunnelPlugin tunnelPlugin) {
    super("Save calls", "Save calls",
        Icons.ICON_SAVE, tunnelPlugin);
  }

  @Override
  public void actionPerformed(AnActionEvent event) {
    ApplicationManager.getApplication().invokeLater(() -> {
      try {
        performSaveAction();
      } catch (IOException e) {
        Notifications.Bus.notify(new Notification(
            TcpTunnelWindow.NOTIFICATION_ID,
            "Error",
            "Error while saving log file: " + e.getMessage(),
            NotificationType.ERROR
        ));
      }
    });
  }

  @Override
  public void update(AnActionEvent event) {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    Presentation p = event.getPresentation();
    p.setEnabled(tunnelPanel.getCallListSize() > 0);
    p.setVisible(true);
  }


  private void performSaveAction() throws IOException {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    String callList = tunnelPanel.getCallListToString();

    File downloadsDir = SystemDirectories.getDownloadsDirectory();

    JFileChooser fileChooser = new JFileChooser(downloadsDir);
    fileChooser.setDialogTitle("Save Log File");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Log Files (*.log)", "log"));
    // absolute, otherwise JFileChooser resolves the name against the process working directory and
    // silently moves the dialog away from the downloads folder
    fileChooser.setSelectedFile(new File(downloadsDir, DEFAULT_FILE_NAME));

    int userSelection = fileChooser.showSaveDialog(null);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
      File fileToSave = fileChooser.getSelectedFile();

      if (!fileToSave.getName().toLowerCase().endsWith(".log")) {
        fileToSave = new File(fileToSave.getAbsolutePath() + ".log");
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
        writer.write(callList);

        ApplicationManager.getApplication().invokeLater(() -> {
          Notifications.Bus.notify(new Notification(
              TcpTunnelWindow.NOTIFICATION_ID,
              "File Saved",
              "Log file saved successfully!",
              NotificationType.INFORMATION
          ));
        });

      }
    }
  }

}
