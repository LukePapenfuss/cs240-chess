package dataaccess;
import model.GameData;

import javax.xml.crypto.Data;
import java.util.ArrayList;

public interface GameDAO {

    public void createGame(GameData gameData) throws DataAccessException;

    public GameData getGame(int gameID) throws DataAccessException;

    public ArrayList<GameData> listGames() throws DataAccessException;

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException;

}
