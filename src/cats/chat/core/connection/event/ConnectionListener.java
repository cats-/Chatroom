package cats.chat.core.connection.event;

import java.io.Serializable;

/**
 * Instant Messenger
 * Josh
 * 05/07/13
 * 2:01 PM
 */
public interface ConnectionListener extends Serializable{

    public void onMessageSent(final ConnectionEvent e);

    public void onMessageReceived(final ConnectionEvent e);

    public void onConnectionOpened(final ConnectionEvent e);

    public void onConnectionClosed(final ConnectionEvent e);
}
