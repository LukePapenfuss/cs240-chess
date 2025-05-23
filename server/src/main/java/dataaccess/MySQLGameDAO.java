package dataaccess;

import model.GameData;
import service.request.GameInfo;

import java.util.ArrayList;

public class MySQLGameDAO implements GameDAO {

    public void createGame(GameData gameData) throws DataAccessException {

    }

    public GameData getGame(int gameID) throws DataAccessException {

        return null;
    }

    public ArrayList<GameInfo> listGames() throws DataAccessException {

        return null;
    }

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException {

    }

    public void clear() {

    }

}
