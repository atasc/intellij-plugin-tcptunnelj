package io.atasc.intellij.tcptunnelj.toolWindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import io.atasc.intellij.tcptunnelj.tunnellij.TunnelPlugin;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author atasc
 * @since
 */
public class TcpTunnelWindowFactory implements ToolWindowFactory {
  private static final Logger LOGGER = Logger.getInstance(TcpTunnelWindowFactory.class);

  static {
    //LOGGER.warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");
  }

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    int windowToLoad = 3;

    switch (windowToLoad) {
      case 1 -> {
        TcpTunnelWindow tcpTunnelWindow = new TcpTunnelWindow(toolWindow);
        var content = ContentFactory.getInstance().createContent(tcpTunnelWindow.getContent(), null, false);
        toolWindow.getContentManager().addContent(content);
        //toolWindow.setIcon(Icons.ICON_TOOL);
      }
      case 2 -> {
      }
      case 3 -> {
        SwingUtilities.invokeLater(() -> {
          TunnelPlugin tunnelPlugin = new TunnelPlugin(project);
          var content = ContentFactory.getInstance().createContent(tunnelPlugin.getContent(), null, false);
          toolWindow.getContentManager().addContent(content);
          //toolWindow.setIcon(Icons.ICON_TOOL);
        });
      }
    }

  }

  @Override
  public boolean shouldBeAvailable(Project project) {
    return true;
  }

  @Override
  public @Nullable ToolWindowAnchor getAnchor() {
    return ToolWindowFactory.super.getAnchor();
  }

  @Override
  public @Nullable Icon getIcon() {
    //return ToolWindowFactory.super.getIcon();
    //Icon icon = Icons.ICON_TOOL;
    Icon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/icon.png")));
    return icon;
  }

  @Override
  public boolean isDoNotActivateOnStart() {
    return ToolWindowFactory.super.isDoNotActivateOnStart();
  }

  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    ToolWindowFactory.super.init(toolWindow);
  }

  @Override
  public boolean isApplicable(@NotNull Project project) {
    return ToolWindowFactory.super.isApplicable(project);
  }

  @Override
  public @Nullable Object isApplicableAsync(@NotNull Project project, @NotNull Continuation<? super Boolean> $completion) {
    return ToolWindowFactory.super.isApplicableAsync(project, $completion);
  }

  @Override
  public @Nullable Object manage(@NotNull ToolWindow toolWindow, @NotNull ToolWindowManager toolWindowManager, @NotNull Continuation<? super Unit> $completion) {
    return ToolWindowFactory.super.manage(toolWindow, toolWindowManager, $completion);
  }
}
