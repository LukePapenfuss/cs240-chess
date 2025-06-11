package websocket.commands;

import chess.*;
import model.GameData;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;
    private final String board;
    private final GameData game;

    public MakeMoveCommand(String authToken, int gameID, ChessMove move, String board, GameData game) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.board = board;
        this.game = game;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getBoard() { return board; }

    public GameData getGame() { return game; }
}