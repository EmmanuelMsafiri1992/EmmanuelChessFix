package server;
import model.gameData;
import java.util.Collection;

public record ListGameResponse(Collection<gameData> game){ }