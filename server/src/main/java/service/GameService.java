package service;
import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MySQLGameDAO;
import model.GameData;
import request.*;

public class GameService {

    private GameDAO gameDAO;

    {
        try {
            gameDAO = new MySQLGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public CreateResult create(CreateRequest createRequest) throws DataAccessException {
        if (createRequest.gameName() == null) {
            throw new DataAccessException("Error: bad request");
        }

        ListResult listResult = new ListResult(gameDAO.listGames());

        int gameID = listResult.games().size()+1;

        GameData newGame = new GameData(gameID, null, null, createRequest.gameName(), new ChessGame());

        gameDAO.createGame(newGame);

        CreateResult result = new CreateResult(gameID);

        return result;
    }

    public JoinResult join(String username, JoinRequest joinRequest) throws DataAccessException {
        if (username == null || joinRequest.playerColor() == null || !(joinRequest.gameID() > 0)) {
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

    public void updateGame(UpdateRequest updateRequest) throws DataAccessException {
        if (updateRequest.game() == null || updateRequest.gameID() == 0) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = gameDAO.getGame(updateRequest.gameID());

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        } else {
            GameData updatedGame = new GameData(
                    game.gameID(),
                    updateRequest.whiteUsername(),
                    updateRequest.blackUsername(),
                    game.gameName(),
                    updateRequest.game()
            );

            gameDAO.updateGame(updateRequest.gameID(), updatedGame);
        }
    }

    public ListResult list(ListRequest listRequest) throws DataAccessException {
        if (listRequest.authToken() == null) {
            throw new DataAccessException("Error: bad request");
        }

        ListResult result = new ListResult(gameDAO.listGames());

        return result;
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }
}
