package cats.chat.core.message;

import java.io.Serializable;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 3:06 PM
 */
public class ServerMessage extends Message implements Serializable{

    public ServerMessage(final String message){
        super("SERVER: " + message, SERVER);
    }
}
