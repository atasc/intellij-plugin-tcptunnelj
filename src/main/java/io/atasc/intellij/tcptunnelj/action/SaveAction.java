package io.atasc.intellij.tcptunnelj.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

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
        writer.write("ciao mondo");
        Messages.showMessageDialog("Log file saved successfully!", "Success", Messages.getInformationIcon());
      } catch (IOException e) {
        e.printStackTrace();
        Messages.showMessageDialog("Error while saving log file: " + e.getMessage(), "Error", Messages.getErrorIcon());
      }
    }
  }

  public void actionPerformedX(AnActionEvent event) {
    TunnelPanel tunnelPanel = this.tunnelPlugin.getTunnelPanel();

    // Create a FileChooserDescriptor for saving files
    FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, false, false, false, false, true)
        .withTitle("Save Log File")
        .withDescription("Choose a location to save the log file")
        .withFileFilter(virtualFile -> virtualFile.getName().endsWith(".log"));

    // Suggest default file name
    VirtualFile defaultFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(System.getProperty("user.home"));
    if (defaultFile != null) {
      FileChooser.chooseFile(fileChooserDescriptor, event.getProject(), defaultFile, chosenFile -> {
        // Ensure file has the correct .log extension
        String filePath = chosenFile.getPath();
        if (!filePath.toLowerCase().endsWith(".log")) {
          filePath += ".log";
        }

        // Save the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
          writer.write("ciao mondo");
          Messages.showMessageDialog("Log file saved successfully at: " + filePath, "Success", Messages.getInformationIcon());
        } catch (IOException e) {
          e.printStackTrace();
          Messages.showMessageDialog("Error while saving log file: " + e.getMessage(), "Error", Messages.getErrorIcon());
        }
      });
    }
  }


  @Override
  public void update(AnActionEvent event) {
  }

}
