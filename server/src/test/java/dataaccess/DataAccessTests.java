package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.UserService;
import service.request.*;

import javax.xml.crypto.Data;

public class DataAccessTests {

    // AUTH TESTS ---------------------------------------

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

    @Test
    @DisplayName("Positive Get Username Test ")
    public void positiveGetUsernameTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");

        dao.createAuth(authData);

        String result = dao.getUsername("token");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("name", result);
    }

    @Test
    @DisplayName("Negative Get Username Test ")
    public void negativeGetUsernameTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        dao.clear();

        AuthData authData = new AuthData("token", "name");

        dao.createAuth(authData);

        String result = dao.getUsername("Doesn't Exist");

        Assertions.assertNull(result);
    }

    // USER TESTS ---------------------------------------

    @Test
    @DisplayName("Positive Clear User Test ")
    public void positiveClearUserTest() throws DataAccessException {
        MySQLUserDAO dao = new MySQLUserDAO();

        Assertions.assertDoesNotThrow(() -> dao.clear());
    }

    @Test
    @DisplayName("Positive Create User Test ")
    public void positiveCreateUserTest() throws DataAccessException {
        MySQLUserDAO dao = new MySQLUserDAO();

        dao.clear();

        UserData userData = new UserData("name", "pass", "mail");
        UserData userData2 = new UserData("name2", "pass2", "mail2");

        Assertions.assertDoesNotThrow(() -> dao.createUser(userData));
        Assertions.assertDoesNotThrow(() -> dao.createUser(userData2));
    }

    @Test
    @DisplayName("Negative Create User Test ")
    public void negativeCreateUserTest() throws DataAccessException {
        MySQLUserDAO dao = new MySQLUserDAO();

        dao.clear();

        UserData userData = new UserData("name", "pass", "mail");
        UserData userData2 = new UserData("name", "pass", "mail");

        dao.createUser(userData);
        Assertions.assertThrows(DataAccessException.class, () -> dao.createUser(userData2));
    }

    @Test
    @DisplayName("Positive Get User Test ")
    public void positiveGetUserTest() throws DataAccessException {
        MySQLUserDAO dao = new MySQLUserDAO();

        dao.clear();

        UserData userData = new UserData("name", "pass", "mail");
        UserData userData2 = new UserData("name2", "pass2", "mail2");

        dao.createUser(userData);
        dao.createUser(userData2);

        UserData result = dao.getUser("name");
        UserData result2 = dao.getUser("name2");

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result2);
        Assertions.assertEquals("name", result.username());
        Assertions.assertEquals("name2", result2.username());
    }

    @Test
    @DisplayName("Negative Get User Test ")
    public void negativeGetUserTest() throws DataAccessException {
        MySQLUserDAO dao = new MySQLUserDAO();

        dao.clear();

        UserData userData = new UserData("name", "pass", "mail");

        dao.createUser(userData);

        UserData result = dao.getUser("Doesn't Exist");

        Assertions.assertNull(result);
    }

}