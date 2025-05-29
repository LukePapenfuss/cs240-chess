package client;

import java.util.Arrays;

import chess.ChessGame;
import com.google.gson.Gson;

import model.GameData;
import server.ServerFacade;
import service.request.*;

public class Client {

    private String visitorAuth = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.SIGNEDOUT;

    public Client(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.SIGNEDOUT) {
                return switch (cmd) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "quit" -> quit();
                    default -> help();
                };
            } else {
                return switch (cmd) {
                    case "create" -> create(params);
                    case "list" -> list();
                    case "join" -> join(params);
                    case "observe" -> observe(params);
                    case "logout" -> logout();
                    case "quit" -> quit();
                    default -> help();
                };
            }
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    // LOGGED OUT COMMANDS

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];

            RegisterRequest request = new RegisterRequest(username, password, email);

            try {
                RegisterResult result = server.register(request);

                state = State.SIGNEDIN;

                visitorAuth = result.authToken();

                return String.format("You are now registered and logged in as %s.", result.username());

            } catch (ResponseException e) {
                throw new ResponseException("Invalid Credentials");
            }

        } else {
            throw new ResponseException("Expected: register <username> <password> <email>");
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];

            LoginRequest loginRequest = new LoginRequest(username, password);

            try {
                LoginResult result = server.login(loginRequest);

                state = State.SIGNEDIN;

                visitorAuth = result.authToken();

                return String.format("You are logged in as %s.", result.username());

            } catch (ResponseException e) {
                throw new ResponseException("Invalid Credentials");
            }

        } else {
            throw new ResponseException("Expected: login <username> <password>");
        }
    }

    // LOGGED IN COMMANDS

    public String create(String... params) throws ResponseException {
        if (params.length == 1) {
            String gameName = params[0];

            CreateRequest request = new CreateRequest(gameName);

            try {
                CreateResult result = server.create(visitorAuth, request);

                return String.format("Chess match with ID [%s] was created.", result.gameID());

            } catch (ResponseException e) {
                throw new ResponseException("Could not create the game.");
            }

        } else {
            throw new ResponseException("Expected: create <name>");
        }
    }

    public String list() throws ResponseException {
        ListRequest request = new ListRequest(visitorAuth);

        try {
            ListResult result = server.list(visitorAuth, request);

            if (result.games().isEmpty()) {
                return "No games found.";
            }

            String str = "";

            for (int i = 0; i < result.games().size(); ++i) {
                GameInfo game = result.games().get(i);

                str += (i+1) + ". Game name: " + game.gameName() + "\tWhite: " + game.whiteUsername() + "\tBlack: " + game.blackUsername() + "\n";
            }

            return str;
        } catch (ResponseException e) {
            throw new ResponseException("Could not list all games.");
        }

    }

    public String join(String... params) throws ResponseException {
        ChessGame.TeamColor color = params[1].equalsIgnoreCase("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        int gameInt = Integer.parseInt(params[0]);

        ListRequest listRequest = new ListRequest(visitorAuth);

        try {
            ListResult listResult = server.list(visitorAuth, listRequest);

            int gameID = listResult.games().get(gameInt-1).gameID();

            JoinRequest request = new JoinRequest(color, gameID);

            JoinResult result = server.join(visitorAuth, request);

            return "Succesfully joined game as " + color.toString() + ".";
        } catch (ResponseException e) {
            throw new ResponseException("Could not join game.");
        }
    }

    public String observe(String... params) throws ResponseException {
        return "observe";
    }

    public String logout() throws ResponseException {
        return "logout";
    }

    // SHARED COMMANDS

    public String quit() throws ResponseException { return "quit"; }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - help
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    """;
        }
        return """
                - help
                - logout
                - create <name>
                - list
                - join <id> [WHITE|BLACK]
                - observe <id>
                - quit
                """;
    }

    public String getState() { return state.toString(); }

}