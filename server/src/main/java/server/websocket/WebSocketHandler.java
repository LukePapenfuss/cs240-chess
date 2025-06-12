package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.sun.source.tree.WhileLoopTree;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            String username = command.getAuthToken();

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, new Gson().fromJson(message, ConnectCommand.class));
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

    private void connect(Session session, ConnectCommand command) throws IOException {
        connections.add(command.getAuthToken(), session);
        var message = command.getColor() != null ? String.format("%s has joined the game as " + (command.getColor() == ChessGame.TeamColor.WHITE ? "white." : "black."),
                command.getAuthToken()) : String.format("%s is observing.", command.getAuthToken());
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

        ChessGame game = command.getGame().game();
        connections.broadcast(username, new LoadGameMessage(command.getGameID(), command.getMove()));

        if (game.isInCheckmate(game.getTeamTurn())) {

            String checkmateMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                    command.getGame().whiteUsername() : command.getGame().blackUsername()) + " is in checkmate.";

            connections.broadcast(null, new NotificationMessage(checkmateMessage));

            command.getGame().game().finishGame();

        } else if (game.isInCheck(game.getTeamTurn())) {

            String checkMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                    command.getGame().whiteUsername() : command.getGame().blackUsername()) + " is in check.";

            connections.broadcast(null, new NotificationMessage(checkMessage));

        } else if (game.isInStalemate(game.getTeamTurn())) {

            String staleMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                    command.getGame().whiteUsername() : command.getGame().blackUsername()) + " is in stalemate.";

            connections.broadcast(null, new NotificationMessage(staleMessage));

        }

    }

    private void resign(Session session, String username, UserGameCommand command) throws IOException {
        var message = String.format("%s has resigned!", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(username, notification);
    }
}