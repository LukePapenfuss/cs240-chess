package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.Server;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private Server server;

    public WebSocketHandler(Server server) {
        this.server = server;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

            String username = "";
            try {
                username = server.authorize(command.getAuthToken());
            } catch (DataAccessException e) {
                throw new UnauthorizedException("unauthorized");
            }

            //ListResult listResult = server.list(command.getAuthToken(), new ListRequest(command.getAuthToken()));

            //if (command.getGameID() > listResult.games().size()) { throw new Exception("Error: invalid game ID."); }

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, new Gson().fromJson(message, ConnectCommand.class));
                case MAKE_MOVE -> makeMove(session, username, new Gson().fromJson(message, MakeMoveCommand.class));
                case LEAVE -> leaveGame(session, username, command);
                case RESIGN -> resign(session, username, command);
            }
        } catch (UnauthorizedException ex) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: unauthorized")));
        } catch (Exception ex) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage(ex.getMessage())));
        }
    }

    private void connect(Session session, String username, ConnectCommand command) throws IOException {
        connections.add(command.getAuthToken(), session);
        var message = command.getColor() != null ? String.format("%s has joined the game as " + (command.getColor() == ChessGame.TeamColor.WHITE ? "white." : "black."),
                username) : String.format("%s is observing.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification);
        session.getRemote().sendString(new Gson().toJson(new LoadGameMessage(command.getGameID(), null)));
    }

    private void leaveGame(Session session, String username, UserGameCommand command) throws IOException {
        connections.remove(command.getAuthToken());
        var message = String.format("%s left the game.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification);
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws IOException {
        var message = String.format("%s made the move: " + command.getMove().toString(), username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification);

        GameData gameData;
        try {
            gameData = server.getGame(command.getGameID(), command.getAuthToken());
        } catch (DataAccessException e) {
            throw new IOException("ERROR");
        }

        ChessGame game = gameData.game();

        connections.broadcast(command.getAuthToken(), new LoadGameMessage(command.getGameID(), command.getMove()));

        if (game.isInCheckmate(game.getTeamTurn())) {

            String checkmateMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                    gameData.whiteUsername() : gameData.blackUsername()) + " is in checkmate.";

            connections.broadcast(null, new NotificationMessage(checkmateMessage));

            game.finishGame();

        } else if (game.isInCheck(game.getTeamTurn())) {

            String checkMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                    gameData.whiteUsername() : gameData.blackUsername()) + " is in check.";

            connections.broadcast(null, new NotificationMessage(checkMessage));

        } else if (game.isInStalemate(game.getTeamTurn())) {

            String staleMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                    gameData.whiteUsername() : gameData.blackUsername()) + " is in stalemate.";

            connections.broadcast(null, new NotificationMessage(staleMessage));

        }

    }

    private void resign(Session session, String username, UserGameCommand command) throws IOException {
        var message = String.format("%s has resigned!", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification);
    }
}