package io.atasc.intellij.tcptunnelj;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author atasc
 * @since
 */
public final class TcpTunnelPluginBundle extends DynamicBundle {

  @NonNls
  private static final String BUNDLE = "messages.TcpTunnelPluginBundle";

  private static final TcpTunnelPluginBundle INSTANCE = new TcpTunnelPluginBundle();

  private TcpTunnelPluginBundle() {
    super(BUNDLE);
  }

  public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
    return INSTANCE.getMessage(key, params);
  }

  @SuppressWarnings("unused")
  public static String messagePointer(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
    return INSTANCE.getLazyMessage(key, params).toString();
  }
}
