package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public ChessPosition(String notation) throws InvalidMoveException {
        char file = notation.toLowerCase().charAt(0);
        char rank = notation.charAt(1);

        if (!Character.isAlphabetic(file) || !Character.isDigit(rank)) {
            throw new InvalidMoveException("Incorrect move notation.");
        }

        this.row = Integer.parseInt(String.valueOf(rank));
        this.col = "abcdefgh".indexOf(file) + 1;
    }

    /**
     * Generates a hashCode for the chess position
     *
     * @return A hashCode for the chess position
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Determines whether two chess positions are identical
     *
     * @param obj The chess position to compare with
     * @return True if they are identical chess positions
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        ChessPosition that = (ChessPosition) obj;
        return row == that.getRow() && col == that.getColumn();
    }

    /**
     * Outputs a string showing the position in chess notation
     *
     * @return String of the move in chess notation (ex. g6)
     */
    @Override
    public String toString() {
        String alpha = "abcdefgh";

        return alpha.charAt(col-1) + Integer.toString(row);
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    /**
     * Determines whether the position is located on the board
     *
     * @return True if the position is on the board
     */
    public boolean insideBoard() {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

}
