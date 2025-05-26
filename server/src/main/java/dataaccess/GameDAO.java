package dataaccess;
import model.GameData;
import service.request.GameInfo;

import java.util.ArrayList;

public interface GameDAO {

    public void createGame(GameData gameData) throws DataAccessException;

    public GameData getGame(int gameID) throws DataAccessException;

    public ArrayList<GameInfo> listGames() throws DataAccessException;

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException;

    public void clear() throws DataAccessException;
}
