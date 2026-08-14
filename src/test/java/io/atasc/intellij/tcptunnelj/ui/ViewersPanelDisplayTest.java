package io.atasc.intellij.tcptunnelj.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A JSON body arrives as one line of tens of thousands of characters, and Swing draws such a line
 * as a smear of overlapping glyphs. {@link ViewersPanel#forDisplay(String)} is what keeps the
 * viewers readable, without touching anything short enough to draw.
 */
public class ViewersPanelDisplayTest {

  @Test
  public void leavesShortLinesAlone() {
    String text = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"n\":1}";

    assertSame(text, ViewersPanel.forDisplay(text));
  }

  @Test
  public void breaksUpAnOverLongLine() {
    String line = repeat('x', 2500);
    String display = ViewersPanel.forDisplay(line);

    assertEquals(line, display.replace("\n", ""));
    assertEquals(3, display.split("\n", -1).length);
    for (String piece : display.split("\n", -1)) {
      assertTrue("no piece may stay over the limit", piece.length() <= 1000);
    }
  }

  @Test
  public void keepsTheHeadersIntactAroundAnOverLongBody() {
    String headers = "HTTP/1.1 200 OK\nContent-Type: application/json\n";
    String body = repeat('y', 1500);
    String display = ViewersPanel.forDisplay(headers + "\n" + body);

    assertEquals(headers + "\n" + repeat('y', 1000) + "\n" + repeat('y', 500), display);
  }

  @Test
  public void keepsTheLineStructureAfterABrokenUpLine() {
    String display = ViewersPanel.forDisplay(repeat('z', 1200) + "\nlast\n");

    assertEquals(repeat('z', 1000) + "\n" + repeat('z', 200) + "\nlast\n", display);
  }

  @Test
  public void handlesEmptyAndNullText() {
    assertEquals("", ViewersPanel.forDisplay(null));
    assertEquals("", ViewersPanel.forDisplay(""));
  }

  private static String repeat(char c, int times) {
    StringBuilder builder = new StringBuilder(times);
    for (int i = 0; i < times; i++) {
      builder.append(c);
    }
    return builder.toString();
  }
}
