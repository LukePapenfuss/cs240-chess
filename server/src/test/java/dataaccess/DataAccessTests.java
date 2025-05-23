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
    @DisplayName("auth tests")
    public void authTest() throws DataAccessException {
        MySQLAuthDAO dao = new MySQLAuthDAO();

        AuthData authData = new AuthData("token", "name");
        AuthData authData2 = new AuthData("token2", "name2");

        dao.clear();

        dao.createAuth(authData);
        dao.createAuth(authData2);

        dao.deleteAuth(authData);

        AuthData result = dao.getAuth("token");
        AuthData result2 = dao.getAuth("token2");

        Assertions.assertNull(result);
        Assertions.assertNotNull(result2);
//        Assertions.assertEquals("name", result.username());
        Assertions.assertEquals("name2", result2.username());
    }

}