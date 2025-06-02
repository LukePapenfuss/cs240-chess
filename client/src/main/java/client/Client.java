package client;

import java.util.Arrays;

import chess.ChessGame;

import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import request.*;

public class Client {

    private String visitorAuth = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;
    private int currentGameIndex = 0;

    public Client(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            if (state == State.LOGGEDOUT) {
                return switch (cmd) {
                    case "register" -> register(params);
                    case "login" -> login(params);
                    case "quit" -> quit();
                    default -> help();
                };
            } else if (state == State.LOGGEDIN) {
                return switch (cmd) {
                    case "create" -> create(params);
                    case "list" -> list();
                    case "join" -> join(params);
                    case "observe" -> observe(params);
                    case "logout" -> logout();
                    case "quit" -> quit();
                    default -> help();
                };
            } else {
                // In game commands (Playing and Observing)
                return switch (cmd) {
                    case "exit" -> exit();
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

                state = State.LOGGEDIN;

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

                state = State.LOGGEDIN;

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

                return String.format("Chess match named %s was created", gameName);

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

            StringBuilder str = new StringBuilder();

            for (int i = 0; i < result.games().size(); ++i) {
                GameData game = result.games().get(i);

                str.append((i + 1)).append(". Game name: ").append(game.gameName()).append("\tWhite: ")
                        .append(game.whiteUsername()).append("\tBlack: ").append(game.blackUsername()).append("\n");
            }

            return str.toString();
        } catch (ResponseException e) {
            throw new ResponseException("Could not list all games.");
        }

    }

    public String join(String... params) throws ResponseException {
        try {
            Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException("Please enter an integer value.");
        }

        if (!params[1].equalsIgnoreCase("white") && !params[1].equalsIgnoreCase("black")) {
            throw new ResponseException("Please choose team white or team black.");
        }

        ChessGame.TeamColor color = params[1].equalsIgnoreCase("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        int gameInt = Integer.parseInt(params[0]);

        ListRequest listRequest = new ListRequest(visitorAuth);

        try {
            ListResult listResult = server.list(visitorAuth, listRequest);

            if (gameInt > listResult.games().size()) {
                throw new ResponseException("");
            }

            int gameID = listResult.games().get(gameInt-1).gameID();

            JoinRequest request = new JoinRequest(color, gameID);

            JoinResult result = server.join(visitorAuth, request);

            state = State.INGAME;
            currentGameIndex = gameInt;

            return printGame(gameInt, color == ChessGame.TeamColor.WHITE);
        } catch (ResponseException e) {
            throw new ResponseException("Could not join game.");
        }
    }

    public String observe(String... params) throws ResponseException {
        try {
            Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException("Please enter an integer value.");
        }

        int gameInt = Integer.parseInt(params[0]);

        try {
            return printGame(gameInt, true);
        } catch (ResponseException e) {
            throw new ResponseException("Could not observe game.");
        }
    }

    public String logout() throws ResponseException {
        try {
            LogoutRequest request = new LogoutRequest(visitorAuth);

            server.logout(request);

            visitorAuth = null;

            state = State.LOGGEDOUT;

            return "Logged out successfully.";
        } catch (ResponseException e) {
            throw new ResponseException("Could not log out.");
        }

    }

    // IN GAME COMMANDS

    public String exit() throws ResponseException {
        state = State.LOGGEDIN;

        return "Exited game.";
    }

    // SHARED COMMANDS

    public String quit() throws ResponseException { return "Quitting..."; }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    - help
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    """;
        } else if (state == State.LOGGEDIN) {
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
        return """
                    - help
                    - exit (the game)
                    - quit (the program)
                    """;
    }

    public String getState() { return state.toString(); }

    // OTHER METHODS

    private String printGame(int gameIndex, boolean playAsWhite) throws ResponseException {
        ListRequest listRequest = new ListRequest(visitorAuth);

        try {
            ListResult listResult = server.list(visitorAuth, listRequest);

            if (gameIndex > listResult.games().size()) {
                throw new ResponseException("");
            }

            String defaultColor = "\u001b[39;49m";
            String whiteOnDark = "\u001b[39;47m";
            String whiteOnLight = "\u001b[39;100m";
            String blackOnDark = "\u001b[30;47m";
            String blackOnLight = "\u001b[30;100m";

            ChessGame game = listResult.games().get(gameIndex-1).game();

            String str = defaultColor + (playAsWhite ? " \u2003 \u2003a \u2003b \u2003c \u2003d \u2003e \u2003f \u2003g \u2003h  \n" :
                    " \u2003 \u2003h \u2003g \u2003f \u2003e \u2003d \u2003c \u2003b \u2003a  \n");

            for (int i = 0; i < 8; ++i) {
                str += defaultColor + "\u2003" + (playAsWhite ? 8 - i : i + 1) + " ";
                for (int j = 0; j < 8; ++j) {

                    ChessPiece piece = game.getBoard().getPiece(new ChessPosition(playAsWhite ? 8-i : i+1, playAsWhite ? j+1 : 8-j));

                    if (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        str += ((i + j) % 2 == 0) ? whiteOnDark : whiteOnLight;
                    } else {
                        str += ((i + j) % 2 == 0) ? blackOnDark : blackOnLight;
                    }

                    str += " ";
                    if(piece != null) {
                        str += piece.toSymbol();
                    } else { str += "\u2003"; }
                    str += " ";
                }
                str += defaultColor + "\u2003" + (playAsWhite ? 8 - i : i + 1) + " \n";
            }

            str += defaultColor + (playAsWhite ? " \u2003 \u2003a \u2003b \u2003c \u2003d \u2003e \u2003f \u2003g \u2003h  \n" :
                    " \u2003 \u2003h \u2003g \u2003f \u2003e \u2003d \u2003c \u2003b \u2003a  \n");

            return str;
        } catch (ResponseException e) {
            throw new ResponseException("Could not display game.");
        }
    }

}