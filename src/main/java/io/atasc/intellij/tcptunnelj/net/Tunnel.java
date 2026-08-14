package io.atasc.intellij.tcptunnelj.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author atasc
 * @since
 */
public class Tunnel {
  private final int srcPort;
  private final int destPort;
  private final String destHost;
  private volatile boolean shouldStop = false;
  private volatile boolean isRunning = false;
  private ServerSocket serverSocket;
  private final List<TunnelListener> listeners = new LinkedList<>();

  /**
   * The connections being pumped right now. {@link #stop()} closes them, otherwise their threads stay
   * blocked in a read until the peer gives up — and a thread of this plugin outliving the tunnel is
   * what keeps the plugin from being unloaded without restarting the IDE.
   */
  private final Set<ClientHandler> handlers = ConcurrentHashMap.newKeySet();

  public Tunnel(int srcPort, int destPort, String destHost) {
    this.srcPort = srcPort;
    this.destPort = destPort;
    this.destHost = destHost;
  }

  public void addTunnelListener(TunnelListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public void removeTunnelListener(TunnelListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  // Metodo per notificare un nuovo call
  public void notifyNewCall(Call call) {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.newCall(call);
      }
    }
  }

  // Metodo per notificare il termine di un call
  public void notifyCallEnded(Call call) {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.endCall(call);
      }
    }
  }

  public void onDataReceived(Call call, boolean isRequest, String data) {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.onDataReceived(call, data);
      }
    }
  }

  public void addListener(TunnelListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public void removeListener(TunnelListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void start() throws TunnelException {
    try {
      serverSocket = new ServerSocket(srcPort);
      isRunning = true;
      fireTunnelStarted();
      System.out.println("Tunnel started on port " + srcPort + " to " + destHost + ":" + destPort);

      while (!shouldStop) {
        try {
          Socket clientSocket = serverSocket.accept();
          // the connection to the destination is opened by the handler thread: doing it here would
          // serialize connection setup and, on failure, would take the whole accept loop down
          ClientHandler handler = new ClientHandler(clientSocket, destHost, destPort, this);
          handlers.add(handler);
          handler.start();

          if (shouldStop) {
            // stop() ran between accept() and the registration above, so this one is ours to cut
            handler.close();
          }
        } catch (IOException e) {
          if (shouldStop || serverSocket.isClosed()) {
            break;
          }
          System.err.println("Error accepting connection: " + e.getMessage());
        }
      }
    } catch (IOException e) {
      throw new TunnelException("Cannot start tunnel: " + e.getMessage());
    } finally {
      stop();
    }
  }

  public void stop() {
    shouldStop = true;
    isRunning = false;
    fireTunnelStopped();

    try {
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException e) {
      System.err.println("Error closing server socket: " + e.getMessage());
    }

    // Closing the server socket only ends the accept loop; the connections already being pumped have
    // their own threads, blocked in a read that nothing else will interrupt.
    for (ClientHandler handler : handlers) {
      handler.close();
    }
    handlers.clear();
  }

  /**
   * Called by a {@link ClientHandler} when its connection is over, so that {@link #stop()} does not
   * keep walking connections that have already closed themselves.
   */
  void handlerFinished(ClientHandler handler) {
    handlers.remove(handler);
  }

  private void fireTunnelStarted() {
    synchronized (listeners) {
      listeners.forEach(TunnelListener::tunnelStarted);
    }
  }

  private void fireTunnelStopped() {
    synchronized (listeners) {
      listeners.forEach(TunnelListener::tunnelStopped);
    }
  }
}
