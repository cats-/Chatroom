package cats.chat.server;

import cats.chat.core.ServerConstants;
import cats.chat.core.connection.Connection;
import cats.chat.core.connection.event.ConnectionEvent;
import cats.chat.core.connection.event.ConnectionListener;
import cats.chat.core.message.Message;
import cats.chat.core.message.ServerMessage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 3:12 PM
 */
public class Server extends JFrame implements ActionListener, Runnable, ServerConstants, ConnectionListener{

    private class ConnectionList extends JList<Connection>{

        private class Renderer extends DefaultListCellRenderer {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focused){
                final Component c = super.getListCellRendererComponent(list, value, index, selected, focused);
                if(index < 0)
                    return c;
                final Connection connection = model.get(index);
                final JLabel label = (JLabel)c;
                label.setBackground(connection.isConnected() ? Color.GREEN : Color.RED);
                return label;
            }
        }

        private final DefaultListModel<Connection> model;
        private final List<Connection> connections;
        private final Renderer renderer;

        private ConnectionList(){
            connections = new LinkedList<>();
            model = new DefaultListModel<>();
            setModel(model);

            renderer = new Renderer();
            setCellRenderer(renderer);
        }

        private void addConnection(final Connection connection){
            SwingUtilities.invokeLater(
                    () -> {
                        connections.add(connection);
                        model.addElement(connection);
                        repaint();
                        updateScrollBorder();
                    }
            );
        }

        private void removeConnection(final Connection connection){
            SwingUtilities.invokeLater(
                    () -> {
                        connections.remove(connection);
                        model.removeElement(connection);
                        repaint();
                        updateScrollBorder();
                    }
            );
        }

        public void processMouseEvent(final MouseEvent e){
            super.processMouseEvent(e);
            final Point p = e.getPoint();
            final int i = locationToIndex(p);
            if(i < 0)
                return;
            if(e.getButton() != MouseEvent.BUTTON3)
                return;
            final String string = model.get(i).toString();
            SwingUtilities.invokeLater(
                    () -> {
                        titleItem.setText(string);
                        titleItem.repaint();
                        setSelectedIndex(i);
                        popup.show(this, p.x, p.y);
                    }
            );
        }
    }

    private final ConnectionList list;
    private final JScrollPane scroll;
    private final TitledBorder scrollBorder;

    private final JTextArea console;
    private final JScrollPane consoleScroll;

    private final JPopupMenu popup;
    private final JMenuItem titleItem;
    private final JMenuItem killItem;
    private final JMenuItem sendItem;

    private ServerSocket server;

    public Server(){
        super("Server Details");
        setLayout(new BorderLayout());

        popup = new JPopupMenu();

        titleItem = new JMenuItem();
        titleItem.setEnabled(false);

        killItem = new JMenuItem("Kill");
        killItem.addActionListener(this);

        sendItem = new JMenuItem("Send");
        sendItem.addActionListener(this);

        popup.add(titleItem);
        popup.addSeparator();
        popup.add(killItem);
        popup.add(sendItem);

        list = new ConnectionList();

        scrollBorder = new TitledBorder("0 Connections");

        scroll = new JScrollPane(list);
        scroll.setBorder(scrollBorder);

        console = new JTextArea();
        console.setLineWrap(true);
        console.setWrapStyleWord(true);
        console.setEditable(false);

        consoleScroll = new JScrollPane(console);
        consoleScroll.setBorder(new TitledBorder("Log Area"));

        add(scroll, BorderLayout.NORTH);
        add(consoleScroll, BorderLayout.CENTER);
    }

    private void log(final String msg){
        SwingUtilities.invokeLater(
                () -> {
                    console.append(">> " + msg + "\n");
                    console.repaint();
                }
        );
    }

    public void onConnectionOpened(final ConnectionEvent e){
        log("Connection opened: " + e.getConnection());
    }

    public void onConnectionClosed(final ConnectionEvent e){
        final Connection connection = e.getConnection();
        list.removeConnection(connection);
        updateScrollBorder();
        log("Connection closed: " + connection);
    }

    public void onMessageReceived(final ConnectionEvent e){
        final Message message = e.getMessage();
        send(message);
        log("Message recieved: " + message);
    }

    private void send(final String msg){
        send(new ServerMessage(msg));
    }

    private void send(final Connection connection, final String msg){
        final ServerMessage message = new ServerMessage(msg);
        try{
            connection.send(message);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void send(final Message message){
        list.connections.stream().forEach(
                c -> {
                    try{
                        c.send(message);
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
        );
    }

    public void onMessageSent(final ConnectionEvent e){
        log("Message sent: " + e.getMessage());
    }

    public void run(){
        try{
            log("Initializing server...");
            server = new ServerSocket(PORT);
            log("Started Server: " + server);
            while(true){
                try{
                    final Socket socket = server.accept();
                    final Connection connection = new Connection(socket);
                    connection.addConnectionListener(this);
                    list.addConnection(connection);
                    updateScrollBorder();
                    send(connection, "Welcome to CatsChat");
                    send(String.format("%d total users online", list.connections.size()));
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updateScrollBorder(){
        SwingUtilities.invokeLater(
                () -> {
                    scrollBorder.setTitle(String.format("%d Connections", list.model.size()));
                    scroll.repaint();
                }
        );
    }

    public void start(){
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(700, 500);
        setVisible(true);
        final Thread t = new Thread(this);
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
    }

    public void actionPerformed(final ActionEvent e){
        final Object source = e.getSource();
        if(source.equals(killItem)){
            final int index = list.getSelectedIndex();
            if(index < 0)
                return;
            list.removeConnection(list.model.get(index));
        }else if(source.equals(sendItem)){
            final int index = list.getSelectedIndex();
            if(index < 0)
                return;
            final Connection connection = list.model.get(index);
            final String input = JOptionPane.showInputDialog(this, "Enter data to send to: " + connection);
            if(input == null || input.isEmpty())
                return;
            try{
                connection.send(new ServerMessage(input));
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    public static void main(String args[]){
        final Server server = new Server();
        server.start();
    }
}
