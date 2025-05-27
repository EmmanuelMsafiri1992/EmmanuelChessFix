package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataAccess.*;
import model.*;
import service.*;
import spark.*;
import java.util.*;

// Main server class for handling HTTP requests for user, auth, and game services
public class Server {
    // Service objects for business logic
    private final userService userService;
    private final authService authService;
    private final gameService gameService;
    // Gson for JSON serialization/deserialization
    private final Gson gson;

    // Constructor initializes data access objects and services
    public Server() {
        // Initialize in-memory DAOs for data storage
        userDAO userDAO = new MemoryUserDAO(); // Stores user data
        authDAO authDAO = new MemoryAuthDAO(); // Stores authentication tokens
        gameDAO gameDAO = new MemoryGameDAO(); // Stores game data
        // Initialize services with DAOs
        this.userService = new userService(userDAO, authDAO); // Manages user operations
        this.authService = new authService(authDAO); // Manages authentication
        this.gameService = new gameService(gameDAO, authDAO); // Manages game operations
        // Create Gson instance for JSON processing
        this.gson = new GsonBuilder().create();
    }

    // Starts the server on the specified port and sets up endpoints
    public int run(int desiredPort) {
        Spark.port(desiredPort); // Set the server port
        Spark.staticFiles.location("web"); // Serve static files from 'web' directory
        // Define HTTP endpoints
        Spark.post("/user", this::register); // Endpoint for user registration
        Spark.post("/session", this::login); // Endpoint for user login
        Spark.delete("/session", this::logout); // Endpoint for user logout
        Spark.get("/game", this::listgames); // Endpoint to list all games
        Spark.post("/game", this::createGame); // Endpoint to create a new game
        Spark.put("/game", this::joinGame); // Endpoint to join a game
        Spark.delete("/db", this::clear); // Endpoint to clear all data
        Spark.init(); // Initialize the Spark server
        Spark.awaitInitialization(); // Wait for server to start
        return Spark.port(); // Return the actual port used
    }

    // Stops the server
    public void stop() {
        Spark.stop(); // Stop the Spark server
        Spark.awaitStop(); // Wait for server to fully stop
    }

    // Handles user registration
    private Object register(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        String requestBody = request.body(); // Get raw request body
        userData user; // Declare userData variable
        try {
            // Parse request body into userData object
            user = gson.fromJson(requestBody, userData.class);
        } catch (Exception e) {
            // Handle JSON parsing errors
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Error: bad request")); // Return error
        }
        try {
            // Register user and get auth token
            authData authData = userService.register(user);
            response.status(200); // Set status to 200 (OK)
            return gson.toJson(authData); // Return auth data as JSON
        } catch (DataAccessException e) {
            // Handle registration errors
            response.status(getErrorStatus(e)); // Set appropriate error status
            // Customize message for "already taken" error
            String mes = e.getMessage().toLowerCase().contains("already taken") ?
                    "Error: already taken" : "Error: " + e.getMessage();
            return gson.toJson(new ErrorResponse(mes)); // Return error message
        }
    }

