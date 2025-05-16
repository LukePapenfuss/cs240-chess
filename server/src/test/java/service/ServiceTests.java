package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.*;
import service.request.*;

import javax.xml.crypto.Data;

public class ServiceTests {

    @Test
    @DisplayName("Positive Register Test")
    public void PositiveRegisterTest() throws DataAccessException {
        UserService userService = new UserService();

        RegisterRequest request = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult result = userService.register(request);

        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals(result.username(), request.username());
    }

    @Test
    @DisplayName("Negative Register Test")
    public void NegativeRegisterTest() {
        UserService userService = new UserService();

        RegisterRequest request = new RegisterRequest("Heinz", null, "inator@evil.com");

        Assertions.assertThrows(DataAccessException.class, () -> userService.register(request));
    }

    @Test
    @DisplayName("Positive Login Test")
    public void PositiveLoginTest() throws DataAccessException {
        UserService userService = new UserService();

        RegisterRequest request = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult result = userService.register(request);

        LoginRequest loginRequest = new LoginRequest("Heinz", "Doofenschmirz");

        LoginResult loginResult = userService.login(loginRequest);

        Assertions.assertNotNull(loginResult.authToken());
        Assertions.assertEquals(loginResult.username(), result.username());
    }

    @Test
    @DisplayName("Negative Login Test")
    public void NegativeLoginTest() throws DataAccessException {
        UserService userService = new UserService();

        RegisterRequest request = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult result = userService.register(request);

        LoginRequest loginRequest = new LoginRequest("Heinz", "Wrong Password");

        Assertions.assertThrows(DataAccessException.class, () -> userService.login(loginRequest));
    }

    @Test
    @DisplayName("Positive Logout Test")
    public void PositiveLogoutTest() throws DataAccessException {
        UserService userService = new UserService();

        RegisterRequest request = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult result = userService.register(request);

        LogoutRequest logoutRequest = new LogoutRequest(result.authToken());

        userService.logout(logoutRequest);

        Assertions.assertNull(userService.getUsername(result.authToken()));
    }

    @Test
    @DisplayName("Negative Logout Test")
    public void NegativeLogoutTest() throws DataAccessException {
        UserService userService = new UserService();

        RegisterRequest request = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult result = userService.register(request);

        LogoutRequest logoutRequest = new LogoutRequest(null);

        Assertions.assertThrows(DataAccessException.class, () -> userService.logout(logoutRequest));
    }

    @Test
    @DisplayName("Positive Create Test")
    public void PositiveCreateTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Thwart Me.");

        CreateResult createResult = gameService.create(createRequest);

        Assertions.assertNotNull(createResult);
        Assertions.assertTrue(createResult.gameID() > 0);
    }

    @Test
    @DisplayName("Negative Create Test")
    public void NegativeCreateTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest(null);

        Assertions.assertThrows(DataAccessException.class, () -> gameService.create(createRequest));
    }

    @Test
    @DisplayName("Positive List Test")
    public void PositiveListTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Thwart Me.");

        CreateResult createResult = gameService.create(createRequest);

        ListRequest listRequest = new ListRequest(registerResult.authToken());

        ListResult listResult = gameService.list(listRequest);

        Assertions.assertNotNull(listResult.games());
        Assertions.assertFalse(listResult.games().isEmpty());
        Assertions.assertEquals(listResult.games().getFirst().gameID(), createResult.gameID());
    }

    @Test
    @DisplayName("Negative List Test")
    public void NegativeListTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Thwart Me.");

        CreateResult createResult = gameService.create(createRequest);

        ListRequest listRequest = new ListRequest(null);

        Assertions.assertThrows(DataAccessException.class, () -> gameService.list(listRequest));
    }

    @Test
    @DisplayName("Positive Join Test")
    public void PositiveJoinTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Thwart Me.");

        CreateResult createResult = gameService.create(createRequest);

        JoinRequest joinRequest = new JoinRequest(ChessGame.TeamColor.WHITE, createResult.gameID());

        JoinResult joinResult = gameService.join("Heinz", joinRequest);

        Assertions.assertNotNull(joinResult);
    }

    @Test
    @DisplayName("Negative Join Test")
    public void NegativeJoinTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Thwart Me.");

        CreateResult createResult = gameService.create(createRequest);

        JoinRequest joinRequest = new JoinRequest(ChessGame.TeamColor.WHITE, -42);

        Assertions.assertThrows(DataAccessException.class, () -> gameService.join("Heinz", joinRequest));
    }

    @Test
    @DisplayName("Clear Test")
    public void ClearTest() throws DataAccessException {
        UserService userService = new UserService();
        GameService gameService = new GameService();

        RegisterRequest registerRequest = new RegisterRequest("Heinz", "Doofenschmirz", "inator@evil.com");

        RegisterResult registerResult = userService.register(registerRequest);

        CreateRequest createRequest = new CreateRequest("Thwart Me.");

        CreateResult createResult = gameService.create(createRequest);

        userService.clear();
        gameService.clear();

        ListRequest listRequest = new ListRequest(registerResult.authToken());

        ListResult listResult = gameService.list(listRequest);

        Assertions.assertTrue(listResult.games().isEmpty());
        Assertions.assertNull(userService.getUsername(registerResult.authToken()));
    }

}
