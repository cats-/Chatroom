package cats.chat.core.message;

import java.io.Serializable;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 3:01 PM
 */
public class ClientMessage extends Message implements Serializable{

    public ClientMessage(final String msg){
        super(msg, CLIENT);
    }
}
