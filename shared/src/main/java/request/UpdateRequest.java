package request;

import chess.ChessGame;

public record UpdateRequest(ChessGame game, int gameID, String whiteUsername, String blackUsername) { }
