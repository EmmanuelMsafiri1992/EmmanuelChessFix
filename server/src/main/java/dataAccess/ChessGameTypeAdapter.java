package dataAccess;

import chess.ChessGame;
import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.JsonToken;
import java.io.IOException;

public class ChessGameTypeAdapter extends TypeAdapter<ChessGame> {
    @Override
    public void write(JsonWriter out, ChessGame game) throws IOException {
        out.beginObject();
        out.name("teamTurn").value(game.getTeamTurn().toString());
        out.name("board");
        new ChessBoardTypeAdapter().write(out, game.getBoard());
        out.endObject();
    }

    @Override
    public ChessGame read(JsonReader in) throws IOException {
        ChessGame game = new ChessGame();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("teamTurn")) {
                game.setTeamTurn(ChessGame.TeamColor.valueOf(in.nextString()));
            } else if (name.equals("board")) {
                game.setBoard(new ChessBoardTypeAdapter().read(in));
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        return game;
    }
}

class ChessBoardTypeAdapter extends TypeAdapter<ChessBoard> {
    @Override
    public void write(JsonWriter out, ChessBoard board) throws IOException {
        out.beginArray();
        for (int row = 1; row <= 8; row++) {
            out.beginArray();
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = board.getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    out.nullValue();
                } else {
                    out.beginObject();
                    out.name("color").value(piece.getTeamColor().toString());
                    out.name("type").value(piece.getPieceType().toString());
                    out.endObject();
                }
            }
            out.endArray();
        }
        out.endArray();
    }

    @Override
    public ChessBoard read(JsonReader in) throws IOException {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        in.beginArray();
        for (int row = 8; row >= 1; row--) {
            in.beginArray();
            for (int col = 1; col <= 8; col++) {
                if (in.peek() == JsonToken.NULL) {
                    in.skipValue();
                    continue;
                }
                in.beginObject();
                String color = null;
                String type = null;
                while (in.hasNext()) {
                    String name = in.nextName();
                    if (name.equals("color")) {
                        color = in.nextString();
                    } else if (name.equals("type")) {
                        type = in.nextString();
                    } else {
                        in.skipValue();
                    }
                }
                in.endObject();
                if (color != null && type != null) {
                    ChessPiece piece = new ChessPiece(
                            ChessGame.TeamColor.valueOf(color),
                            ChessPiece.PieceType.valueOf(type)
                    );
                    board.addPiece(new ChessPosition(row, col), piece);
                }
            }
            in.endArray();
        }
        in.endArray();
        return board;
    }
}