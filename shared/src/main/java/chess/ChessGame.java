package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;
    private boolean finished;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();

        finished = false;

        teamTurn = TeamColor.WHITE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        ChessGame that = (ChessGame) obj;

        return board.equals(that.getBoard()) && teamTurn.equals(that.getTeamTurn());
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
            // Return null if no piece is at the start position
            if (board.getPiece(startPosition) == null) {
                return null;
            }

            // Find the team color
            ChessGame.TeamColor teamColor = board.getPiece(startPosition).getTeamColor();

            // Find all the basic moves that this piece can make
            ArrayList<ChessMove> moves = (ArrayList<ChessMove>) board.getPiece(startPosition).pieceMoves(board, startPosition);
            ArrayList<ChessMove> validMoves = new ArrayList<>();

            // Loop through the basic moves
            for (int i = 0; i < moves.size(); ++i) {
                // Create a new branch board
                ChessGame branch = new ChessGame();
                branch.setBoard(board);

                // Find the new piece if promoting
                ChessPiece newPiece = moves.get(i).getPromotionPiece() == null ?
                        branch.getBoard().getPiece(moves.get(i).getStartPosition()) :
                        new ChessPiece(teamColor, moves.get(i).getPromotionPiece());

                // If it is castling, move the rook too
                if (branch.isCastling(moves.get(i)) != null) {
                    boolean queenSide = branch.isCastling(moves.get(i)) == ChessPiece.PieceType.QUEEN;

                    // Make a new rook
                    ChessPiece newRook = new ChessPiece(board.getPiece(startPosition).getTeamColor(), ChessPiece.PieceType.ROOK);
                    newRook.flagAsMoved();

                    int row = moves.get(i).getEndPosition().getRow();
                    int col = moves.get(i).getEndPosition().getColumn();

                    // Place the rook
                    branch.getBoard().addPiece(new ChessPosition(row, col + (queenSide ? 1 : -1)), newRook);
                    branch.getBoard().removePiece(new ChessPosition(row, (queenSide ? 1 : 8)));
                }

                // If the move was en passant, capture the other pawn
                if (branch.isEnPassant(moves.get(i))) {
                    int row = moves.get(i).getStartPosition().getRow();
                    int col = moves.get(i).getEndPosition().getColumn();

                    branch.getBoard().removePiece(new ChessPosition(row, col));
                }

                // Move the piece hypothetically
                branch.getBoard().addPiece(moves.get(i).getEndPosition(), newPiece);

                // Make the previous space empty
                branch.getBoard().removePiece(moves.get(i).getStartPosition());

                // See if we are hypothetically in check
                boolean invalidMove = branch.isInCheck(teamColor);

                if (!invalidMove) {
                    validMoves.add(moves.get(i));
                }
            }

            return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // Throw invalid move if there is no piece
        if (board.getPiece(move.getStartPosition()) == null) { throw new InvalidMoveException("Invalid move: no piece" ); }

        // Find all valid moves for start position
        ArrayList<ChessMove> possibleMoves = (ArrayList<ChessMove>) validMoves(move.getStartPosition());

        boolean isValid = false;

        // Loop through possible moves to see if this move is valid
        for (int i = 0; i < possibleMoves.size(); ++i) {
            if (possibleMoves.get(i).equals(move)) {
                isValid = true;
                break;
            }
        }

        // Check to make sure the moving piece matches the team color
        if (board.getPiece(move.getStartPosition()).getTeamColor() != teamTurn) { isValid = false; }

        // If the move is not valid throw an exception
        if (!isValid) { throw new InvalidMoveException("Invalid move attempted: " + move); }

        // Get the piece to move
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());

        // If promoting, change moving piece to promoted piece
        if (move.getPromotionPiece() != null) { movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece()); }

        // If it is castling, move the rook too
        if (isCastling(move) != null) {
            // Find the direction
            boolean queenSide = isCastling(move) == ChessPiece.PieceType.QUEEN;

            // Make a new rook
            ChessPiece newRook = new ChessPiece(movingPiece.getTeamColor(), ChessPiece.PieceType.ROOK);
            newRook.flagAsMoved();

            // Place the rook
            board.addPiece(new ChessPosition(move.getEndPosition().getRow(), move.getEndPosition().getColumn() + (queenSide ? 1 : -1)), newRook);
            board.removePiece(new ChessPosition(move.getEndPosition().getRow(), (queenSide ? 1 : 8)));
        }

        // If the move was en passant, capture the other pawn
        if (isEnPassant(move)) { board.removePiece(new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn())); }

        // If the move is valid, make the move
        movingPiece.flagAsMoved();
        board.addPiece(move.getEndPosition(), movingPiece);
        board.removePiece(move.getStartPosition());

        // Set all en passant flags to false
        board.resetEnPassant();

        // If the pawn moved 2 spaces, flag it as en passantable. If not, un-flag it
        movingPiece.setEnPassantable(movingPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
                Math.abs(move.getEndPosition().getRow() - move.getStartPosition().getRow()) > 1);

        // Update team color
        teamTurn = teamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // Find the king
        ChessPosition kingPosition = board.findKing(teamColor);

        // Find all valid moves from the opponent
        return board.positionIsAttacked(kingPosition, (teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE));
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // Determine if we are in check
        boolean inCheck = isInCheck(teamColor);

        // If we aren't in check, we aren't in checkmate
        if (!inCheck) { return false; }

        // If we are in check, see if we have any valid moves
        ArrayList<ChessPosition> myPieces = board.findAllPieces(teamColor);
        for (int i = 0; i < myPieces.size(); ++i) {
            if (!validMoves(myPieces.get(i)).isEmpty()) { return false; }
        }

        // If no valid moves were found, we are in checkmate
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // Determine if we are in check
        boolean inCheck = isInCheck(teamColor);

        // If we are in check, we aren't in stalemate
        if (inCheck) { return false; }

        // If we are in check, see if we have any valid moves
        ArrayList<ChessPosition> myPieces = board.findAllPieces(teamColor);
        for (int i = 0; i < myPieces.size(); ++i) {
            if (!validMoves(myPieces.get(i)).isEmpty()) { return false; }
        }

        // If no valid moves were found, we are in stalemate
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param newBoard the new board to use
     */
    public void setBoard(ChessBoard newBoard) {
        board.setBoard(newBoard);
        teamTurn = TeamColor.WHITE;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * @return which side the king is castling on, null if it is not castling
     */
    public ChessPiece.PieceType isCastling(ChessMove move) {
        if (board.getPiece(move.getStartPosition()) == null) { return null; }

        if (board.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.KING) {
            if (Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) > 1) {
                return (move.getStartPosition().getColumn() > move.getEndPosition().getColumn() ?
                        ChessPiece.PieceType.QUEEN : ChessPiece.PieceType.KING);
            }
        }

        return null;
    }

    /**
     * @return whether the move is an en passant move
     */
    public boolean isEnPassant(ChessMove move) {
        if (board.getPiece(move.getStartPosition()) == null) { return false; }

        if (board.getPiece(move.getStartPosition()).getPieceType() == ChessPiece.PieceType.PAWN) {
            return move.getStartPosition().getRow() != move.getEndPosition().getRow() &&
                    move.getStartPosition().getColumn() != move.getEndPosition().getColumn() &&
                    board.getPiece(move.getEndPosition()) == null;
        }

        return false;
    }

    public void finishGame() {
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }
}
