package websocket.messages;

public class LoadGameMessage extends ServerMessage {

    String game;

    public LoadGameMessage(String game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public String getGame() {
        return game;
    }
}
