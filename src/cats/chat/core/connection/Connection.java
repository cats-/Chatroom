package cats.chat.core.connection;

import cats.chat.core.connection.event.ConnectionEvent;
import cats.chat.core.connection.event.ConnectionListener;
import cats.chat.core.message.ClientMessage;
import cats.chat.core.message.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import static cats.chat.core.connection.event.ConnectionEvent.*;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 1:49 PM
 */
public final class Connection extends Thread implements Serializable, Runnable{

    private final Socket socket;
    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    private final List<ConnectionListener> listeners;

    public Connection(final Socket socket) throws IOException {
        this.socket = socket;

        listeners = new LinkedList<>();

        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();

        input = new ObjectInputStream(socket.getInputStream());

        setPriority(MAX_PRIORITY);
        start();
    }

    public boolean isConnected(){
        return socket != null && socket.isConnected();
    }

    public boolean isClosed(){
        return socket != null && socket.isClosed();
    }

    public boolean addConnectionListener(final ConnectionListener listener){
        return listeners.add(listener);
    }

    public boolean removeConnectionListener(final ConnectionListener listener){
        return listeners.remove(listener);
    }

    protected void fireConnectionListeners(final ConnectionEvent e){
        switch(e.getID()){
            case MESSAGE_RECEIVED:
                for(final ConnectionListener l : listeners)
                    l.onMessageReceived(e);
                break;
            case MESSAGE_SENT:
                for(final ConnectionListener l : listeners)
                    l.onMessageSent(e);
                break;
            case CONNECTION_CLOSED:
                for(final ConnectionListener l : listeners)
                    l.onConnectionClosed(e);
                break;
            case CONNECTION_OPENED:
                for(final ConnectionListener l : listeners)
                    l.onConnectionOpened(e);
                break;
        }
    }

    public void run(){
        fireConnectionListeners(new ConnectionEvent(this, CONNECTION_OPENED));
        while(socket.isConnected()){
            try{
                final Message message = (Message)input.readObject();
                fireConnectionListeners(new ConnectionEvent(this, message, MESSAGE_RECEIVED));
            }catch(Exception ex){
                break;
            }
        }
        fireConnectionListeners(new ConnectionEvent(this, CONNECTION_CLOSED));
    }

    public void send(final Message message) throws IOException{
        output.writeObject(message);
        output.flush();
    }

    public void send(final String message) throws IOException{
        send(new ClientMessage(message));
    }

    public Socket getSocket(){
        return socket;
    }

    public ObjectInputStream getInput(){
        return input;
    }

    public ObjectOutputStream getOutput(){
        return output;
    }

    public void close() throws IOException{
        input.close();
        output.close();
        socket.close();
    }

    public String toString(){
        return !isConnected() ? "Not connected" : socket.toString();
    }

}
