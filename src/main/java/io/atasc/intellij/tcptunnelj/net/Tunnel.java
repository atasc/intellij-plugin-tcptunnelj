package io.atasc.intellij.tcptunnelj.net;

import io.atasc.intellij.tcptunnelj.TunnelConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * @author boruvka/atasc
 * @since
 */
public class Tunnel {
  private int srcPort;
  private int destPort;
  private String destHost = "localhost";
  private boolean shouldStop = false;
  private LinkedList<TunnelListener> listeners = new LinkedList<>();
  private ServerSocket serverSocket = null;
  private boolean isRunning = false;

  public Tunnel(int srcPort, int destPort) {
    this.srcPort = srcPort;
    this.destPort = destPort;
  }

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
      if (listeners.contains(listener)) {
        listeners.remove(listener);
      }
    }
  }

  private void fireCallNotifyEvent(Call call) {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.newCall(call);
      }
    }
  }

  private void fireCallEndedEvent(Call call) {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.endCall(call);
      }
    }
  }

  private void fireTunnelStarted() {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.tunnelStarted();
      }
    }
  }

  private void fireTunnelStopped() {
    synchronized (listeners) {
      for (TunnelListener listener : listeners) {
        listener.tunnelStopped();
      }
    }
  }


  public boolean isRunning() {
    return isRunning;
  }

  public void start() throws TunnelException {

    try {

      serverSocket = new ServerSocket(srcPort);
      System.out.println("Tunnel started at port " + srcPort + ", "
          + " tunneling to " + destHost + ":" + destPort);
      fireTunnelStarted();

    } catch (IOException e) {

      fireTunnelStopped();
      throw new TunnelException("Cannot create server socket. The port "
          + srcPort + " is probably used by another application.");

    }
    while (!shouldStop) {
      Socket client;
      Socket dest;
      isRunning = true;
      try {
        client = serverSocket.accept();
      } catch (IOException e) {
        shouldStop = true;
        break;
      }
      try {
        dest = new Socket(destHost, destPort);
        new ClientServant(client, dest);
      } catch (IOException e) {
        // TODO: in status bar, also info about client...
        throw new TunnelException("Cannot connect to " + destHost + ":"
            + destPort + ". (" + e.getMessage() + ")");

      }
    }
    fireTunnelStopped();

  }

  public void stop() {
    fireTunnelStopped();
    if (serverSocket != null) {
      try {
        serverSocket.close();
        isRunning = false;
      } catch (IOException e) {
      }
    }
  }

  class ClientServant {

    private Socket source;

    private Socket dest;

    public ClientServant(Socket source, Socket dest) throws TunnelException {
      this.source = source;
      this.dest = dest;

      try {
        serve();
      } catch (IOException e) {
        throw new TunnelException(e.getMessage());
      }
    }

    private void serve() throws IOException {

      Call call = new Call("localhost", source.getLocalPort(), dest
          .getInetAddress().getHostName(), dest.getPort());

      fireCallNotifyEvent(call);

      // create writers and start them
      new Writer(call, source.getInputStream(), dest.getOutputStream(),
          call.getOutputLogger()).start();

      new Writer(call, dest.getInputStream(), source.getOutputStream(),
          call.getInputLogger()).start();
      // create writers and start them
    }

    class Writer extends Thread {
      InputStream readFrom;
      OutputStream writeTo;
      OutputStream logTo;
      Call call;

      final int BUFFER_SIZE = TunnelConfig.BUFFER_LENGTH;

      public Writer(Call call, InputStream readFrom,
                    OutputStream writeTo, OutputStream logTo) {
        this.call = call;
        this.readFrom = readFrom;
        this.writeTo = writeTo;
        this.logTo = logTo;
      }

      public void run() {

        try {
          byte[] buffer = new byte[BUFFER_SIZE];

          int n;
          while ((n = readFrom.read(buffer)) > 0) {
            writeTo.write(buffer, 0, n);
            writeTo.flush();
            if (logTo != null) {
              logTo.write(buffer, 0, n);
              logTo.flush();
            }
          }

        } catch (IOException e) {
          // ignore (Connection reset)
        } finally {
          try {
            writeTo.close();
            if (logTo != null) {
              logTo.close();
            }

            source.close();
            dest.close();

            System.out.println("writer " + getName() + ":"
                + hashCode() + " closed.");

            call.setEnd(System.currentTimeMillis());
            fireCallEndedEvent(call);

          } catch (IOException e) {
            // ignore
          }
        }
      }
    }
  }

}
