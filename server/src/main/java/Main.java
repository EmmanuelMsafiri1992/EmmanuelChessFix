import chess.*;
import com.google.gson.Gson;
import dataAccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;
import java.util.Collection;

// Main class to run the chess server and demonstrate a chess piece
public class Main {
    // Service objects for user, authentication, and game operations
    private final UserService userService;
    private final AuthService authService;
    private final GameService gameService;
    // Gson instance for JSON serialization/deserialization
    private final Gson gson;

    /**
     * Constructor initializes services and Gson.
     */
    public Main() {
        // Initialize in-memory user data access object
        UserDAO userDAO = new MemoryUserDAO();
        // Initialize in-memory auth data access object
        AuthDAO authDAO = new MemoryAuthDAO();
        // Initialize in-memory game data access object
        GameDAO gameDAO = new MemoryGameDAO();
        // Initialize user service with user and auth DAOs
        this.userService = new UserService(userDAO, authDAO);
        // Initialize auth service with auth DAO
        this.authService = new AuthService(authDAO);
        // Initialize game service with game and auth DAOs
        this.gameService = new GameService(gameDAO, authDAO);
        // Initialize Gson for JSON processing
        this.gson = new Gson();
    }

    /**
     * Starts the server on the specified port and sets up endpoints.
     *
     * @param desiredPort the port to run the server on
     * @return the actual port used by the server
     */
    public int run(int desiredPort) {
        // Set the server port to the desired port
        Spark.port(desiredPort);
        // Serve static files from the 'web' directory
        Spark.staticFiles.location("web");

        // Endpoint to register a new user
        Spark.post("/user", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            try {
                // Parse request body into userData object
                UserData user = gson.fromJson(req.body(), UserData.class);
                // Check if user data is null
                if (user == null) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("Error bad request"));
                }
                // Register user and get auth token
                AuthData authData = userService.register(user);
                // Set status to 200 for success
                res.status(200);
                // Return auth data as JSON
                return gson.toJson(authData);
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 400 for general errors
                res.status(400);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error bad request"));
            }
        });

        // Endpoint to log in a user
        Spark.post("/session", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            try {
                // Parse request body into userData object
                UserData user = gson.fromJson(req.body(), UserData.class);
                // Check if user data is null
                if (user == null) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("Error bad request"));
                }
                // Log in user and get auth token
                AuthData authData = userService.login(user);
                // Set status to 200 for success
                res.status(200);
                // Return auth data as JSON
                return gson.toJson(authData);
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 400 for general errors
                res.status(400);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error bad request"));
            }
        });

        // Endpoint to log out a user
        Spark.post("/delete", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            // Get auth token from Authorization header
            String authToken = req.headers("authorization");
            try {
                // Log out user using auth token
                authService.logout(authToken);
                // Set status to 200 for success
                res.status(200);
                // Return empty JSON object
                return "{}";
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 400 for general errors
                res.status(400);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error bad request"));
            }
        });

        // Endpoint to list all games
        Spark.get("/game", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            // Get auth token from Authorization header
            String authToken = req.headers("authorization");
            try {
                // List games and wrap in ListGameResponse
                ListGameResponse response = new ListGameResponse(gameService.listGames(authToken));
                // Set status to 200 for success
                res.status(200);
                // Return games list as JSON
                return gson.toJson(response);
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 401 for general errors
                res.status(401);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error unauthorized"));
            }
        });

        // Endpoint to create a new game
        Spark.post("/game", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            // Get auth token from Authorization header
            String auth = req.headers("authorization");
            try {
                // Parse request body into GameRequest object
                GameRequest request = gson.fromJson(req.body(), GameRequest.class);
                // Check if request is null
                if (request == null || request.gameName() == null) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("Error bad request"));
                }
                // Create game and get game data
                GameData game = gameService.createGame(auth, request.gameName());
                // Set status to 200 for success
                res.status(200);
                // Return game ID in GameResponse as JSON
                return gson.toJson(new GameResponse(game.gameID()));
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 401 for general errors
                res.status(401);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error unauthorized"));
            }
        });

        // Endpoint to join a game
        Spark.put("/game", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            // Get auth token from Authorization header
            String auth = req.headers("authorization");
            try {
                // Parse request body into JoinGameRequest object
                JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
                // Check if request is null or invalid
                if (joinGameRequest == null || joinGameRequest.gameID() <= 0 || joinGameRequest.playerColor() == null) {
                    res.status(400);
                    return gson.toJson(new ErrorResponse("Error bad request"));
                }
                // Join game with specified game ID and player color
                gameService.joinGame(auth, joinGameRequest.gameID(), joinGameRequest.playerColor());
                // Set status to 200 for success
                res.status(200);
                // Return empty JSON object
                return "{}";
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 401 for general errors
                res.status(401);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error unauthorized"));
            }
        });

        // Endpoint to clear all data
        Spark.delete("/db", (req, res) -> {
            // Set response type to JSON
            res.type("application/json");
            try {
                // Clear all game data
                gameService.clear();
                // Clear all auth data
                authService.clear();
                // Clear all user data
                userService.clearAll();
                // Set status to 200 for success
                res.status(200);
                // Return empty JSON object
                return "{}";
            } catch (DataAccessException e) {
                // Set status based on error message
                res.status(getStatusCode(e.getMessage()));
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error " + e.getMessage()));
            } catch (Exception e) {
                // Set status to 401 for general errors
                res.status(401);
                // Return error message as JSON
                return gson.toJson(new ErrorResponse("Error unauthorized"));
            }
        });

        // Initialize the Spark server
        Spark.init();
        // Wait for the server to start
        Spark.awaitInitialization();
        // Return the port the server is running on
        return Spark.port();
    }

    /**
     * Stops the server.
     */
    public void stop() {
        // Stop the Spark server
        Spark.stop();
        // Wait for the server to fully stop
        Spark.awaitStop();
    }

    /**
     * Entry point of the application, demonstrates a chess piece and starts the server.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Create a new white pawn chess piece
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        // Print the chess piece with a prefix
        System.out.println("♕ 240 Chess Server: " + piece);
        // Create a new Main instance
        Main runServer = new Main();
        // Start the server on port 8081
        runServer.run(8081);
    }

    // Maps error messages to HTTP status codes
    private int getStatusCode(String message) {
        // Convert message to lowercase for comparison
        String mes = message.toLowerCase();
        // Return 400 for bad request errors
        if (mes.contains("bad request") || mes.contains("game not found")) {
            return 400;
            // Return 401 for unauthorized errors
        } else if (mes.contains("unauthorized")) {
            return 401;
            // Return 403 for already taken errors
        } else if (mes.contains("already taken") || mes.contains("already exists") || mes.contains("player color required")) {
            return 403;
        }
        // Default to 500 for other errors
        return 500;
    }

    // Record for error response
    private record ErrorResponse(String message) {}
    // Record for listing games response
    private record ListGameResponse(Collection<GameData> games) {}
    // Record for game creation response
    private record GameResponse(int gameID) {}
    // Record for game creation request
    private record GameRequest(String gameName) {}
    // Record for joining a game request
    private record JoinGameRequest(int gameID, String playerColor) {}
}