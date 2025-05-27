import chess.*;

// Main class to demonstrate creating and printing a chess piece
public class Main {
    /**
     * Entry point of the application, creates a chess piece and prints it.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Create a new white pawn chess piece
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        // Print the chess piece with a descriptive prefix
        System.out.println("♕ 342 Chess Client: " + piece);
    }
}