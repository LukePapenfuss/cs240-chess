package client;

import java.util.Arrays;

import com.google.gson.Gson;

import server.ServerFacade;
import service.request.*;

public class Client {

    private String visitorName = null;
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
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> quit();
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];

            RegisterRequest request = new RegisterRequest(username, password, email);

            try {
                RegisterResult result = server.register(request);

                state = State.SIGNEDIN;

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

                return String.format("You are signed in as %s.", result.username());

            } catch (ResponseException e) {
                throw new ResponseException("Invalid Credentials");
            }

        } else {
            throw new ResponseException("Expected: login <username> <password>");
        }
    }

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