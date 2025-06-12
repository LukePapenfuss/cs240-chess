package websocket.messages;

import chess.ChessMove;

public class LoadGameMessage extends ServerMessage {

    int game;
    ChessMove move;

    public LoadGameMessage(int game, ChessMove move) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
        this.move = move;
    }

    public int getGame() {
        return game;
    }

    public ChessMove getMove() { return move; }
}
