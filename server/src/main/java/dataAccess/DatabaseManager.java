package dataAccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseManager {
    private static final String DATABASE_URL;
    private static final String DATABASE_USERNAME;
    private static final String DATABASE_PASSWORD;

    static {
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            DATABASE_URL = prop.getProperty("database.url");
            DATABASE_USERNAME = prop.getProperty("database.username");
            DATABASE_PASSWORD = prop.getProperty("database.password");
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
        createDatabase();
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD);
        } catch (SQLException e) {
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
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
            )
            """
        };
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD)) {
            for (String stmt : createStatements) {
                try (var ps = conn.prepareStatement(stmt)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database tables: " + e.getMessage());
        }
    }
}