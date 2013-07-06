package cats.chat.client;

import cats.chat.core.ServerConstants;
import cats.chat.core.connection.Connection;
import cats.chat.core.connection.event.ConnectionEvent;
import cats.chat.core.connection.event.ConnectionListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 9:15 PM
 */
public class Client extends JFrame implements ServerConstants, ActionListener, ConnectionListener{

    private Connection connection;

    private final JTextArea chatArea;
    private final JScrollPane chatScroll;
    private final JTextField chatBox;

    private final String name;

    public Client(){
        super("Client");
        setLayout(new BorderLayout());
        addWindowListener(
                new WindowAdapter(){
                    public void windowClosing(final WindowEvent e){
                        if(connection != null && connection.isConnected()){
                            try{
                                connection.close();
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }

                    }
                }
        );

        name = JOptionPane.showInputDialog(this, "Enter a name");

        chatBox = new JTextField();
        chatBox.addActionListener(this);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        chatScroll = new JScrollPane(chatArea);

        add(chatScroll, BorderLayout.CENTER);
        add(chatBox, BorderLayout.SOUTH);
    }

    public void onConnectionOpened(final ConnectionEvent e){

    }

    public void onConnectionClosed(final ConnectionEvent e){
        append("Connection closed; you are no longer connected");
    }

    public void onMessageReceived(final ConnectionEvent e){
        append(e.getMessage().getMessage());
    }

    public void onMessageSent(final ConnectionEvent e){

    }

    public void start(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);
        try{
            append("Starting connection...");
            connection = new Connection(new Socket(HOST, PORT));
            connection.addConnectionListener(this);
            append("Finished initializing connection...");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void append(final String msg){
        SwingUtilities.invokeLater(
                () -> {
                    chatArea.append(msg + "\n");
                    chatArea.repaint();
                }
        );
    }

    public void actionPerformed(final ActionEvent e){
        final Object source = e.getSource();
        if(source.equals(chatBox)){
            if(connection == null || !connection.isConnected())
                return;
            final String text = chatBox.getText().trim();
            if(text.isEmpty())
                return;
            if(connection.isClosed())
                return;
            try{
                connection.send(String.format("%s: %s", name, text));
            }catch(Exception ex){
                ex.printStackTrace();
            }finally{
                SwingUtilities.invokeLater(
                        () -> {
                            chatBox.setText("");
                            chatBox.repaint();
                        }
                );
            }
        }
    }

    public static void main(String args[]){
        final Client client = new Client();
        client.start();
    }
}
