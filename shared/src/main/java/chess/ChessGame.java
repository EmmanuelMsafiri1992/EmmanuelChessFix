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
    private TeamColor turn;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && turn == chessGame.turn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, turn);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "board=" + board +
                ", turn=" + turn +
                '}';
    }

    public ChessGame() {
        turn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }
        this.turn = team;
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
        if (startPosition == null) {
            throw new IllegalArgumentException("Start position cannot be null");
        }
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> valid = new ArrayList<>();
        for (ChessMove move : moves) {
//            simulate the move
            ChessPiece target = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(move.getStartPosition(), null);
            if (!isInCheck(piece.getTeamColor())) {
                valid.add(move);
            }
//            doing the actual move
            board.addPiece(startPosition, piece);
            board.addPiece(move.getEndPosition(), target);
        }
        return valid;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (move == null) {
            throw new InvalidMoveException("move cannot be null");
        }
//        checking start position piece for possible valid moves
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("piece cannot be null");
        }
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException("not your turn");
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("invalid move");
        }
//        checking start position piece's possible end moves
        ChessPiece target = board.getPiece(move.getEndPosition());
        if (target != null && target.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("cannot capture own piece");
        }
        ChessPiece newPiece = piece;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int endRow = move.getEndPosition().getRow();
            if ((endRow == 8 && piece.getTeamColor() == TeamColor.WHITE) ||
                    (endRow == 1 && piece.getTeamColor() == TeamColor.BLACK)) {
                if (move.getPromotionPiece() != null) {
                    newPiece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
                    board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
                    board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
                } else {
                    throw new InvalidMoveException("must specify promotion piece for pawn promotion");
                }
            }
            else{
                board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
                board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
            }
        }
        else if(piece.getPieceType() == ChessPiece.PieceType.QUEEN){
            board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
            board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
        }
        else if(piece.getPieceType() == ChessPiece.PieceType.BISHOP){
            board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
            board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
        }
        else if(piece.getPieceType() == ChessPiece.PieceType.KING){
            board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
            board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
        }
        else if(piece.getPieceType() == ChessPiece.PieceType.KNIGHT){
            board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
            board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
        }
        else if(piece.getPieceType() == ChessPiece.PieceType.ROOK){
            board.addPiece(move.getEndPosition(), newPiece); //new spot set to new piece with promotion
            board.addPiece(move.getStartPosition(), null); //old spot set to start pos with null piece
        }
        if (piece.getTeamColor() == TeamColor.WHITE){
            turn = TeamColor.BLACK;
        }
        else{
            turn = TeamColor.WHITE;
        }

//
//        int index = getPieceIndex(move.getStartPosition(), piece.getTeamColor());
//
//        if (piece.getTeamColor() == TeamColor.WHITE) whiteMoved[index] = true;
//        else blackMoved[index] = true;
//        board.addPiece(move.getEndPosition(), newPiece);
//        board.addPiece(move.getStartPosition(), null);
//        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
//    }
//
//    private int getPieceIndex(ChessPosition pos, TeamColor color) {
//        int row = pos.getRow() - 1;
//        int col = pos.getColumn() - 1;
//        if (color == TeamColor.WHITE) {
//            if (row == 0) return col + 8;
//            if (row == 1) return col;
//            else {
//                if (row == 7) return col + 8;
//                if (row == 6) {
//                    return col;
//                }
//            }
//        }
//        return -1;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck (TeamColor teamColor) {  // which team's color is getting passed into here?
//        find kings position, check if enemy piece can attack the king using nested for loop ***
        if (teamColor == null) {
            throw new IllegalArgumentException("team color cannot be null");
        }
        ChessPosition kingPos = findKing(teamColor);
        TeamColor opponent = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int fRow = 1; fRow <= 8; fRow++) {
            for (int fCol = 1; fCol <= 8; fCol++) {
                ChessPosition pos = new ChessPosition(fRow, fCol);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == opponent) {
                    Collection<ChessMove> chessMoves = piece.pieceMoves(board, pos);
                    for (ChessMove move : chessMoves) {
                        if (move.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    private ChessPosition findKing(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece kingPiece = board.getPiece(pos);
                if (kingPiece != null && kingPiece.getPieceType() == ChessPiece.PieceType.KING && kingPiece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        throw new IllegalStateException("king not found for team");
    }

    public boolean isInCheckmate (TeamColor teamColor){
        if (teamColor == null) {
            throw new IllegalArgumentException("team color cannot be null");
        }
        if(!isInCheck(teamColor)) {
            return false;}
//                    ChessPosition kingPos = findKing(teamColor);
//                    TeamColor opponent = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (int opRow = 1; opRow <= 8; opRow++) {
            for (int opCol = 1; opCol <= 8; opCol++) {
                ChessPosition opPos = new ChessPosition(opRow, opCol);
                ChessPiece opPiece = board.getPiece(opPos);
                if(opPiece != null && opPiece.getTeamColor() == teamColor){
                    Collection<ChessMove>chessMoves = validMoves(opPos);
                    if(chessMoves != null && !chessMoves.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate (TeamColor teamColor){
        if (teamColor == null) {
            throw new IllegalArgumentException("team color cnanot be null");
        }
        if (isInCheck(teamColor)) {
            return false;
        }
        for (int sRow = 1; sRow <= 8; sRow++) {
            for (int sCol = 1; sCol <= 8; sCol++) {
                ChessPosition sPos = new ChessPosition(sRow, sCol);
                ChessPiece sPiece = board.getPiece(sPos);
                if (sPiece != null && sPiece.getTeamColor() == teamColor) {
                    Collection<ChessMove> chessMoves = validMoves(sPos);
                    if (chessMoves != null && !chessMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard (ChessBoard board){
        if(board == null){
            throw new IllegalArgumentException("board cannot be null");
        }
        this.board = board;

    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard () {
        return board;
    }
}