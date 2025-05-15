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
        int gameID = (int) Math.floor(Math.random()*100000000);

        GameData newGame = new GameData(gameID, null, null, createRequest.gameName(), new ChessGame());

        gameDAO.createGame(newGame);

        CreateResult result = new CreateResult(gameID);

        return result;
    }

    public JoinResult join(String authToken, JoinRequest joinRequest) throws DataAccessException {
        GameData game = gameDAO.getGame(joinRequest.gameID());

        if (authToken == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        } else {
            if ((Objects.equals(joinRequest.playerColor(), "WHITE") && game.whiteUsername() == null) ||
                    (Objects.equals(joinRequest.playerColor(), "BLACK") && game.blackUsername() == null)) {

                GameData updatedGame = new GameData(
                        joinRequest.gameID(),
                        (joinRequest.playerColor().equals("WHITE") ? authToken : game.whiteUsername()),
                        (joinRequest.playerColor().equals("BLACK") ? authToken : game.blackUsername()),
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
        ListResult result = new ListResult(gameDAO.listGames());

        return result;
    }

    public void clear() {
        gameDAO.clear();
    }
}
