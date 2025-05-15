package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

    private ArrayList<GameData> games = new ArrayList<>();

    public void createGame(GameData gameData) throws DataAccessException {
        games.add(gameData);
    }

    public GameData getGame(int gameID) throws DataAccessException { return null; }

    public ArrayList<GameData> listGames() throws DataAccessException { return null; }

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException {}

}
