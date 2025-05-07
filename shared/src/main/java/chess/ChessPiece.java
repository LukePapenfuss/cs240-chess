package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;
    private boolean moved;
    private boolean enPassantable;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
        moved = false;
        enPassantable = false;
    }

    /**
     * Generates a hashCode for the piece
     *
     * @return A hashCode for the piece
     */
    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     * Determines whether two pieces are identical
     *
     * @param obj The piece to compare with
     * @return True if they are identical pieces
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        ChessPiece that = (ChessPiece) obj;
        return pieceColor.equals(that.pieceColor) && type.equals(that.type);
    }

    /**
     * @return character of the piece in chess notation
     */
    @Override
    public String toString() {
        return pieceTypeNotation(type);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * @return which type of chess piece this piece is in chess notation
     */
    public String pieceTypeNotation(PieceType thisType) {
        String pieceStr = " ";

        switch (getPieceType()) {
            case KING -> pieceStr = "K";
            case QUEEN -> pieceStr = "Q";
            case ROOK -> pieceStr = "R";
            case BISHOP -> pieceStr = "B";
            case KNIGHT -> pieceStr = "N";
            case PAWN -> pieceStr = "P";
        }

        if (getTeamColor() == ChessGame.TeamColor.BLACK) { pieceStr = pieceStr.toLowerCase(); }

        return pieceStr;
    }

    /**
     * Mark the piece as having been moved from start
     */
    public void flagAsMoved() {
        moved = true;
    }

    /**
     * @return whether the piece has been moved since the beginning
     */
    public boolean ifMoved() {
        return moved;
    }

    /**
     * Sets the state of the enParrantable variable
     *
     * @param ep whether the piece is en passantable
     */
    public void setEnPassantable(boolean ep) {
        enPassantable = ep;
    }

    /**
     * @return whether this pawn may be valid for en passant
     */
    public boolean isEnPassantable() {
        return enPassantable;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        switch (type) {
            case KING -> possibleMoves = new KingMovesCalculator().pieceMoves(board, myPosition);
            case QUEEN -> possibleMoves = new QueenMovesCalculator().pieceMoves(board, myPosition);
            case BISHOP -> possibleMoves = new BishopMovesCalculator().pieceMoves(board, myPosition);
            case KNIGHT -> possibleMoves = new KnightMovesCalculator().pieceMoves(board, myPosition);
            case ROOK -> possibleMoves = new RookMovesCalculator().pieceMoves(board, myPosition);
            case PAWN -> possibleMoves = new PawnMovesCalculator().pieceMoves(board, myPosition);
        }

        return possibleMoves;
    }
}