package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import service.request.GameInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLGameDAO implements GameDAO {

    DataConnector connector = new DataConnector();

    public MySQLGameDAO() throws DataAccessException {
        connector.configureDatabase(createStatements);
    }

    public void createGame(GameData gameData) throws DataAccessException {
        var statement = "INSERT INTO game (gameID, gameName, whiteUsername, blackUsername, game) VALUES (?, ?, ?, ?, ?)";

        var json = new Gson().toJson(gameData.game(), ChessGame.class);

        var id = connector.executeUpdate(statement, gameData.gameID(), gameData.gameName(), gameData.whiteUsername(), gameData.blackUsername(), json);
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, gameName, whiteUsername, blackUsername, game FROM game";
            var ps = conn.prepareStatement(statement);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (Objects.equals(rs.getInt("gameID"), gameID)) {
                        return new GameData(rs.getInt("gameID"), rs.getString("whiteUsername"),
                                rs.getString("blackUsername"), rs.getString("gameName"),
                                new Gson().fromJson(rs.getString("game"), ChessGame.class));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    public ArrayList<GameInfo> listGames() throws DataAccessException {
        ArrayList<GameInfo> infos = new ArrayList<>();

        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, gameName, whiteUsername, blackUsername, game FROM game";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        infos.add(new GameInfo(rs.getInt("gameID"), rs.getString("whiteUsername"),
                                rs.getString("blackUsername"), rs.getString("gameName")));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()));
        }

        return infos;
    }

    public void updateGame(int gameID, GameData newGameData) throws DataAccessException {
        var statement = "UPDATE game SET gameID = ?, whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        var json = new Gson().toJson(newGameData.game(), ChessGame.class);

        connector.executeUpdate(statement, newGameData.gameID(), newGameData.whiteUsername(), newGameData.blackUsername(),
                newGameData.gameName(), json, gameID);
    }

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE game";
        connector.executeUpdate(statement);
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  game (
              `gameID` int NOT NULL,
              `gameName` varchar(256) NOT NULL,
              `whiteUsername` varchar(256) DEFAULT NULL,
              `blackUsername` varchar(256) DEFAULT NULL,
              `game` TEXT DEFAULT NULL,
              PRIMARY KEY (gameID),
              INDEX(gameName),
              INDEX(whiteUsername),
              INDEX(blackUsername)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
}
