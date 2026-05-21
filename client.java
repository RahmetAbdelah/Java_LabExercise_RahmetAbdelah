package chatapp;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class client {
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    private JFrame frame = new JFrame("letschat");
    private DefaultListModel<String> messageListModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(messageListModel);
    private JTextField textField = new JTextField();
    private JButton sendButton = new JButton("Send");
    private final Color COLOR_BG = new Color(30, 31, 34);
    private final Color COLOR_SURFACE = new Color(43, 45, 49);
    private final Color COLOR_ACCENT = new Color(88, 101, 242);
    private final Color COLOR_TEXT = new Color(242, 243, 245);
    private final Color COLOR_MUTED = new Color(148, 155, 164);

    public client() {
        username = JOptionPane.showInputDialog(frame, "Enter your username:", "Join Chat", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            username = "User_" + (int)(Math.random() * 1000);
        }

        messageList.setBackground(COLOR_BG);
        messageList.setSelectionBackground(COLOR_BG);
        messageList.setSelectionForeground(COLOR_TEXT);
        messageList.setCellRenderer(new ChatMessageRenderer());

        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_SURFACE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("● Global Chatroom");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(35, 165, 90));

        JLabel userLabel = new JLabel(username, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(COLOR_TEXT);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userLabel, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout(12, 0));
        bottomPanel.setBackground(COLOR_BG);
        bottomPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(COLOR_SURFACE);
        textField.setForeground(COLOR_TEXT);
        textField.setCaretColor(COLOR_TEXT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_SURFACE, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(COLOR_ACCENT);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        bottomPanel.add(textField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(headerPanel, BorderLayout.NORTH);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(500, 650);
        frame.setLocationRelativeTo(null);

        ActionListener sendAction = e -> {
            String text = textField.getText().trim();
            if (!text.isEmpty()) {
                out.println("[" + username + "]: " + text);
                textField.setText("");
            }
            textField.requestFocusInWindow();
        };
        textField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);
    }

    private class ChatMessageRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String text = (String) value;

            JPanel cellPanel = new JPanel(new BorderLayout());
            cellPanel.setBackground(COLOR_BG);
            cellPanel.setBorder(new EmptyBorder(6, 16, 6, 16));

            JLabel messageLabel = new JLabel();
            messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            if (text.startsWith("-->") || text.endsWith("lost.")) {
                messageLabel.setText(text);
                messageLabel.setForeground(COLOR_MUTED);
                messageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                cellPanel.add(messageLabel, BorderLayout.CENTER);
            } else {
                int splitIdx = text.indexOf("]:");
                if (splitIdx != -1) {
                    String sender = text.substring(0, splitIdx + 1);
                    String body = text.substring(splitIdx + 2);

                    messageLabel.setText("<html><b style='color:#5865F2;'>" + sender + "</b><span style='color:#F2F3F4;'>" + body + "</span></html>");
                } else {
                    messageLabel.setText(text);
                    messageLabel.setForeground(COLOR_TEXT);
                }

                JPanel bubble = new JPanel(new BorderLayout());
                bubble.setBackground(COLOR_SURFACE);
                bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
                bubble.add(messageLabel, BorderLayout.CENTER);

                cellPanel.add(bubble, BorderLayout.WEST);
            }

            return cellPanel;
        }
    }

    private void runChat() {
        String serverAddress = "localhost";
        int port = 12345;

        try {
            Socket socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("--> " + username + " joined the chat.");

            Thread listenThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                            messageListModel.addElement(finalLine);
                            messageList.ensureIndexIsVisible(messageListModel.size() - 1);
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> messageListModel.addElement("Connection to server lost."));
                }
            });
            listenThread.start();

        } catch (IOException e) {
            messageListModel.addElement("Could not connect to server.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            client client1 = new client();
            client1.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client1.frame.setVisible(true);
            client1.runChat();
        });
    }
}