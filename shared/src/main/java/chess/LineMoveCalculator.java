package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class LineMoveCalculator {
    private final int[][] directions;

    public LineMoveCalculator(int[][] directions) {
        this.directions = directions;
    }

    ArrayList<ChessMove> getPositions(ChessPosition myPosition, ChessBoard board) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        for (int i = 0; i < directions.length; ++i) {
            ChessPosition newPosition = new ChessPosition(myPosition.getRow(), myPosition.getColumn());
            boolean capturing = false;
            do {
                // Get Position NE one square
                newPosition = new ChessPosition(newPosition.getRow() + directions[i][0], newPosition.getColumn() + directions[i][1]);

                // If the space is outside the chess board, end the diagonal
                if (!newPosition.insideBoard()) { break; }

                // If the space is occupied by a piece of the same color, end the diagonal
                if (board.getPiece(newPosition) != null) {
                    if (board.getPiece(myPosition).getTeamColor() == board.getPiece(newPosition).getTeamColor()) {
                        break;
                    } else {
                        capturing = true;
                    }
                }

                // Add this new position to the possible moves
                possibleMoves.add(new ChessMove(myPosition, newPosition, null));
            } while ( !capturing );
        }

        return possibleMoves;
    }


}
