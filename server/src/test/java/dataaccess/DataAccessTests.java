package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.*;
import service.UserService;
import service.request.*;

import javax.xml.crypto.Data;

public class DataAccessTests {

    @Test
    @DisplayName("Positive Clear Auth Test ")
    public void positiveClearAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        Assertions.assertDoesNotThrow(() -> dao.clear());
    }

    @Test
    @DisplayName("Positive Create Auth Test ")
    public void positiveCreateAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");
        AuthData authData2 = new AuthData("token2", "name2");

        Assertions.assertDoesNotThrow(() -> dao.createAuth(authData));
        Assertions.assertDoesNotThrow(() -> dao.createAuth(authData2));
    }

    @Test
    @DisplayName("Negative Create Auth Test ")
    public void negativeCreateAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");
        AuthData authData2 = new AuthData("token", "name");

        dao.createAuth(authData);
        Assertions.assertThrows(DataAccessException.class, () -> dao.createAuth(authData2));
    }

    @Test
    @DisplayName("Positive Get Auth Test ")
    public void positiveGetAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");
        AuthData authData2 = new AuthData("token2", "name2");

        dao.createAuth(authData);
        dao.createAuth(authData2);

        AuthData result = dao.getAuth("token");
        AuthData result2 = dao.getAuth("token2");

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result2);
        Assertions.assertEquals("name", result.username());
        Assertions.assertEquals("name2", result2.username());
    }

    @Test
    @DisplayName("Negative Get Auth Test ")
    public void negativeGetAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");

        dao.createAuth(authData);

        AuthData result = dao.getAuth("Doesn't Exist");

        Assertions.assertNull(result);
    }

    @Test
    @DisplayName("Positive Delete Auth Test ")
    public void positiveDeleteAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");
        AuthData authData2 = new AuthData("token2", "name2");

        dao.createAuth(authData);
        dao.createAuth(authData2);

        dao.deleteAuth(authData);

        AuthData result = dao.getAuth("token");
        AuthData result2 = dao.getAuth("token2");

        Assertions.assertNull(result);
        Assertions.assertNotNull(result2);
        Assertions.assertEquals("name2", result2.username());
    }

    @Test
    @DisplayName("Negative Delete Auth Test ")
    public void negativeDeleteAuthTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");
        AuthData authData2 = new AuthData(null, null);

        dao.createAuth(authData);

        dao.deleteAuth(authData2);

        AuthData result = dao.getAuth("token");

        Assertions.assertNotNull(result);
    }

}