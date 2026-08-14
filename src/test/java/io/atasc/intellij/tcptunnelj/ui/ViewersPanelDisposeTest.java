package io.atasc.intellij.tcptunnelj.ui;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/**
 * The viewers are platform editors, and the editor factory holds on to every editor it hands out
 * until it is released. Nothing used to dispose the plugin, so this is the check that the disposal
 * chain — tool window disposable → {@code TcpTunnelPlugin} → {@code TunnelPanel} →
 * {@code CallsPanel} → {@code ViewersPanel} — actually reaches the editors.
 */
public class ViewersPanelDisposeTest extends BasePlatformTestCase {

  public void testDisposeReleasesBothEditors() {
    int before = EditorFactory.getInstance().getAllEditors().length;

    ViewersPanel panel = new ViewersPanel();
    assertEquals("the panel holds a request and a response editor",
        before + 2, EditorFactory.getInstance().getAllEditors().length);

    panel.dispose();
    assertEquals("both editors must be released",
        before, EditorFactory.getInstance().getAllEditors().length);
  }

  public void testDisposeIsSafeTwice() {
    int before = EditorFactory.getInstance().getAllEditors().length;

    ViewersPanel panel = new ViewersPanel();
    panel.dispose();
    // Disposer never disposes twice, but close() delegates here too and a double release throws
    panel.dispose();

    assertEquals(before, EditorFactory.getInstance().getAllEditors().length);
  }

  /**
   * The calls panel owns the viewers and a repeating timer; both have to go.
   */
  public void testCallsPanelDisposeReachesTheViewers() {
    int before = EditorFactory.getInstance().getAllEditors().length;

    CallsPanel panel = new CallsPanel();
    assertEquals(before + 2, EditorFactory.getInstance().getAllEditors().length);

    panel.dispose();
    assertEquals(before, EditorFactory.getInstance().getAllEditors().length);
  }
}
