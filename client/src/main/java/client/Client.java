package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import chess.*;

import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import model.GameData;
import org.glassfish.grizzly.http.server.Response;
import request.*;

public class Client {

    private String visitorAuth = null;
    private String visitorUsername = null;
    private final ServerFacade server;
    private WebSocketFacade ws;
    private final NotificationHandler notificationHandler;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;
    private int currentGameIndex = 0;
    private int currentGameID = 0;
    private ChessGame.TeamColor teamColor = ChessGame.TeamColor.WHITE;

    public Client(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.notificationHandler = notificationHandler;
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
            } else if (state == State.INGAME) {
                return switch (cmd) {
                    case "redraw" -> redraw();
                    case "leave" -> exit();
                    case "resign" -> resign();
                    case "move" -> move(params);
                    case "highlight" -> highlight(params);
                    case "quit" -> quit();
                    default -> help();
                };
            } else if (state == State.OBSERVING) {
                return switch (cmd) {
                    case "redraw" -> redraw();
                    case "leave" -> exit();
                    case "highlight" -> highlight(params);
                    case "quit" -> quit();
                    default -> help();
                };
            } else {
                return switch (cmd) {
                    case "yes" -> confirmResignation();
                    case "no" -> unconfirmResignation();
                    default -> "Please enter yes or no.";
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
                visitorUsername = result.username();

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
                visitorUsername = result.username();
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
        if (params.length != 2) {
            throw new ResponseException("Expected: join <id> [WHITE|BLACK]");
        }

        try {
            Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new ResponseException("Please enter an integer value.");
        }

        if (!params[1].equalsIgnoreCase("white") && !params[1].equalsIgnoreCase("black")) {
            throw new ResponseException("Please choose team white or team black.");
        }

        if (Integer.parseInt(params[0]) <= 0) {
            throw new ResponseException("Please enter an integer value greater than 0");
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
            currentGameID = gameID;
            teamColor = color;

            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.connect(visitorAuth, currentGameIndex > listResult.games().size() ? 0 : currentGameIndex, teamColor);

            return "";
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

        if (Integer.parseInt(params[0]) <= 0) {
            throw new ResponseException("Please enter an integer value greater than 0");
        }

        int gameInt = Integer.parseInt(params[0]);
        ListRequest listRequest = new ListRequest(visitorAuth);

        try {
            ListResult listResult = server.list(visitorAuth, listRequest);

            if (gameInt > listResult.games().size()) {
                throw new ResponseException("");
            }

            int gameID = listResult.games().get(gameInt-1).gameID();

            state = State.OBSERVING;
            currentGameIndex = gameInt;
            currentGameID = gameID;
            teamColor = ChessGame.TeamColor.WHITE;

            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.connect(visitorAuth, currentGameIndex > listResult.games().size() ? 0 : currentGameIndex, null);

            return "";
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
    public String redraw() throws ResponseException {
        try {
            return printGame(currentGameIndex, teamColor == ChessGame.TeamColor.WHITE, null, null);
        } catch (ResponseException e) {
            throw new ResponseException("Could not redraw the game.");
        }
    }
    public String resign() throws ResponseException {
        state = State.CONFIRMATION;

        return "Are you sure you want to resign?";
    }
    public String move(String... params) throws ResponseException {
        if (params.length == 2 || params.length == 3) {
            String start = params[0];
            String end = params[1];
            String promotion = params.length == 3 ? params[2] : null;
            ListRequest listRequest = new ListRequest(visitorAuth);
            ListResult listResult = server.list(visitorAuth, listRequest);
            if (currentGameIndex > listResult.games().size()) {
                throw new ResponseException("Couldn't find the game.");
            }
            GameData gameData = listResult.games().get(currentGameIndex-1);
            if (gameData.game().isFinished()) {
                throw new ResponseException("The game is already over.");
            }
            ChessPosition startPos;
            ChessPosition endPos;
            try {
                startPos = new ChessPosition(start);
                endPos = new ChessPosition(end);
            } catch (InvalidMoveException e) {
                throw new ResponseException("Invalid move notation.");
            }
            ChessMove move = new ChessMove(startPos, endPos, promotion == null ? null : convertToPieceType(promotion));
            try {
                if (gameData.game().getTeamTurn() != teamColor) {
                    throw new InvalidMoveException("It isn't your turn.");
                }
                gameData.game().makeMove(move);
                ws.makeMove(visitorAuth, currentGameIndex, move, printGame(currentGameIndex, teamColor != ChessGame.TeamColor.WHITE, null, move));
            } catch (InvalidMoveException e) {
                throw new ResponseException(e.getMessage());
            }
            return ""; // Add highlighted move
        } else {
            throw new ResponseException("Expected: move <start> <end> <promotion>");
        }
    }
    public String highlight(String... params) throws ResponseException {
        if (params.length == 1) {
            try {
                return printGame(currentGameIndex, teamColor == ChessGame.TeamColor.WHITE, params[0], null);
            } catch (ResponseException e) {
                throw new ResponseException("Could not highlight the piece.");
            }
        } else {
            throw new ResponseException("Expected: highlight <tile>");
        }
    }
    public String exit() throws ResponseException {
        state = State.LOGGEDIN;
        ws.leave(visitorAuth, currentGameID);
        ws = null;
        currentGameID = 0;
        currentGameIndex = 0;
        return "Exited game.";
    }

    // SHARED COMMANDS
    public String quit() throws ResponseException { return "quit"; }
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
        } else if (state == State.OBSERVING) {
            return """
                    - help
                    - redraw
                    - highlight <tile>
                    - leave (the game)
                    - quit (the program)
                    """;
        }
        return """
                    - help
                    - redraw
                    - resign
                    - move <start> <end>
                    - highlight <tile>
                    - leave (the game)
                    - quit (the program)
                    """;
    }
    public String getState() { return state.toString(); }

    // CONFIRMATION COMMANDS
    public String confirmResignation() throws ResponseException {
        state = State.INGAME;
        ws.resign(visitorAuth, currentGameID);
        ListRequest listRequest = new ListRequest(visitorAuth);
        ListResult listResult = server.list(visitorAuth, listRequest);
        if (currentGameIndex > listResult.games().size()) {
            throw new ResponseException("Game not found.");
        }
        return "";
    }
    public String unconfirmResignation() throws ResponseException {
        state = State.INGAME;
        return "Returning to the game";
    }

    // OTHER METHODS
    public String printGame(int gameIndex, boolean playAsWhite, String highlightedTile, ChessMove move) throws ResponseException {
        ListRequest listRequest = new ListRequest(visitorAuth);
        playAsWhite = teamColor == ChessGame.TeamColor.WHITE;
        try {
            ListResult listResult = server.list(visitorAuth, listRequest);
            if (gameIndex > listResult.games().size()) {
                throw new ResponseException("Game: " + gameIndex + " not found in " + listResult.games().size() + " games.");
            }
            String defaultColor = "\u001b[39;49m";
            String whiteOnDark = "\u001b[39;47m";
            String whiteOnLight = "\u001b[39;100m";
            String blackOnDark = "\u001b[30;47m";
            String blackOnLight = "\u001b[30;100m";
            String whiteOnValid = "\u001b[39;43m";
            String blackOnValid = "\u001b[30;43m";
            ChessGame game = listResult.games().get(gameIndex-1).game();
            ArrayList<ChessMove> validMoves = new ArrayList<>();
            try {
                if (highlightedTile != null) {
                    validMoves = (ArrayList<ChessMove>) game.validMoves(new ChessPosition(highlightedTile));
                }
            } catch (InvalidMoveException e) {
                throw new ResponseException(e.getMessage());
            }
            if (highlightedTile != null && (validMoves == null || validMoves.isEmpty())) {
                throw new ResponseException("The piece on " + highlightedTile + " has no legal move.");
            }
            String str = defaultColor + (playAsWhite ? " \u2003 \u2003a \u2003b \u2003c \u2003d \u2003e \u2003f \u2003g \u2003h  \n" :
                    " \u2003 \u2003h \u2003g \u2003f \u2003e \u2003d \u2003c \u2003b \u2003a  \n");
            for (int i = 0; i < 8; ++i) {
                str += defaultColor + "\u2003" + (playAsWhite ? 8 - i : i + 1) + " ";
                for (int j = 0; j < 8; ++j) {
                    ChessPosition pos = new ChessPosition(playAsWhite ? 8-i : i+1, playAsWhite ? j+1 : 8-j);
                    ChessPiece piece = game.getBoard().getPiece(pos);
                    boolean isValid = false;
                    for (int k = 0; k < validMoves.size(); ++k) {
                        isValid = isValid || Objects.equals(validMoves.get(k).getEndPosition(), pos);
                    }
                    if ((highlightedTile != null && isValid) ||
                            (move != null && (move.getEndPosition().equals(pos) || move.getStartPosition().equals(pos)))) {
                        str += (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE ? whiteOnValid : blackOnValid);
                    } else {
                        str += ((i+j) % 2 == 0) ?
                                (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE ? whiteOnDark : blackOnDark) :
                                (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE ? whiteOnLight : blackOnLight);
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
            throw new ResponseException(e.getMessage());
        }
    }
    private ChessPiece.PieceType convertToPieceType(String type) {
        switch (type.toLowerCase()) {
            case "king" -> {
                return ChessPiece.PieceType.KING;
            }
            case "queen" -> {
                return ChessPiece.PieceType.QUEEN;
            }
            case "rook" -> {
                return ChessPiece.PieceType.ROOK;
            }
            case "bishop" -> {
                return ChessPiece.PieceType.BISHOP;
            }
            case "knight" -> {
                return ChessPiece.PieceType.KNIGHT;
            }
            default -> {
                return ChessPiece.PieceType.PAWN;
            }
        }
    }
}