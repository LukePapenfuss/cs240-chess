package dataaccess;

import model.GameData;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

    private ArrayList<GameData> games = new ArrayList<>();

    public void createGame(GameData gameData) throws DataAccessException {
        games.add(gameData);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        for (int i = 0; i < games.size(); ++i) {
            if (games.get(i).gameID() == gameID) {
                return games.get(i);
            }
        }

        return null;
    }

    public ArrayList<GameData> listGames() throws DataAccessException {
        return games;
    }

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException {
        for (int i = 0; i < games.size(); ++i) {
            if (games.get(i).gameID() == gameID) {
                games.set(i, newGameData);
            }
        }
    }

    public void clear() {
        games = new ArrayList<>();
    }

}
