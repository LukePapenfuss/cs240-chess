package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;
    private final String board;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move, String board) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
        this.board = board;
    }

    public ChessMove getMove() {
        return move;
    }

    public String getBoard() { return board; }
}