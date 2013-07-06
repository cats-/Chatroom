package cats.chat.core.connection.event;

import cats.chat.core.connection.Connection;
import cats.chat.core.message.Message;
import java.io.Serializable;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 2:01 PM
 */
public class ConnectionEvent implements Serializable{

    public static final int MESSAGE_RECEIVED = 0;
    public static final int MESSAGE_SENT = 1;
    public static final int CONNECTION_OPENED = 2;
    public static final int CONNECTION_CLOSED = 3;

    private final Connection connection;
    private final Message message;
    private final int id;
    private final long time;

    public ConnectionEvent(final Connection connection, final Message message, final int id){
        this.connection = connection;
        this.message = message;
        this.id = id;

        time = System.currentTimeMillis();
    }

    public ConnectionEvent(final Connection connection, final int id){
        this(connection, null, id);
    }

    public Connection getConnection(){
        return connection;
    }

    public Message getMessage(){
        return message;
    }

    public long getTime(){
        return time;
    }

    public int getID(){
        return id;
    }
}
