package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.Messages;
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
    try {
      TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

      String callList = tunnelPanel.getCallListToString();

      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Save Log File");
      fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Log Files (*.log)", "log"));

      // Set default file name
      fileChooser.setSelectedFile(new File("tcptunnelj.log"));

      int userSelection = fileChooser.showSaveDialog(null);

      if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();

        // Ensure the file has a .log extension
        if (!fileToSave.getName().toLowerCase().endsWith(".log")) {
          fileToSave = new File(fileToSave.getAbsolutePath() + ".log");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
          writer.write(callList);
          Messages.showMessageDialog("Log file saved successfully!", "Success", Messages.getInformationIcon());
        } catch (IOException e) {
          e.printStackTrace();
          Messages.showMessageDialog("Error while saving log file: " + e.getMessage(), "Error", Messages.getErrorIcon());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      Messages.showMessageDialog("Error while saving log file: " + e.getMessage(), "Error", Messages.getErrorIcon());
    }

  }

  @Override
  public void update(AnActionEvent event) {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    Presentation p = event.getPresentation();
    p.setEnabled(!tunnelPanel.isRunning() && tunnelPanel.getCallListSize() > 0);
    p.setVisible(true);
  }

}