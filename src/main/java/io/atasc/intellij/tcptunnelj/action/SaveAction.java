package io.atasc.intellij.tcptunnelj.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import io.atasc.intellij.tcptunnelj.TunnelPlugin;
import io.atasc.intellij.tcptunnelj.ui.Icons;
import io.atasc.intellij.tcptunnelj.ui.TunnelPanel;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author atasc
 * @since
 */
public class SaveAction extends BaseAction {

  public SaveAction(TunnelPlugin tunnelPlugin) {
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
            "TcpTunnelJ Notifications",
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
    p.setEnabled(!tunnelPanel.isRunning() && tunnelPanel.getCallListSize() > 0);
    p.setVisible(true);
  }


  private void performSaveAction() throws IOException {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    String callList = tunnelPanel.getCallListToString();

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Log File");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Log Files (*.log)", "log"));
    fileChooser.setSelectedFile(new File("tcptunnelj.log"));

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
              "TcpTunnelJ Notifications",
              "File Saved",
              "Log file saved successfully!",
              NotificationType.INFORMATION
          ));
        });

      }
    }
  }

}
