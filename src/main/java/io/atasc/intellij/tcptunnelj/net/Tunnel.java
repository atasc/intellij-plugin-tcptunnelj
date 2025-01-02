package io.atasc.intellij.tcptunnelj.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Tunnel {
  private final int srcPort;
  private final int destPort;
  private final String destHost;
  private boolean shouldStop = false;
  private boolean isRunning = false;
  private ServerSocket serverSocket;
  private final List<TunnelListener> listeners = new LinkedList<>();

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
          Socket destinationSocket = new Socket(destHost, destPort);
          new ClientHandler(clientSocket, destinationSocket, this).start();
        } catch (IOException e) {
          if (!shouldStop) {
            throw new TunnelException("Error accepting connection: " + e.getMessage());
          }
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
