package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import client.ResponseException;
import client.ServerFacade;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import request.UpdateRequest;
import server.Server;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;
import java.util.Objects;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private Server server;
    private final ServerFacade serverFacade;

    public WebSocketHandler(Server server) {
        this.server = server;
        serverFacade = new ServerFacade("http://localhost:8080");
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

            if (command.getGameID() > server.gameCount(command.getAuthToken())) { throw new Exception("Error: invalid game ID."); }

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
        connections.add(command.getAuthToken(), session, command.getGameID());
        var message = command.getColor() != null ? String.format("%s has joined the game as " + (command.getColor() == ChessGame.TeamColor.WHITE ? "white." : "black."),
                username) : String.format("%s is observing.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification, command.getGameID());
        session.getRemote().sendString(new Gson().toJson(new LoadGameMessage(command.getGameID(), null)));
    }

    private void leaveGame(Session session, String username, UserGameCommand command) throws IOException {
        connections.remove(command.getAuthToken());
        var message = String.format("%s left the game.", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification, command.getGameID());
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws IOException {
        GameData gameData;

        try {
            gameData = server.getGame(command.getGameID(), command.getAuthToken());

            ChessGame game = gameData.game();

            if (Objects.equals(username, game.getTeamTurn() == ChessGame.TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername())) {

                try {
                    game.makeMove(command.getMove());

                    try {
                        serverFacade.updateGame(command.getAuthToken(), new UpdateRequest(game, gameData.gameID()));
                        System.out.println("Updated Game");
                    } catch (ResponseException e) {
                        System.out.println("Error: Couldn't update game");
                    }

                    var message = String.format("%s made the move: " + command.getMove().toString(), username);
                    var notification = new NotificationMessage(message);
                    connections.broadcast(command.getAuthToken(), notification, command.getGameID());

                    System.out.println("Broadcast:\n" + game.getBoard().toString());
                    connections.broadcast(null, new LoadGameMessage(command.getGameID(), command.getMove()), command.getGameID());

                    if (game.isInCheckmate(game.getTeamTurn())) {

                        String checkmateMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                                gameData.whiteUsername() : gameData.blackUsername()) + " is in checkmate.";

                        connections.broadcast(null, new NotificationMessage(checkmateMessage), command.getGameID());

                        game.finishGame();

                    } else if (game.isInCheck(game.getTeamTurn())) {

                        String checkMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                                gameData.whiteUsername() : gameData.blackUsername()) + " is in check.";

                        connections.broadcast(null, new NotificationMessage(checkMessage), command.getGameID());

                    } else if (game.isInStalemate(game.getTeamTurn())) {

                        String staleMessage = (game.getTeamTurn() == ChessGame.TeamColor.WHITE ?
                                gameData.whiteUsername() : gameData.blackUsername()) + " is in stalemate.";

                        connections.broadcast(null, new NotificationMessage(staleMessage), command.getGameID());

                    }
                } catch (InvalidMoveException e) {
                    session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: invalid move")));
                }

            } else {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: invalid request")));
            }
        } catch (DataAccessException e) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: game not found")));
        }
    }

    private void resign(Session session, String username, UserGameCommand command) throws IOException {
        var message = String.format("%s has resigned!", username);
        var notification = new NotificationMessage(message);
        connections.broadcast(command.getAuthToken(), notification, command.getGameID());
    }
}