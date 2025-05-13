package chess;

import java.util.ArrayList;

public interface PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class KingMovesCalculator implements PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        // Find all adjacent spaces by adding these to current position
        int[][] adjacent = {
                {-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}
        };

        // Loop through all adjacent spaces
        for (int i = 0; i < adjacent.length; ++i) {
            // Create a new position for each adjacent space
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + adjacent[i][0], myPosition.getColumn() + adjacent[i][1]);

            // If the space is outside the chess board, skip it
            if (!newPosition.insideBoard()) { continue; }

            // If the space is occupied by a piece of the same color, skip it
            if (board.getPiece(newPosition) != null && board.getPiece(myPosition).getTeamColor() == board.getPiece(newPosition).getTeamColor()) {
                continue;
            }

            // Add this new position to the possible moves
            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
        }

        // Castling
        if(!board.getPiece(myPosition).ifMoved() && myPosition.getColumn() == 5) {
            ChessPiece myPiece = board.getPiece(myPosition);

            // Switch the color to find the opposing color
            ChessGame.TeamColor opposingTeam = myPiece.getTeamColor() ==
                    ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

            // If the king hasn't moved, check if the rooks haven't moved.
            ChessPiece leftCorner = board.getPiece(new ChessPosition(myPosition.getRow(), 1));
            if(leftCorner != null && leftCorner.getPieceType() == ChessPiece.PieceType.ROOK && !leftCorner.ifMoved()) {
                // Make sure the path is clear and the king will never be in check
                if (board.getPiece(new ChessPosition(myPosition.getRow(), 2)) == null &&
                        board.getPiece(new ChessPosition(myPosition.getRow(), 3)) == null &&
                        board.getPiece(new ChessPosition(myPosition.getRow(), 4)) == null &&
                        !board.positionIsAttacked(new ChessPosition(myPosition.getRow(), 3), opposingTeam) &&
                        !board.positionIsAttacked(new ChessPosition(myPosition.getRow(), 4), opposingTeam) &&
                        !board.positionIsAttacked(myPosition, opposingTeam)) {

                    // Create the new move and mark the castling flag
                    ChessMove newMove = new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), 3), null);
                    possibleMoves.add(newMove);
                }
            }

            ChessPiece rightCorner = board.getPiece(new ChessPosition(myPosition.getRow(), 8));
            if(rightCorner != null && rightCorner.getPieceType() == ChessPiece.PieceType.ROOK && !rightCorner.ifMoved()) {
                // Make sure the path is clear and the king will never be in check
                if (board.getPiece(new ChessPosition(myPosition.getRow(), 6)) == null &&
                        board.getPiece(new ChessPosition(myPosition.getRow(), 7)) == null &&
                        !board.positionIsAttacked(new ChessPosition(myPosition.getRow(), 6), opposingTeam) &&
                        !board.positionIsAttacked(new ChessPosition(myPosition.getRow(), 7), opposingTeam) &&
                        !board.positionIsAttacked(myPosition, opposingTeam)) {

                    // Create the new move and mark the castling flag
                    ChessMove newMove = new ChessMove(myPosition, new ChessPosition(myPosition.getRow(), 7), null);
                    possibleMoves.add(newMove);
                }
            }

        }

        return possibleMoves;
    }
}

class BishopMovesCalculator implements PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] directions = {
                {1,1},{-1,1},{-1,-1},{1,-1}
        };

        // Find all the moves in each direction
        LineMoveCalculator newLineMoves = new LineMoveCalculator(directions);
        possibleMoves = newLineMoves.getPositions(myPosition, board);

        return possibleMoves;
    }
}

class RookMovesCalculator implements PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] directions = {
                {1,0},{-1,0},{0,1},{0,-1}
        };


        // Find all the moves in each direction
        LineMoveCalculator newLineMoves = new LineMoveCalculator(directions);
        possibleMoves = newLineMoves.getPositions(myPosition, board);

        return possibleMoves;
    }
}

class QueenMovesCalculator implements PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] directions = {
                {1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{-1,-1},{1,-1}
        };

        // Find all the moves in each direction
        LineMoveCalculator newLineMoves = new LineMoveCalculator(directions);
        possibleMoves = newLineMoves.getPositions(myPosition, board);

        return possibleMoves;
    }
}

class KnightMovesCalculator implements PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] jumps = {
                {1,2},{1,-2},{-1,2},{-1,-2},{2,1},{2,-1},{-2,1},{-2,-1}
        };

        for (int i = 0; i < jumps.length; ++i) {
            // Create a new position for each adjacent space
            ChessPosition newPosition = new ChessPosition(myPosition.getRow() + jumps[i][0], myPosition.getColumn() + jumps[i][1]);

            // If the space is outside the chess board, skip it
            if (!newPosition.insideBoard()) { continue; }

            // If the space is occupied by a piece of the same color, skip it
            if (board.getPiece(newPosition) != null) {
                if (board.getPiece(myPosition).getTeamColor() == board.getPiece(newPosition).getTeamColor()) {
                    continue;
                }
            }

            // Add this new position to the possible moves
            possibleMoves.add(new ChessMove(myPosition, newPosition, null));
        }

        return possibleMoves;
    }
}

