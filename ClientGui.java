import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientGui extends JFrame {

  private Socket socket;
  private BufferedReader reader;
  private PrintWriter writer;

  private JTextArea chatArea;
  private JTextField inputField;
  private JButton sendButton;

  public ClientGui() {
    setTitle("Java Chat Client");
    setSize(400, 500);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    chatArea = new JTextArea();
    chatArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(chatArea);

    inputField = new JTextField();
    sendButton = new JButton("Send");

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(inputField, BorderLayout.CENTER);
    bottomPanel.add(sendButton, BorderLayout.EAST);

    add(scrollPane, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);

    sendButton.addActionListener(e -> sendMessage());
    inputField.addActionListener(e -> sendMessage());

    setVisible(true);
    connectToServer();
  }

  private void connectToServer() {
    try {
      socket = new Socket("127.0.0.1", 12345);
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      writer = new PrintWriter(socket.getOutputStream(), true);

      String serverMessage = reader.readLine(); // "Enter your nickname:"
      System.out.println(serverMessage);

      String nickname = JOptionPane.showInputDialog(this, "Enter your nickname:");
      writer.println(nickname);

      new Thread(() -> {
        String line;
        try {
          while ((line = reader.readLine()) != null) {
            chatArea.append(line + "\n");
          }
        } catch (IOException e) {
          chatArea.append("Disconnected from server.\n");
        }
      }).start();

    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Unable to connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
      System.exit(0);
    }
  }

  private void sendMessage() {
    String message = inputField.getText();
    if (!message.trim().isEmpty()) {
      writer.println(message);
      chatArea.append("Me: " + message + "\n"); // Show own message
      inputField.setText("");
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(ClientGui::new);
  }
}
