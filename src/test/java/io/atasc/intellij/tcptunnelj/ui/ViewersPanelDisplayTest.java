package io.atasc.intellij.tcptunnelj.ui;

import org.junit.Test;

import static io.atasc.intellij.tcptunnelj.ui.ViewersPanel.MAX_DISPLAY_LINE_LENGTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A JSON body arrives as one line of tens of thousands of characters, which Swing can only lay out
 * as a smear of overlapping glyphs once line wrap is off. The viewers wrap by default, so
 * {@link ViewersPanel#forDisplay(String, boolean)} hands the text through untouched; only when the
 * wrap is turned off does {@link ViewersPanel#forDisplay(String)} break the long lines up.
 */
public class ViewersPanelDisplayTest {

  @Test
  public void wrappedViewersShowTheTextUntouched() {
    String line = repeat('x', MAX_DISPLAY_LINE_LENGTH * 3);

    assertSame(line, ViewersPanel.forDisplay(line, true));
  }

  @Test
  public void unwrappedViewersBreakUpAnOverLongLine() {
    String line = repeat('x', MAX_DISPLAY_LINE_LENGTH * 2 + 500);
    String display = ViewersPanel.forDisplay(line, false);

    assertEquals(line, display.replace("\n", ""));
    assertEquals(3, display.split("\n", -1).length);
  }

  @Test
  public void handlesNullTextWhateverTheWrap() {
    assertEquals("", ViewersPanel.forDisplay(null, true));
    assertEquals("", ViewersPanel.forDisplay(null, false));
  }

  @Test
  public void leavesShortLinesAlone() {
    String text = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n{\"n\":1}";

    assertSame(text, ViewersPanel.forDisplay(text));
  }

  @Test
  public void breaksUpAnOverLongLine() {
    String line = repeat('x', MAX_DISPLAY_LINE_LENGTH * 2 + 500);
    String display = ViewersPanel.forDisplay(line);

    assertEquals(line, display.replace("\n", ""));
    assertEquals(3, display.split("\n", -1).length);
    for (String piece : display.split("\n", -1)) {
      assertTrue("no piece may stay over the limit", piece.length() <= MAX_DISPLAY_LINE_LENGTH);
    }
  }

  @Test
  public void keepsTheHeadersIntactAroundAnOverLongBody() {
    String headers = "HTTP/1.1 200 OK\nContent-Type: application/json\n";
    String body = repeat('y', MAX_DISPLAY_LINE_LENGTH + 500);
    String display = ViewersPanel.forDisplay(headers + "\n" + body);

    assertEquals(headers + "\n" + repeat('y', MAX_DISPLAY_LINE_LENGTH) + "\n" + repeat('y', 500),
        display);
  }

  @Test
  public void keepsTheLineStructureAfterABrokenUpLine() {
    String display = ViewersPanel.forDisplay(repeat('z', MAX_DISPLAY_LINE_LENGTH + 200) + "\nlast\n");

    assertEquals(repeat('z', MAX_DISPLAY_LINE_LENGTH) + "\n" + repeat('z', 200) + "\nlast\n",
        display);
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
