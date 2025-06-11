package server.websocket;

import client.ResponseException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            String username = command.getAuthToken();

            // saveSession(command.getGameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, command);
                case MAKE_MOVE -> makeMove(session, username, new Gson().fromJson(message, MakeMoveCommand.class));
                case LEAVE -> leaveGame(session, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session.getRemote(), "Error: unauthorized");
        } catch (Exception ex) {
            sendMessage(session.getRemote(), "Error: " + ex.getMessage());
        }
    }

    private void sendMessage(RemoteEndpoint remote, String message) throws IOException {
        remote.sendString(new Gson().toJson(new ErrorMessage(message)));
    }

    private void connect(Session session, UserGameCommand command) throws IOException {
        connections.add(command.getAuthToken(), session);
        var message = String.format("%s has joined the game.", command.getAuthToken());
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification);
    }

    private void leaveGame(Session session, UserGameCommand command) throws IOException {
        connections.remove(command.getAuthToken());
        var message = String.format("%s left the game.", command.getAuthToken());
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws IOException {
        var message = String.format("%s made the move: " + command.getMove().toString(), username);
        var notification = new NotificationMessage(message);
        connections.broadcast(username, notification);

        String game = command.getBoard();
        connections.broadcast(username, new LoadGameMessage("\n" + game));
    }

    private void resign(Session session, String username, UserGameCommand command) throws IOException {
        var message = String.format("%s has resigned!", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(username, notification);
    }
}