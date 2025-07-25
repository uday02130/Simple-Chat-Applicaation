
// Server.java
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
  private static final int PORT = 12345;
  private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(PORT);
    System.out.println("Server started on port " + PORT);

    while (true) {
      Socket clientSocket = serverSocket.accept();
      ClientHandler handler = new ClientHandler(clientSocket);
      clients.add(handler);
      new Thread(handler).start();
    }
  }

  public static void broadcast(String message, ClientHandler excludeClient) {
    synchronized (clients) {
      for (ClientHandler client : clients) {
        if (client != excludeClient) {
          client.sendMessage(message);
        }
      }
    }
  }

  public static void removeClient(ClientHandler client) {
    clients.remove(client);
  }
}

class ClientHandler implements Runnable {
  private Socket socket;
  private PrintWriter out;
  private String nickname;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  public void run() {
    try (
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
      out = new PrintWriter(socket.getOutputStream(), true);
      out.println("Enter your nickname: ");
      nickname = in.readLine();
      System.out.println(nickname + " joined the chat.");
      Server.broadcast(nickname + " joined the chat.", this);

      String msg;
      while ((msg = in.readLine()) != null) {
        System.out.println(nickname + ": " + msg);
        Server.broadcast(nickname + ": " + msg, this);
      }

    } catch (IOException e) {
      System.out.println(nickname + " disconnected.");
    } finally {
      Server.removeClient(this);
      Server.broadcast(nickname + " left the chat.", this);
      try {
        socket.close();
      } catch (IOException ignored) {
      }
    }
  }

  public void sendMessage(String message) {
    out.println(message);
  }
}
