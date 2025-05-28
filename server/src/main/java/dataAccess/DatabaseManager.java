package dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseManager {
    private static String DATABASE_URL;
    private static String DATABASE_USERNAME;
    private static String DATABASE_PASSWORD;
    private static boolean initialized = false;

    static {
        try {
            loadProperties();
        } catch (Exception e) {
            System.err.println("Failed to load properties during static initialization: " + e.getMessage());
            throw new RuntimeException("Static initialization failed", e);
        }
    }

    public static void initialize() {
        if (!initialized) {
            synchronized (DatabaseManager.class) {
                if (!initialized) {
                    try {
                        createDatabase();
                        initialized = true;
                        System.out.println("DatabaseManager initialized successfully");
                    } catch (Exception e) {
                        System.err.println("Failed to initialize database: " + e.getMessage());
                        throw new RuntimeException("Database initialization failed", e);
                    }
                }
            }
        }
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Failed to initialize database: db.properties not found in classpath");
            }
            Properties prop = new Properties();
            prop.load(input);
            System.out.println("Loaded properties: " + prop);
            setDatabaseProperties(prop);
        } catch (Exception e) {
            System.err.println("Error loading properties: " + e.getMessage());
            throw new RuntimeException("Failed to load properties: " + e.getMessage(), e);
        }
    }

    public void loadProperties(Properties props) {
        System.out.println("Loading test properties: " + props);
        if (props == null || props.isEmpty()) {
            throw new RuntimeException("Missing required properties in provided Properties object");
        }
        String originalUrl = DATABASE_URL;
        String originalUsername = DATABASE_USERNAME;
        String originalPassword = DATABASE_PASSWORD;

        try {
            setDatabasePropertiesFromTest(props);
            createDatabase();
        } catch (Exception e) {
            System.err.println("Error applying test properties: " + e.getMessage());
            throw new RuntimeException("Failed to create database tables: " + e.getMessage(), e);
        } finally {
            DATABASE_URL = originalUrl;
            DATABASE_USERNAME = originalUsername;
            DATABASE_PASSWORD = originalPassword;
            System.out.println("Restored original properties: URL=" + DATABASE_URL);
        }
    }

    public void loadPropertiesFromResources() {
        System.out.println("Loading properties from resources (cleanup)");
        loadProperties();
    }

    private static void setDatabaseProperties(Properties prop) {
        DATABASE_URL = prop.getProperty("database.url");
        DATABASE_USERNAME = prop.getProperty("database.username", "");
        DATABASE_PASSWORD = prop.getProperty("database.password", "");
        System.out.println("Set database properties: URL=" + DATABASE_URL + ", Username=" + DATABASE_USERNAME);

        if (DATABASE_URL == null || DATABASE_URL.trim().isEmpty()) {
            throw new RuntimeException("Missing required properties in provided Properties object");
        }
    }

    private static void setDatabasePropertiesFromTest(Properties prop) {
        String dbName = prop.getProperty("db.name");
        String dbUser = prop.getProperty("db.user");
        String dbPassword = prop.getProperty("db.password");
        String dbHost = prop.getProperty("db.host");
        String dbPort = prop.getProperty("db.port");
        System.out.println("Set test database properties: dbName=" + dbName + ", dbUser=" + dbUser + ", dbHost=" + dbHost + ", dbPort=" + dbPort);

        if (dbName == null || dbUser == null || dbPassword == null || dbHost == null || dbPort == null) {
            throw new RuntimeException("Missing required properties in provided Properties object");
        }

        DATABASE_URL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
        DATABASE_USERNAME = dbUser;
        DATABASE_PASSWORD = dbPassword;
    }

    public static Connection getConnection() throws DataAccessException {
        initialize();
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
            conn.setAutoCommit(true);
            System.out.println("Database connection established: " + DATABASE_URL);
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to establish connection: " + e.getMessage());
            throw new DataAccessException("Unable to get connection: " + e.getMessage());
        }
    }

    private static void createDatabase() {
        String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    PRIMARY KEY (username)
                )
            """,
                """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT NOT NULL AUTO_INCREMENT,
                    whiteUsername VARCHAR(255),
                    blackUsername VARCHAR(255),
                    gameName VARCHAR(255) NOT NULL,
                    game TEXT NOT NULL,
                    PRIMARY KEY (gameID)
                )
            """,
                """
                CREATE TABLE IF NOT EXISTS authTokens (
                    authToken VARCHAR(255) NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    PRIMARY KEY (authToken)
                )
            """
        };
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            conn.setAutoCommit(true);
            String dbName = DATABASE_URL.substring(DATABASE_URL.lastIndexOf("/") + 1);
            System.out.println("Checking if database '" + dbName + "' is accessible");
            try (ResultSet rs = conn.getMetaData().getCatalogs()) {
                boolean dbExists = false;
                while (rs.next()) {
                    if (rs.getString(1).equals(dbName)) {
                        dbExists = true;
                        break;
                    }
                }
                System.out.println("Database '" + dbName + "' exists: " + dbExists);
            }

            for (String stmt : createStatements) {
                try (PreparedStatement ps = conn.prepareStatement(stmt)) {
                    ps.executeUpdate();
                    String tableName = stmt.contains("users") ? "users" : stmt.contains("games") ? "games" : "authTokens";
                    System.out.println("Executed table creation for: " + tableName);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to create database tables: " + e.getMessage());
            throw new RuntimeException("Failed to create database tables: " + e.getMessage(), e);
        }
    }

    public static void clearDatabase() throws DataAccessException {
        initialize();
        String[] clearStatements = {
                "DELETE FROM authTokens",
                "DELETE FROM games",
                "DELETE FROM users"
        };
        try (Connection conn = getConnection()) {
            for (String stmt : clearStatements) {
                try (PreparedStatement ps = conn.prepareStatement(stmt)) {
                    int rowsAffected = ps.executeUpdate();
                    System.out.println("Cleared " + rowsAffected + " rows from " + stmt.substring(12));
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to clear database: " + e.getMessage());
            throw new DataAccessException("Failed to clear database: " + e.getMessage());
        }
    }

    public static void createUser(String username, String password, String email) throws DataAccessException {
        initialize();
        System.out.println("Attempting to create user: " + username);
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected (createUser): " + rowsAffected);
            if (rowsAffected == 0) {
                throw new DataAccessException("No rows inserted for user: " + username);
            }
        } catch (SQLException e) {
            System.err.println("Error in createUser: " + e.getMessage());
            throw new DataAccessException("Failed to create user: " + e.getMessage());
        }
    }

    public static boolean userExists(String username) throws DataAccessException {
        initialize();
        System.out.println("Checking if user exists: " + username);
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    System.out.println("userExists check for " + username + ": " + exists);
                    return exists;
                }
                System.out.println("userExists check for " + username + ": no rows returned");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error in userExists: " + e.getMessage());
            throw new DataAccessException("Failed to check user existence: " + e.getMessage());
        }
    }

    public static int createGame(String gameName, String gameData) throws DataAccessException {
        initialize();
        System.out.println("Attempting to create game: " + gameName);
        String sql = "INSERT INTO games (gameName, game) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gameName);
            ps.setString(2, gameData);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected (createGame): " + rowsAffected);
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    System.out.println("Generated gameID: " + gameID);
                    return gameID;
                }
                throw new DataAccessException("Failed to retrieve generated gameID");
            }
        } catch (SQLException e) {
            System.err.println("Error in createGame: " + e.getMessage());
            throw new DataAccessException("Failed to create game: " + e.getMessage());
        }
    }

    public static boolean gameExists(int gameID) throws DataAccessException {
        initialize();
        System.out.println("Checking if game exists: gameID=" + gameID);
        String sql = "SELECT COUNT(*) FROM games WHERE gameID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    System.out.println("gameExists check for gameID " + gameID + ": " + exists);
                    return exists;
                }
                System.out.println("gameExists check for gameID " + gameID + ": no rows returned");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error in gameExists: " + e.getMessage());
            throw new DataAccessException("Failed to check game existence: " + e.getMessage());
        }
    }

    public static void createAuthToken(String authToken, String username) throws DataAccessException {
        initialize();
        System.out.println("Attempting to create authToken for user: " + username);
        String sql = "INSERT INTO authTokens (authToken, username) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected (createAuthToken): " + rowsAffected);
            if (rowsAffected == 0) {
                throw new DataAccessException("No rows inserted for authToken: " + authToken);
            }
        } catch (SQLException e) {
            System.err.println("Error in createAuthToken: " + e.getMessage());
            throw new DataAccessException("Failed to create auth token: " + e.getMessage());
        }
    }

    public static boolean authTokenExists(String authToken) throws DataAccessException {
        initialize();
        System.out.println("Checking if authToken exists: " + authToken);
        String sql = "SELECT COUNT(*) FROM authTokens WHERE authToken = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    System.out.println("authTokenExists check for " + authToken + ": " + exists);
                    return exists;
                }
                System.out.println("authTokenExists check for " + authToken + ": no rows returned");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error in authTokenExists: " + e.getMessage());
            throw new DataAccessException("Failed to check auth token existence: " + e.getMessage());
        }
    }

    public static void updateGame(int gameId, String whiteUsername, String blackUsername, String gameData) throws DataAccessException {
        initialize();
        System.out.println("Attempting to update game: gameID=" + gameId);
        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, game = ? WHERE gameID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, whiteUsername);
            ps.setString(2, blackUsername);
            ps.setString(3, gameData);
            ps.setInt(4, gameId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected (updateGame): " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Error in updateGame: " + e.getMessage());
            throw new DataAccessException("Failed to update game: " + e.getMessage());
        }
    }

    public static String getUserPassword(String username) throws DataAccessException {
        initialize();
        System.out.println("Retrieving password for user: " + username);
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    System.out.println("Password retrieved for " + username + ": " + (password != null));
                    return password;
                }
                System.out.println("No password found for " + username);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error in getUserPassword: " + e.getMessage());
            throw new DataAccessException("Failed to get user password: " + e.getMessage());
        }
    }

    public static String getUsernameFromAuthToken(String authToken) throws DataAccessException {
        initialize();
        System.out.println("Retrieving username for authToken: " + authToken);
        String sql = "SELECT username FROM authTokens WHERE authToken = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    System.out.println("Username retrieved for authToken " + authToken + ": " + username);
                    return username;
                }
                System.out.println("No username found for authToken " + authToken);
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error in getUsernameFromAuthToken: " + e.getMessage());
            throw new DataAccessException("Failed to get username from auth token: " + e.getMessage());
        }
    }

    public static ResultSet getAllGames() throws DataAccessException {
        initialize();
        System.out.println("Retrieving all games");
        String sql = "SELECT * FROM games";
        try {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println("Games retrieved successfully");
            return rs;
        } catch (SQLException e) {
            System.err.println("Error in getAllGames: " + e.getMessage());
            throw new DataAccessException("Failed to get all games: " + e.getMessage());
        }
    }

    public static void deleteAuthToken(String authToken) throws DataAccessException {
        initialize();
        System.out.println("Attempting to delete authToken: " + authToken);
        String sql = "DELETE FROM authTokens WHERE authToken = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            int rowsAffected = ps.executeUpdate();
            System.out.println("Rows affected (deleteAuthToken): " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("Error in deleteAuthToken: " + e.getMessage());
            throw new DataAccessException("Failed to delete auth token: " + e.getMessage());
        }
    }
}