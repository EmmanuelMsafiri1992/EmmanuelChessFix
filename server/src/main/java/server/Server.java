package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataAccess.*;
import model.AuthData;
import model.UserData;
import model.GameData;
import service.UserService;
import service.AuthService;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.util.Collection;

/**
 * Main server class for handling HTTP requests related to user, authentication, and game services.
 */
public class Server {
    /** Service for user-related operations. */
    private final UserService userService;
    /** Service for authentication operations. */
    private final AuthService authService;
    /** Service for game-related operations. */
    private final GameService gameService;
    /** Gson instance for JSON serialization and deserialization. */
    private final Gson gson;

    /**
     * Constructs a Server instance, initializing data access objects and services.
     */
    public Server() {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        this.userService = new UserService(userDAO, authDAO);
        this.authService = new AuthService(authDAO);
        this.gameService = new GameService(gameDAO, authDAO);
        this.gson = new GsonBuilder().create();
    }

    /**
     * Starts the server on the specified port and configures HTTP endpoints.
     *
     * @param desiredPort the port to run the server on
     * @return the actual port used by the server
     */
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearAll);
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    /**
     * Stops the server and waits for it to fully shut down.
     */
    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    /**
     * Handles user registration by creating a new user and authentication token.
     *
     * @param request  the HTTP request containing user data
     * @param response the HTTP response
     * @return a JSON response with authentication data or an error message
     */
    private Object register(Request request, Response response) {
        response.type("application/json");
        UserData user;
        try {
            user = gson.fromJson(request.body(), UserData.class);
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        try {
            AuthData authData = userService.register(user);
            response.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            String message = e.getMessage().toLowerCase().contains("already taken")
                    ? "Error: already taken" : "Error: " + e.getMessage();
            return gson.toJson(new ErrorResponse(message));
        }
    }

    /**
     * Clears all user, authentication, and game data from storage.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a JSON response indicating success or an error message
     */
    private Object clearAll(Request request, Response response) {
        response.type("application/json");
        try {
            userService.clearAll();
            gameService.clear();
            authService.clear();
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
    /**
     * Handles joining a game with the specified game ID and player color.
     *
     * @param request  the HTTP request containing join game data
     * @param response the HTTP response
     * @return a JSON response indicating success or an error message
     */
    private Object joinGame(Request request, Response response) {
        response.type("application/json");
        String authToken = request.headers("Authorization");
        JoinGameRequest join;
        try {
            join = gson.fromJson(request.body(), JoinGameRequest.class);
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        if (join == null || join.gameID() <= 0 || join.playerColor() == null) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        try {
            gameService.joinGame(authToken, join.gameID(), join.playerColor());
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Handles creating a new game with the specified name.
     *
     * @param request  the HTTP request containing game name data
     * @param response the HTTP response
     * @return a JSON response with the game ID or an error message
     */
    private Object createGame(Request request, Response response) {
        response.type("application/json");
        String authToken = request.headers("Authorization");
        GameNameRequest gameName;
        try {
            gameName = gson.fromJson(request.body(), GameNameRequest.class);
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        if (gameName == null || gameName.gameName() == null || gameName.gameName().trim().isEmpty()) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        try {
            GameData game = gameService.createGame(authToken, gameName.gameName());
            response.status(200);
            return gson.toJson(new CreateGameResponse(game.gameID()));
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Handles listing all available games.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return a JSON response with the list of games or an error message
     */
    private Object listGames(Request request, Response response) {
        response.type("application/json");
        String authToken = request.headers("Authorization");
        try {
            Collection<GameData> games = gameService.listGames(authToken);
            response.status(200);
            return gson.toJson(new ListGamesResponse(games));
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Handles user logout by invalidating the authentication token.
     *
     * @param request  the HTTP request containing the auth token
     * @param response the HTTP response
     * @return a JSON response indicating success or an error message
     */
    private Object logout(Request request, Response response) {
        response.type("application/json");
        String authToken = request.headers("Authorization");
        try {
            authService.logout(authToken);
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Handles user login and creates an authentication token.
     *
     * @param request  the HTTP request containing login credentials
     * @param response the HTTP response
     * @return a JSON response with authentication data or an error message
     */
    private Object login(Request request, Response response) {
        response.type("application/json");
        String requestBody = request.body();
        if (requestBody == null || requestBody.trim().isEmpty()) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        JsonObject json;
        try {
            json = JsonParser.parseString(requestBody).getAsJsonObject();
        } catch (Exception e) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        boolean hasUsername = json.has("username") && !json.get("username").isJsonNull();
        boolean hasPassword = json.has("password") && !json.get("password").isJsonNull();
        if (!hasUsername || !hasPassword) {
            response.status(400);
            return gson.toJson(new ErrorResponse("Error: bad request"));
        }
        try {
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                response.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
            UserData user = new UserData(username.trim(), password.trim(), null);
            AuthData authData = userService.login(user);
            response.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException e) {
            response.status(getErrorStatus(e));
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Maps exception messages to appropriate HTTP status codes.
     *
     * @param e the DataAccessException to process
     * @return the corresponding HTTP status code
     */
    private int getErrorStatus(DataAccessException e) {
        String message = e.getMessage().toLowerCase();
        if (message.contains("bad request") || message.contains("game not found")) {
            return 400; // Bad Request
        } else if (message.contains("unauthorized")) {
            return 401; // Unauthorized
        } else if (message.contains("already exists") || message.contains("already taken")) {
            return 403; // Forbidden
        }
        return 500; // Internal Server Error
    }

    /** Record for error response. */
    private record ErrorResponse(String message) {}

    /** Record for listing games response. */
    private record ListGamesResponse(Collection<GameData> games) {}

    /** Record for create game response. */
    private record CreateGameResponse(int gameID) {}

    /** Record for game name request. */
    private record GameNameRequest(String gameName) {}

    /** Record for join game request. */
    private record JoinGameRequest(int gameID, String playerColor) {}
}