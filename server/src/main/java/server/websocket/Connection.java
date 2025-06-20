package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public String visitorName;
    public Session session;
    public int gameID;

    public Connection(String visitorName, Session session, int gameID) {
        this.visitorName = visitorName;
        this.session = session;
        this.gameID = gameID;
    }

    public synchronized void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}