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
    private ChessGame.TeamColor teamColor;
    private PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor teamColor, PieceType pieceType) {
        this.teamColor = teamColor;
        this.pieceType = pieceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "teamColor=" + teamColor +
                ", pieceType=" + pieceType +
                '}';
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
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    private void addMove(Collection<ChessMove> possibleMoves, ChessBoard board, ChessPosition myPosition, int row, int col, boolean canLoop) {
        int i = 1;
        while (true) {
            ChessPosition frontMove = new ChessPosition(myPosition.getRow() + row * i, myPosition.getColumn() + col * i);
            i++;
            if (!frontMove.inBounds()) { //check if frontMove not in bounds
                break;
            }
            if (board.getPiece(frontMove) != null && board.getPiece(frontMove).teamColor == board.getPiece(myPosition).teamColor) {
                break; // check frontMove spot is empty and same team
            }
            possibleMoves.add(new ChessMove(myPosition, frontMove, null));
            if (board.getPiece(frontMove) != null && board.getPiece(frontMove).teamColor != board.getPiece(myPosition).teamColor) {
                break; // check front move is empty and other team
            }
            if (!canLoop){
                break;
            }
        }
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

//ROOKRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
        if (pieceType == PieceType.ROOK) {
//          R FRONT
            addMove(possibleMoves, board, myPosition, 1, 0, true);
//          R BACK
            addMove(possibleMoves, board, myPosition, -1, 0, true);
//          R RIGHT
            addMove(possibleMoves, board, myPosition, 0, 1, true);
//          R LEFT
            addMove(possibleMoves, board, myPosition, 0, -1, true);
//ROOKRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR

//KNIGHTKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
        } else if (pieceType == PieceType.KNIGHT) {
//          K FRONT
            addMove(possibleMoves, board, myPosition, 2, 1, false);
//          K BACK
            addMove(possibleMoves, board, myPosition, 2, -1, false);
//          K RIGHT
            addMove(possibleMoves, board, myPosition, 1, 2, false);
//          K LEFT
            addMove(possibleMoves, board, myPosition, 1, -2, false);
//          K FRONT RIGHT
            addMove(possibleMoves, board, myPosition, -2, 1, false);
//          K FRONT LEFT
            addMove(possibleMoves, board, myPosition, -2, -1, false);
//          K BACK RIGHT
            addMove(possibleMoves, board, myPosition, -1, 2, false);
//          K BACK LEFT
            addMove(possibleMoves, board, myPosition, -1, -2, false);

//KNIGHTKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK

//BISHOPBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        } else if (pieceType == PieceType.BISHOP) {
//          B FRONT RIGHT
            addMove(possibleMoves, board, myPosition, 1, 1, true);
//          B FRONT LEFT
            addMove(possibleMoves, board, myPosition, 1, -1, true);
//          B BACK RIGHT
            addMove(possibleMoves, board, myPosition, -1, 1, true);
//          B BACK LEFT
            addMove(possibleMoves, board, myPosition, -1, -1, true);
//BISHOPBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB

//QUEENQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ
        } else if (pieceType == PieceType.QUEEN) {
//          Q FRONT
            addMove(possibleMoves, board, myPosition, 1, 0, true);
//          Q BACK
            addMove(possibleMoves, board, myPosition, -1, 0, true);
//          Q RIGHT
            addMove(possibleMoves, board, myPosition, 0, 1, true);
//          Q LEFT
            addMove(possibleMoves, board, myPosition, 0, -1, true);
//          Q FRONT RIGHT
            addMove(possibleMoves, board, myPosition, 1, 1, true);
//          Q FRONT LEFT
            addMove(possibleMoves, board, myPosition, 1, -1, true);
//          Q BACK RIGHT
            addMove(possibleMoves, board, myPosition, -1, 1, true);
//          Q BACK LEFT
            addMove(possibleMoves, board, myPosition, -1, -1, true);
//            ChessPosition newKingPosition = new ChessPosition(myPosition.getRow()+1, myPosition.getColumn()+1);
//QUEENQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ

//KINGKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
        } else if (pieceType == PieceType.KING) {
//            K FRONT
            addMove(possibleMoves, board, myPosition, 1, 0, false);
//          K BACK
            addMove(possibleMoves, board, myPosition, -1, 0, false);
//          K RIGHT
            addMove(possibleMoves, board, myPosition, 0, 1, false);
//          K LEFT
            addMove(possibleMoves, board, myPosition, 0, -1, false);
//          K FRONT RIGHT
            addMove(possibleMoves, board, myPosition, 1, 1, false);
//          K FRONT LEFT
            addMove(possibleMoves, board, myPosition, 1, -1, false);
//          K BACK RIGHT
            addMove(possibleMoves, board, myPosition, -1, 1, false);
//          K BACK LEFT
            addMove(possibleMoves, board, myPosition, -1, -1, false);


//KINGKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK

//PAWNPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
        } else if (pieceType == PieceType.PAWN) {
            int moveDirection;
            if(teamColor == ChessGame.TeamColor.BLACK){
                moveDirection = -1;
            }
            else{
                moveDirection = 1;
            }
            ChessPosition frontMove;
            frontMove = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn());
            if (myPosition.inBounds()) {
//                FRONT MOVE/PROMOTION
                ChessPiece frontpiece = board.getPiece(frontMove);
                if (frontpiece == null) {
                    if (frontMove.getRow() == 8 || frontMove.getRow() == 1) {
                        possibleMoves.add(new ChessMove(myPosition, frontMove, PieceType.QUEEN));
                        possibleMoves.add(new ChessMove(myPosition, frontMove, PieceType.ROOK));
                        possibleMoves.add(new ChessMove(myPosition, frontMove, PieceType.KNIGHT));
                        possibleMoves.add(new ChessMove(myPosition, frontMove, PieceType.BISHOP));
                    }
                    else{
                        possibleMoves.add(new ChessMove(myPosition, frontMove, null));
                    }
//                        DOUBLE MOVE
                    ChessPosition doubleMove = new ChessPosition(myPosition.getRow() + 2* moveDirection, myPosition.getColumn());
                    if (doubleMove.inBounds()){
                        ChessPiece doublePiece = board.getPiece(doubleMove);
                        if ((doublePiece == null && myPosition.getRow() == 2 && moveDirection == 1) || (doublePiece == null && myPosition.getRow() == 7 && moveDirection == -1)) {
                            possibleMoves.add(new ChessMove(myPosition, doubleMove, null));
                        }
                    }
                }
                //                DIAGONAL RIGHT
                ChessPosition diagonalRightMove = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn() + 1);
                if (diagonalRightMove.inBounds()) {
                    ChessPiece diagonalRightPiece = board.getPiece(diagonalRightMove);
                    if (diagonalRightPiece != null && diagonalRightPiece.teamColor != board.getPiece(myPosition).teamColor) {
                        if (diagonalRightMove.getRow() == 8 || diagonalRightMove.getRow() == 1) {
                            possibleMoves.add(new ChessMove(myPosition, diagonalRightMove, PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, diagonalRightMove, PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, diagonalRightMove, PieceType.KNIGHT));
                            possibleMoves.add(new ChessMove(myPosition, diagonalRightMove, PieceType.BISHOP));
                        }
                        else{
                            possibleMoves.add(new ChessMove(myPosition, diagonalRightMove, null));
                        }
                    }
                }
                //                DIAGONAL LEFT
                ChessPosition diagonalLeftMove = new ChessPosition(myPosition.getRow() + moveDirection, myPosition.getColumn() - 1);
                if(diagonalLeftMove.inBounds()){
                    ChessPiece diagonalLeftPiece = board.getPiece(diagonalLeftMove);
                    if (diagonalLeftPiece != null && diagonalLeftPiece.teamColor != board.getPiece(myPosition).teamColor) {
                        if (diagonalLeftMove.getRow() == 8 || diagonalLeftMove.getRow() == 1) {
                            possibleMoves.add(new ChessMove(myPosition, diagonalLeftMove, PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, diagonalLeftMove, PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, diagonalLeftMove, PieceType.KNIGHT));
                            possibleMoves.add(new ChessMove(myPosition, diagonalLeftMove, PieceType.BISHOP));
                        }
                        else{
                            possibleMoves.add(new ChessMove(myPosition, diagonalLeftMove, null));
                        }
                    }
                }
            }
        }
        return possibleMoves;
    }
}
