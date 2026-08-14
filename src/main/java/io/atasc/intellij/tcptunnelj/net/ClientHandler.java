package io.atasc.intellij.tcptunnelj.net;

import java.io.IOException;
import java.net.Socket;

/**
 * @author atasc
 * @since
 */
public class ClientHandler extends Thread {
  private final Socket clientSocket;
  private final String destHost;
  private final int destPort;
  private final Tunnel tunnel;
  private Socket destinationSocket;

  public ClientHandler(Socket clientSocket, String destHost, int destPort, Tunnel tunnel) {
    this.clientSocket = clientSocket;
    this.destHost = destHost;
    this.destPort = destPort;
    this.tunnel = tunnel;

    // A connection outlives neither the tunnel nor the IDE: as a daemon it can never hold either one
    // open, and it is the tunnel that decides when to cut it short.
    setDaemon(true);
    setName("TcpTunnelJ handler " + clientSocket.getPort());
  }

  @Override
  public void run() {
    try {
      destinationSocket = new Socket(destHost, destPort);

      Call call = new Call(
          clientSocket.getInetAddress().getHostAddress(),
          clientSocket.getPort(),
          destinationSocket.getInetAddress().getHostAddress(),
          destinationSocket.getPort()
      );

      tunnel.notifyNewCall(call);

      TunnelWriter clientToDestination = new TunnelWriter(
          clientSocket.getInputStream(),
          destinationSocket.getOutputStream(),
          call.getOutputLogger(),
          data -> tunnel.onDataReceived(call, true, data)
      );

      TunnelWriter destinationToClient = new TunnelWriter(
          destinationSocket.getInputStream(),
          clientSocket.getOutputStream(),
          call.getInputLogger(),
          data -> tunnel.onDataReceived(call, false, data)
      );

      clientToDestination.start();
      destinationToClient.start();

      clientToDestination.join();
      destinationToClient.join();

      call.setEnd(System.currentTimeMillis());
      tunnel.notifyCallEnded(call);
    } catch (Exception e) {
      System.err.println("Error in ClientHandler: " + e.getMessage());
    } finally {
      closeSockets();
      tunnel.handlerFinished(this);
    }
  }

  /**
   * Cuts this connection short, which is how {@link Tunnel#stop()} gets the two pump threads to
   * leave their blocking reads: closing the sockets makes those reads fail and the threads end.
   */
  void close() {
    closeSockets();
  }

  private void closeSockets() {
    try {
      if (!clientSocket.isClosed()) {
        clientSocket.close();
      }
      if (destinationSocket != null && !destinationSocket.isClosed()) {
        destinationSocket.close();
      }
    } catch (IOException e) {
      System.err.println("Error closing sockets: " + e.getMessage());
    }
  }
}
