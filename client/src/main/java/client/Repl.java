package client;

import client.websocket.NotificationHandler;
import websocket.messages.*;

import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final Client client;

    public Repl(String serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.println("\uD83D\uDC36 Welcome to the Chess. Register or Login to start.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit") && !result.equals("q")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n[" + client.getState() + "] >>> ");
    }

    @Override
    public void notify(ServerMessage notification) {
        switch (notification.getServerMessageType()) {
            case NOTIFICATION:
                NotificationMessage note = (NotificationMessage) notification;
                System.out.println(note.getMessage());
                break;
            case LOAD_GAME:
                LoadGameMessage game = (LoadGameMessage) notification;
                System.out.println(game.getGame());
                break;
            case ERROR:
                ErrorMessage error = (ErrorMessage) notification;
                System.out.println(error.getMessage());
                break;
        }
        printPrompt();
    }
}