package io.atasc.intellij.tcptunnelj.net;

import java.io.IOException;
import java.net.Socket;

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

      // Notifica i listener che una nuova chiamata è iniziata
      tunnel.notifyNewCall(call);

      TunnelWriter clientToDestination = new TunnelWriter(
          clientSocket.getInputStream(),
          destinationSocket.getOutputStream(),
          call.getOutputLogger()
      );

      TunnelWriter destinationToClient = new TunnelWriter(
          destinationSocket.getInputStream(),
          clientSocket.getOutputStream(),
          call.getInputLogger()
      );

      clientToDestination.start();
      destinationToClient.start();

      clientToDestination.join();
      destinationToClient.join();

      call.setEnd(System.currentTimeMillis());

      // Notifica i listener che la chiamata è terminata
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