    // Handles clearing all data
    private Object clear(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        try {
            // Clear all data from services
            userService.clear(); // Clear user data
            gameService.clear(); // Clear game data
            authService.clear(); // Clear auth data
            response.status(200); // Set status to 200 (OK)
            return "{}"; // Return empty JSON object
        } catch (DataAccessException e) {
            // Handle data clearing errors
            response.status(getErrorStatus(e)); // Set appropriate error status
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage())); // Return error
        }
    }

    // Handles joining a game
    private Object joinGame(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        String authToken = request.headers("Authorization"); // Get auth token from header
        JoinGameRequest join; // Declare join request variable
        try {
            // Parse request body into JoinGameRequest
            join = gson.fromJson(request.body(), JoinGameRequest.class);
        } catch (Exception e) {
            // Handle JSON parsing errors
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Error - Bad Request smh")); // Return error
        }
        // Validate join request
        if (join == null || join.gameID() <= 0 || join.playerColor() == null) {
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Error - Bad Request, real bad buddy")); // Return error
        }
        try {
            // Join game with auth token, game ID, and player color
            gameService.joinGame(authToken, join.gameID(), join.playerColor());
            response.status(200); // Set status to 200 (OK)
            return "{}"; // Return empty JSON object
        } catch (DataAccessException e) {
            // Handle game joining errors
            response.status(getErrorStatus(e)); // Set appropriate error status
            return gson.toJson(new ErrorResponse("error - " + e.getMessage())); // Return error
        }
    }

    // Handles creating a new game
    private Object createGame(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        String authToken = request.headers("Authorization"); // Get auth token from header
        GameNameRequest gameName; // Declare game name request variable
        try {
            // Parse request body into GameNameRequest
            gameName = gson.fromJson(request.body(), GameNameRequest.class);
        } catch (Exception e) {
            // Handle JSON parsing errors
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Error - Bad Request")); // Return error
        }
        // Validate game name
        if (gameName == null || gameName.gameName() == null || gameName.gameName().trim().isEmpty()) {
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Error - Bad Request")); // Return error
        }
        try {
            // Create game with auth token and game name
            gameData game = gameService.createGame(authToken, gameName.gameName());
            response.status(200); // Set status to 200 (OK)
            return gson.toJson(new CreateGameResponse(game.gameID())); // Return game ID
        } catch (DataAccessException e) {
            // Handle game creation errors
            response.status(getErrorStatus(e)); // Set appropriate error status
            return gson.toJson(new ErrorResponse("Error - " + e.getMessage())); // Return error
        }
    }

    // Handles listing all games
    private Object listgames(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        String authToken = request.headers("Authorization"); // Get auth token from header
        try {
            // Get list of games using auth token
            Collection<gameData> games = gameService.listGames(authToken);
            response.status(200); // Set status to 200 (OK)
            return gson.toJson(new ListGamesResponse(games)); // Return games list as JSON
        } catch (DataAccessException e) {
            // Handle game listing errors
            response.status(getErrorStatus(e)); // Set appropriate error status
            return gson.toJson(new ErrorResponse("Error - " + e.getMessage())); // Return error
        }
    }

    // Handles user logout
    private Object logout(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        String authToken = request.headers("Authorization"); // Get auth token from header
        try {
            // Logout user using auth token
            authService.logout(authToken);
            response.status(200); // Set status to 200 (OK)
            return "{}"; // Return empty JSON object
        } catch (DataAccessException e) {
            // Handle logout errors
            response.status(getErrorStatus(e)); // Set appropriate error status
            return gson.toJson(new ErrorResponse("Error" + e.getMessage())); // Return error
        }
    }

    // Handles user login
    private Object login(Request request, Response response) {
        response.type("application/json"); // Set response type to JSON
        String requestBody = request.body(); // Get raw request body
        // Check for empty or null request body
        if (requestBody == null || requestBody.trim().isEmpty()) {
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Bad Request")); // Return error
        }
        JsonObject json; // Declare JSON object variable
        try {
            // Parse request body into JSON object
            json = JsonParser.parseString(requestBody).getAsJsonObject();
        } catch (Exception e) {
            // Handle JSON parsing errors
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Bad Request")); // Return error
        }
        // Check for required fields
        boolean hasUsername = json.has("username") && !json.get("username").isJsonNull();
        boolean hasPassword = json.has("password") && !json.get("password").isJsonNull();
        if (!hasUsername || !hasPassword) {
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Error: Bad Request")); // Return error
        }
        try {
            // Extract username and password
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            // Validate username and password
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                response.status(400); // Set status to 400 (Bad Request)
                return gson.toJson(new ErrorResponse("Bad Request")); // Return error
            }
            // Create userData object for login
            userData user = new userData(username.trim(), password.trim(), null);
            try {
                // Attempt login
                authData authData = userService.login(user);
                response.status(200); // Set status to 200 (OK)
                return gson.toJson(authData); // Return auth data as JSON
            } catch (DataAccessException e) {
                // Handle login errors
                int status = getErrorStatus(e); // Get appropriate error status
                response.status(status); // Set status
                return gson.toJson(new ErrorResponse("Error: " + e.getMessage())); // Return error
            }
        } catch (Exception e) {
            // Handle general errors
            response.status(400); // Set status to 400 (Bad Request)
            return gson.toJson(new ErrorResponse("Bad Requesttttt")); // Return error
        }
    }

    // Maps exception messages to HTTP status codes
    private int getErrorStatus(DataAccessException e) {
        String mes = e.getMessage().toLowerCase(); // Get lowercase error message
        if (mes.contains("bad request") || mes.contains("game not found")) {
            return 400; // Bad Request
        } else if (mes.contains("unauthorized")) {
            return 401; // Unauthorized
        } else if (mes.contains("already exists") || mes.contains("already taken")) {
            return 403; // Forbidden
        }
        return 500; // Internal Server Error
    }

    // Record for error response
    private record ErrorResponse(String message) {}
    // Record for listing games response
    private record ListGamesResponse(Collection<gameData> games) {}
    // Record for create game response
    private record CreateGameResponse(int gameID) {}
    // Record for game name request
    private record GameNameRequest(String gameName) {}
    // Record for join game request
    private record JoinGameRequest(int gameID, String playerColor) {}
}