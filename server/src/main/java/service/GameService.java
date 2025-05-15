package service;
import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryGameDAO;
import model.GameData;
import service.request.*;

public class GameService {

    private MemoryGameDAO gameDAO = new MemoryGameDAO();

    public CreateResult create(CreateRequest createRequest) throws DataAccessException {
        int gameID = (int) Math.floor(Math.random()*100000000);

        GameData newGame = new GameData(gameID, null, null, createRequest.gameName(), new ChessGame());

        gameDAO.createGame(newGame);

        CreateResult result = new CreateResult(gameID);

        return result;
    }

    public JoinResult join(JoinRequest loginRequest) {
        return null;
    }

    public ListResult list(ListRequest listRequest) {
        return null;
    }
}
