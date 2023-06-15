import javax.jms.*;
import javax.naming.*;
import javax.swing.*;
import java.awt.*;

public class JMSChat {

    private Connection con; // Połączenie JMS
    private Session session; // Sesja JMS
    private TextMessage msg; // Utworzenie wiadomości tekstowej
    private JTextArea chatArea; // Obszar tekstowy do wyświetlania wiadomości
    private JTextField chatInput; // Pole tekstowe do wprowadzania wiadomości
    private String userName; // Nazwa użytkownika
    private Topic topic; // Temat czatu

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JMSChat chat1 = new JMSChat();
            chat1.createAndShowGUI();

            JMSChat chat2 = new JMSChat();
            chat2.createAndShowGUI();
        });
    }

    //Tworzenie GUI
    private void createAndShowGUI() {
        JFrame frame = new JFrame("Simple JMS Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        chatInput = new JTextField();
        chatInput.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.getContentPane().add(inputPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        showInputDialog();
        setupMessaging();
    }

    private void showInputDialog() {
        userName = JOptionPane.showInputDialog("Enter your nickname:");
        if (userName == null || userName.trim().isEmpty()) {
            closeChat();
        }
    }

    private void setupMessaging() {
        try {
            Context ctx = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory"); // Pobranie fabryki połączeń JMS
            topic = (Topic) ctx.lookup("topic1"); // Pobranie tematu czatu

            con = factory.createConnection(); // Utworzenie połączenia JMS
            session = con.createSession(false, Session.AUTO_ACKNOWLEDGE); // Utworzenie sesji JMS (bez transakcyjności, automatyczne potwierdzanie)
            con.start(); // Rozpoczęcie połączenia

            createDurableSubscriber(topic, userName);
            sendJoinMessage(userName);
        } catch (NamingException | JMSException e) {
            e.printStackTrace();
            closeChat();
        }
    }

    private void createDurableSubscriber(Topic topic, String subscriberName) {
        try {
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberName); // Utworzenie trwałego odbiorcy wiadomości na temacie
            consumer.setMessageListener(new SampleListener());
        } catch (JMSException e) {
            e.printStackTrace();
            closeChat();
        }
    }

    private void sendJoinMessage(String username) {
        try {
            MessageProducer producer = session.createProducer(topic); // Utworzenie nadawcy wiadomości na temacie
            msg = session.createTextMessage();
            msg.setText(username + " joined the chat!");
            producer.send(msg);
        } catch (JMSException e) {
            e.printStackTrace();
            closeChat();
        }
    }

    private void sendMessage() {
        String messageToSend = chatInput.getText().trim();
        if (!messageToSend.isEmpty()) {
            try {
                if (messageToSend.equalsIgnoreCase("exit")) { // Sprawdzenie, czy wiadomość to "exit"
                    msg.setText(userName + " left the chat :(");
                    closeChat();
                } else {
                    msg.setText("[" + userName + "]: " + messageToSend);
                }
                MessageProducer producer = session.createProducer(topic); // Utworzenie nadawcy wiadomości na temacie
                producer.send(msg); // Wysłanie wiadomości
                chatInput.setText(""); // Wyczyszczenie pola tekstowego
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeChat() {
        try {
            if (con != null) {
                if (msg != null) {
                    msg.setText(userName + " left the chat :(");
                    MessageProducer producer = session.createProducer(topic); // Utworzenie nadawcy wiadomości na temacie
                    producer.send(msg); // Wysłanie wiadomości
                }
                con.close(); // Zamknięcie połączenia JMS
            }
            System.exit(0); // Zakończenie programu
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    class SampleListener implements MessageListener {
        @Override
        public void onMessage(Message message) {
            try {
                String text = ((TextMessage) message).getText(); // Pobranie treści wiadomości
                chatArea.append(text + "\n"); // Wyświetlenie wiadomości w obszarze tekstowym
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
