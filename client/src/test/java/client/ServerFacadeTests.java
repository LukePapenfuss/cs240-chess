package client;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import request.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void registerPositive() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");

        var authData = facade.register(request);

        Assertions.assertEquals("player1", authData.username());
        Assertions.assertTrue(authData.authToken().length() > 10);
    }

    @Test
    public void registerNegative() throws ResponseException {
        RegisterRequest request = new RegisterRequest("player1", null, "p1@email.com");

        Assertions.assertThrows(ResponseException.class, () -> facade.register(request));
    }

    @Test
    public void logoutPositive() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        Assertions.assertDoesNotThrow(() -> facade.logout(new LogoutRequest(authData.authToken())));
    }

    @Test
    public void logoutNegative() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        facade.logout(new LogoutRequest(authData.authToken()));

        Assertions.assertThrows(ResponseException.class, () -> facade.logout(new LogoutRequest("Not the right authToken")));
    }

    @Test
    public void loginPositive() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        facade.logout(new LogoutRequest(authData.authToken()));

        LoginResult loginData = facade.login(new LoginRequest("player1", "password"));

        Assertions.assertEquals("player1", loginData.username());
        Assertions.assertTrue(loginData.authToken().length() > 10);
    }

    @Test
    public void loginNegative() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        facade.logout(new LogoutRequest(authData.authToken()));

        Assertions.assertThrows(ResponseException.class, () -> facade.login(new LoginRequest("player2", "password")));
    }

    @Test
    public void createPositive() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult result = facade.create(authData.authToken(), new CreateRequest("testGame"));

        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    public void createNegative() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        Assertions.assertThrows(ResponseException.class, () -> facade.create("Incorrect authToken", new CreateRequest("testGame")));
    }

    @Test
    public void listPositive() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult game1 = facade.create(authData.authToken(), new CreateRequest("testGame1"));
        CreateResult game2 = facade.create(authData.authToken(), new CreateRequest("testGame2"));

        ListResult listResult = facade.list(authData.authToken(), new ListRequest(authData.authToken()));

        Assertions.assertFalse(listResult.games().isEmpty());
    }

    @Test
    public void listNegative() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult game1 = facade.create(authData.authToken(), new CreateRequest("testGame1"));
        CreateResult game2 = facade.create(authData.authToken(), new CreateRequest("testGame2"));

        Assertions.assertThrows(ResponseException.class, () -> facade.list("Incorrect authToken", new ListRequest(authData.authToken())));
    }

    @Test
    public void joinPositive() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult game1 = facade.create(authData.authToken(), new CreateRequest("testGame1"));

        facade.join(authData.authToken(), new JoinRequest(ChessGame.TeamColor.WHITE, game1.gameID()));

        ListResult listResult = facade.list(authData.authToken(), new ListRequest(authData.authToken()));

        Assertions.assertEquals("player1", listResult.games().getLast().whiteUsername());
    }

    @Test
    public void joinNegative() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult game1 = facade.create(authData.authToken(), new CreateRequest("testGame1"));
        CreateResult game2 = facade.create(authData.authToken(), new CreateRequest("testGame2"));

        Assertions.assertThrows(ResponseException.class, () -> facade.join(authData.authToken(), new JoinRequest(ChessGame.TeamColor.WHITE, 0)));
        Assertions.assertThrows(ResponseException.class, () -> facade.join("Bad token", new JoinRequest(ChessGame.TeamColor.WHITE, game1.gameID())));
    }

    @Test
    public void clearPositive() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult game1 = facade.create(authData.authToken(), new CreateRequest("testGame1"));
        CreateResult game2 = facade.create(authData.authToken(), new CreateRequest("testGame2"));

        facade.clear();

        var authData2 = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        ListResult listResult = facade.list(authData2.authToken(), new ListRequest(authData2.authToken()));

        Assertions.assertTrue(listResult.games().isEmpty());
    }

    @Test
    public void clearNegative() throws ResponseException {
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));

        CreateResult game1 = facade.create(authData.authToken(), new CreateRequest("testGame1"));
        CreateResult game2 = facade.create(authData.authToken(), new CreateRequest("testGame2"));

        facade.clear();

        Assertions.assertThrows(ResponseException.class, () -> facade.login(new LoginRequest("player1", "password")));

    }

}
