package io.atasc.intellij.tcptunnelj.net;

/**
 * @author boruvka
 * @since
 */
public interface TunnelListener {

  void newCall(Call call);

  void endCall(Call call);

  void tunnelStarted();

  void tunnelStopped();

  void onDataReceived(Call call, String data);

}
