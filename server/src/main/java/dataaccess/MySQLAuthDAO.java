package dataaccess;

import model.AuthData;

import java.sql.*;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySQLAuthDAO implements AuthDAO {

    DataConnector connector = new DataConnector();

    public MySQLAuthDAO() throws DataAccessException {
        connector.configureDatabase(createStatements);
    }

    public void createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        var id = connector.executeUpdate(statement, authData.username(), authData.authToken());
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (Objects.equals(rs.getString("authToken"), authToken)) {
                            return new AuthData(rs.getString("authToken"), rs.getString("username"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    public void deleteAuth(AuthData authData) throws DataAccessException  {
        var statement = "DELETE FROM auth WHERE authToken=?";
        connector.executeUpdate(statement, authData.authToken());
    }

    public String getUsername(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if (Objects.equals(rs.getString("authToken"), authToken)) {
                            return rs.getString("username");
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error: Unable to read data: %s", e.getMessage()));
        }

        return null;
    }

    public void clear() throws DataAccessException {
        var statement = "TRUNCATE auth";
        connector.executeUpdate(statement);
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  auth (
              `username` varchar(256) NOT NULL,
              `authToken` varchar(256) NOT NULL,
              PRIMARY KEY (authToken),
              INDEX(authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

}
