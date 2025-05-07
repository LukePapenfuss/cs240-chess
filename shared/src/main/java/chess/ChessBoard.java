package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Generates a hashCode for the chess board
     *
     * @return A hashCode for the chess board
     */
    @Override
    public int hashCode() {
        int code = 0;

        // Loop through every square on the board
        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                // If the square contains a piece, add to the hash code based on the code of the piece
                // and the coordinate. If it is null, add a hash code based just on the coordinate.
                code += (squares[i][j] != null ? squares[i][j].hashCode() : 71 ) * (i + 1) * (j + 1);
            }
        }

        return code;
    }

    /**
     * Determines whether two boards are identical
     *
     * @param obj The board to compare with
     * @return True if they are identical boards
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null || getClass() != obj.getClass()) { return false; }
        ChessBoard that = (ChessBoard) obj;

        // Check if all pieces are the same
        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                // Get piece we want to compare with
                ChessPiece thatPiece = that.getPiece(new ChessPosition(i+1,j+1));

                // If one piece is null and the other isn't, return false;
                if ((squares[i][j] == null && thatPiece != null) || (squares[i][j] != null && thatPiece == null)) {
                    return false;
                }

                // If both pieces are null, no need to compare pieces
                if (squares[i][j] == null && thatPiece == null) {
                    continue;
                }

                // If both pieces are not null, return false if they are not the same piece
                if (!squares[i][j].equals(thatPiece)) {
                    return false;
                }
            }
        }

        // Returns true when no differences were detected
        return true;
    }

    /**
     * Outputs a string showing the board
     *
     * @return String representation of the board
     */
    @Override
    public String toString() {
        String str = "";

        for (int i = squares.length-1; i >= 0; --i) {
            str += "|";
            for (int j = 0; j < squares.length; ++j) {
                // Add empty space if null
                if (squares[i][j] == null) {
                    str += " |";
                    continue;
                }

                // Find string representation of piece
                String pieceStr = "";
                switch (squares[i][j].getPieceType()) {
                    case KING -> pieceStr = "K";
                    case QUEEN -> pieceStr = "Q";
                    case ROOK -> pieceStr = "R";
                    case BISHOP -> pieceStr = "B";
                    case KNIGHT -> pieceStr = "N";
                    case PAWN -> pieceStr = "P";
                }

                // Convert to lowercase if it is a black piece
                if (squares[i][j].getTeamColor() == ChessGame.TeamColor.BLACK) { pieceStr = pieceStr.toLowerCase(); }

                // Add piece to string
                str += pieceStr + "|";
            }
            str += "\n";
        }

        return str;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Removes a chess piece from the chessboard
     *
     * @param position where to remove the piece from
     */
    public void removePiece(ChessPosition position) {
        squares[position.getRow()-1][position.getColumn()-1] = null;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to be a copy of the new board
     *
     * @param newBoard the board to be copied
     */
    public void setBoard(ChessBoard newBoard) {
        squares = new ChessPiece[8][8];

        // Copy all of the piece from a new board
        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                ChessPosition pos = new ChessPosition(i + 1, j + 1);

                if(newBoard.getPiece(pos) == null) { continue; }

                ChessPiece newPiece = new ChessPiece(newBoard.getPiece(pos).getTeamColor(), newBoard.getPiece(pos).getPieceType());

                addPiece(pos, newPiece);
            }
        }

    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // Add all White Pawns
        for (int i = 0; i < squares[1].length; ++i) {
            // Create a Pawn
            ChessPiece newPawn = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

            // Add the Pawn to the board
            addPiece(new ChessPosition(2, i + 1), newPawn);
        }

        // Add all Black Pawns
        for (int i = 0; i < squares[1].length; ++i) {
            // Create a Pawn
            ChessPiece newPawn = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);

            // Add the Pawn to the board
            addPiece(new ChessPosition(7, i + 1), newPawn);
        }

        ChessPiece.PieceType[] backRank = {ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK};

        // Set up White's back rank
        for (int i = 0; i < backRank.length; ++i) {
            ChessPiece newPiece = new ChessPiece(ChessGame.TeamColor.WHITE, backRank[i]);
            addPiece(new ChessPosition(1, i + 1), newPiece);
        }

        // Set up Black's back rank
        for (int i = 0; i < backRank.length; ++i) {
            ChessPiece newPiece = new ChessPiece(ChessGame.TeamColor.BLACK, backRank[i]);
            addPiece(new ChessPosition(8, i + 1), newPiece);
        }
    }

    /**
     * Finds the king of a given team color on the board
     *
     * @param teamColor the color of the king to find
     * @return the position of the found king (null if it is not found)
     */
    public ChessPosition findKing(ChessGame.TeamColor teamColor) {
        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                ChessPiece piece = getPiece(new ChessPosition(i+1,j+1));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(i + 1,j + 1);
                }
            }
        }
        return null;
    }

    public ArrayList<ChessPosition> findAllPieces(ChessGame.TeamColor teamColor) {
        ArrayList<ChessPosition> positions = new ArrayList<>();

        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                ChessPiece piece = getPiece(new ChessPosition(i+1,j+1));
                if (piece != null && piece.getTeamColor() == teamColor) {
                    positions.add(new ChessPosition(i + 1,j + 1));
                }
            }
        }

        return positions;
    }

    /**
     * Checks whether the position is attacked by the opposing team
     *
     * @param position the position we want to check for attacks
     * @param attackingTeam the color of the attacking team
     * @return whether the position is attacked
     */
    public boolean positionIsAttacked(ChessPosition position, ChessGame.TeamColor attackingTeam) {
        // Find all the pieces of the attacking team
        ArrayList<ChessPosition> pieces = findAllPieces(attackingTeam);

        // Loop through each piece's moves to determine if the position is attacked
        for (int i = 0; i < pieces.size(); ++i){
            ChessPosition currentPosition = pieces.get(i);
            ChessPiece piece = getPiece(currentPosition);

            // If the piece exists and is the attacking team, find its moves.
            if (piece != null && piece.getTeamColor() == attackingTeam) {
                ArrayList<ChessMove> pieceMoves = (ArrayList<ChessMove>) piece.pieceMoves(this, currentPosition);

                // Loop through all of this piece's moves. If it's end position is my position, we are attacked
                for (int k = 0; k < pieceMoves.size(); k++) {
                    if (position.equals(pieceMoves.get(k).getEndPosition())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Sets all pieces to not be en passantable
     */
    public void resetEnPassant() {
        for (int i = 0; i < squares.length; ++i) {
            for (int j = 0; j < squares[i].length; ++j) {
                if (getPiece(new ChessPosition(i + 1, j + 1)) == null) { continue; }
                getPiece(new ChessPosition(i + 1, j + 1)).setEnPassantable(false);
            }
        }
    }
}