class PawnMovesCalculator implements PieceMovesCalculator {
    public ArrayList<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        // Determine forward direction
        int forward = board.getPiece(myPosition).getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;

        // Move forward one space if unoccupied
        ChessPosition forwardPosition = new ChessPosition(myPosition.getRow() + forward, myPosition.getColumn());
        if (forwardPosition.insideBoard() && board.getPiece(forwardPosition) == null) {
            // Add this new position to the possible moves
            possibleMoves.add(new ChessMove(myPosition, forwardPosition, null));
        }

        // Move forward two spaces if possible
        ChessPosition forward2Position = new ChessPosition(myPosition.getRow() + 2 * forward, myPosition.getColumn());
        if (myPosition.getRow() == (forward == 1 ? 2 : 7) && forward2Position.insideBoard()) {
            if (board.getPiece(forwardPosition) == null && board.getPiece(forward2Position) == null) {
                // Add this new position to the possible moves
                possibleMoves.add(new ChessMove(myPosition, forward2Position, null));
            }
        }

        // Diagonal Attacking
        ChessPosition leftAttackPosition = new ChessPosition(myPosition.getRow() + forward, myPosition.getColumn() - 1);
        ChessPosition rightAttackPosition = new ChessPosition(myPosition.getRow() + forward, myPosition.getColumn() + 1);
        if (leftAttackPosition.insideBoard() && board.getPiece(leftAttackPosition) != null) {
            if (board.getPiece(myPosition).getTeamColor() != board.getPiece(leftAttackPosition).getTeamColor()) {
                // Add this new position to the possible moves
                possibleMoves.add(new ChessMove(myPosition, leftAttackPosition, null));
            }
        }
        if (rightAttackPosition.insideBoard() && board.getPiece(rightAttackPosition) != null) {
            if (board.getPiece(myPosition).getTeamColor() != board.getPiece(rightAttackPosition).getTeamColor()) {
                // Add this new position to the possible moves
                possibleMoves.add(new ChessMove(myPosition, rightAttackPosition, null));
            }
        }

        // En Passant to the left
        ChessPosition leftAdj = new ChessPosition(myPosition.getRow(), myPosition.getColumn()-1);
        if (leftAdj.insideBoard()) {
            ChessPiece leftAdjPiece = board.getPiece(leftAdj);
            if (leftAdjPiece != null && myPosition.getRow() == (forward == 1 ? 5 : 4) && leftAdjPiece.getPieceType() == ChessPiece.PieceType.PAWN
                    && leftAdjPiece.getTeamColor() != board.getPiece(myPosition).getTeamColor() && leftAdjPiece.isEnPassantable()) {
                possibleMoves.add(new ChessMove(myPosition, leftAttackPosition, null));
            }
        }

        // En Passant to the right
        ChessPosition rightAdj = new ChessPosition(myPosition.getRow(), myPosition.getColumn()+1);
        if (rightAdj.insideBoard()) {
            ChessPiece rightAdjPiece = board.getPiece(rightAdj);
            if (rightAdjPiece != null && myPosition.getRow() == (forward == 1 ? 5 : 4) && rightAdjPiece.getPieceType() == ChessPiece.PieceType.PAWN
                    && rightAdjPiece.getTeamColor() != board.getPiece(myPosition).getTeamColor() && rightAdjPiece.isEnPassantable()) {
                possibleMoves.add(new ChessMove(myPosition, rightAttackPosition, null));
            }
        }


        // Promotion
        ArrayList<ChessMove> newPossibleMoves = new ArrayList<>();
        for (int i = 0; i < possibleMoves.toArray().length; ++i) {
            if(possibleMoves.get(i).getEndPosition().getRow() == (forward == 1 ? 8 : 1)) {
                ChessPosition start = possibleMoves.get(i).getStartPosition();
                ChessPosition end = possibleMoves.get(i).getEndPosition();

                newPossibleMoves.add(new ChessMove(start, end, ChessPiece.PieceType.QUEEN));
                newPossibleMoves.add(new ChessMove(start, end, ChessPiece.PieceType.ROOK));
                newPossibleMoves.add(new ChessMove(start, end, ChessPiece.PieceType.BISHOP));
                newPossibleMoves.add(new ChessMove(start, end, ChessPiece.PieceType.KNIGHT));
            } else {
                newPossibleMoves.add(possibleMoves.get(i));
            }
        }

        return newPossibleMoves;
    }
}
