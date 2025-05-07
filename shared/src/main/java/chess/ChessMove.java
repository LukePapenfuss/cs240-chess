package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition, ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * Generates a hashCode for the chess move
     *
     * @return A hashCode for the chess move
     */
    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    /**
     * Determines whether two moves are identical
     *
     * @param obj The move to compare with
     * @return True if they are identical moves
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        ChessMove that = (ChessMove) obj;

        // See if the positions are the same
        boolean samePositions = startPosition.equals(that.getStartPosition()) && endPosition.equals(that.getEndPosition());

        // If positions are the same, check the promotion piece too
        return samePositions && promotionPiece == that.getPromotionPiece();
    }

    /**
     * Outputs a string showing the move in chess notation. Does not show promotion
     *
     * @return String of the move in chess notation (ex. Nb4)
     */
    @Override
    public String toString() {
        return startPosition.toString() + " -> " + endPosition.toString();
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }
}
