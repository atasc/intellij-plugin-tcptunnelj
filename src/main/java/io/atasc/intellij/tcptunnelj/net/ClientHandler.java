package io.atasc.intellij.tcptunnelj.net;

import java.io.IOException;
import java.net.Socket;

/**
 * @author atasc
 * @since
 */
public class ClientHandler extends Thread {
  private final Socket clientSocket;
  private final Socket destinationSocket;
  private final Tunnel tunnel;

  public ClientHandler(Socket clientSocket, Socket destinationSocket, Tunnel tunnel) {
    this.clientSocket = clientSocket;
    this.destinationSocket = destinationSocket;
    this.tunnel = tunnel;
  }

  @Override
  public void run() {
    try {
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
    }
  }


  private void closeSockets() {
    try {
      if (!clientSocket.isClosed()) {
        clientSocket.close();
      }
      if (!destinationSocket.isClosed()) {
        destinationSocket.close();
      }
    } catch (IOException e) {
      System.err.println("Error closing sockets: " + e.getMessage());
    }
  }
}
