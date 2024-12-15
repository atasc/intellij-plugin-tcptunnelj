package io.atasc.intellij.nettunnel;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

public final class NetTunnelPluginBundle extends DynamicBundle {

  @NonNls
  private static final String BUNDLE = "messages.NetTunnelPluginBundle";

  private static final NetTunnelPluginBundle INSTANCE = new NetTunnelPluginBundle();

  private NetTunnelPluginBundle() {
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
