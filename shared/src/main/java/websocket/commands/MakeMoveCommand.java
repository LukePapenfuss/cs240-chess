package websocket.commands;

import chess.*;
import model.GameData;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;
    private final String board;

    public MakeMoveCommand(String authToken, int gameID, ChessMove move, String board) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.board = board;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getBoard() { return board; }
}