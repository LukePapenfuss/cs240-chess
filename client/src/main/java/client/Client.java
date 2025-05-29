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

    public String register(String... params) throws ResponseException { return "This is the register."; }

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
                throw new RuntimeException("Invalid Credentials");
            }

        } else {
            throw new ResponseException("Expected: login <username> <password>");
        }
    }

    public String quit() throws ResponseException { return "This is the quit."; }

    public String help() { return "This is the help."; }

}