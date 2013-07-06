package cats.chat.core.message;

import cats.chat.core.connection.Connection;
import java.io.IOException;
import java.io.Serializable;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 1:45 PM
 */
public class Message implements Serializable {

    public static final int CLIENT = 0;
    public static final int SERVER = 1;

    private final String message;
    private final int id;
    private final long time;

    public Message(final String message, final int id){
        this.message = message;
        this.id = id;

        time = System.currentTimeMillis();
    }

    public void send(final Connection connection) throws IOException {
        connection.send(this);
    }

    public String getMessage(){
        return message;
    }

    public int getID(){
        return id;
    }

    public long getTime(){
        return time;
    }

    public String toString(){
        return String.format("%s[%s]", getClass().getSimpleName(), message);
    }
}
