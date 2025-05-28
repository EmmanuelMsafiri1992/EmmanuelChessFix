package server;
import model.GameData;
import java.util.Collection;

public record ListGameResponse(Collection<GameData> game){ }