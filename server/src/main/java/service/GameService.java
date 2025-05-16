package service;
import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryGameDAO;
import model.GameData;
import service.request.*;

import java.util.Objects;

public class GameService {

    private MemoryGameDAO gameDAO = new MemoryGameDAO();

    public CreateResult create(CreateRequest createRequest) throws DataAccessException {
        if (createRequest.gameName() == null) {
            throw new DataAccessException("Error: bad request");
        }

        int gameID = (int) Math.floor(Math.random()*100000000);

        GameData newGame = new GameData(gameID, null, null, createRequest.gameName(), new ChessGame());

        gameDAO.createGame(newGame);

        CreateResult result = new CreateResult(gameID);

        return result;
    }

    public JoinResult join(String username, JoinRequest joinRequest) throws DataAccessException {
        if (username == null || joinRequest.playerColor() == null || !(joinRequest.gameID() > 10)) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = gameDAO.getGame(joinRequest.gameID());

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        } else {
            if ((joinRequest.playerColor() == ChessGame.TeamColor.WHITE && game.whiteUsername() == null) ||
                    (joinRequest.playerColor() == ChessGame.TeamColor.BLACK && game.blackUsername() == null)) {

                GameData updatedGame = new GameData(
                        joinRequest.gameID(),
                        (joinRequest.playerColor().equals(ChessGame.TeamColor.WHITE) ? username : game.whiteUsername()),
                        (joinRequest.playerColor().equals(ChessGame.TeamColor.BLACK) ? username : game.blackUsername()),
                        game.gameName(),
                        game.game()
                );

                gameDAO.updateGame(joinRequest.gameID(), updatedGame);

                JoinResult result = new JoinResult();

                return result;
            } else {
                throw new DataAccessException("Error: already taken");
            }
        }
    }

    public ListResult list(ListRequest listRequest) throws DataAccessException {
        if (listRequest.authToken() == null) {
            throw new DataAccessException("Error: bad request");
        }

        ListResult result = new ListResult(gameDAO.listGames());

        return result;
    }

    public void clear() {
        gameDAO.clear();
    }
}
