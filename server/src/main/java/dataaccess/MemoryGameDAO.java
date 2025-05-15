package dataaccess;

import model.GameData;
import service.request.GameInfo;

import java.util.ArrayList;

public class MemoryGameDAO implements GameDAO {

    private ArrayList<GameData> games = new ArrayList<>();

    public void createGame(GameData gameData) throws DataAccessException {
        games.add(gameData);
    }

    public GameData getGame(int gameID) throws DataAccessException { return null; }

    public ArrayList<GameInfo> listGames() throws DataAccessException {
        ArrayList<GameInfo> infos = new ArrayList<>();

        for (int i = 0; i < games.size(); ++i) {
            GameData game = games.get(i);

            infos.add(new GameInfo(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }

        return infos;
    }

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException {}

}
