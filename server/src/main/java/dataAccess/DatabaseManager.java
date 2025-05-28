package dataAccess;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseManager {
    private Connection conn;

    public DatabaseManager() throws SQLException {
        try {
            // Explicitly load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IOException("db.properties not found");
                }
                props.load(in);
            }
            String url = props.getProperty("database.url");
            String username = props.getProperty("database.username");
            String password = props.getProperty("database.password");
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(true);
            createTables();
        } catch (IOException | ClassNotFoundException e) {
            throw new SQLException("Unable to initialize database: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Properties props = new Properties();
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                    if (in == null) {
                        throw new IOException("db.properties not found");
                    }
                    props.load(in);
                }
                String url = props.getProperty("database.url");
                String username = props.getProperty("database.username");
                String password = props.getProperty("database.password");
                conn = DriverManager.getConnection(url, username, password);
                conn.setAutoCommit(true);
            }
            return conn;
        } catch (IOException | ClassNotFoundException e) {
            throw new SQLException("Unable to get connection: " + e.getMessage(), e);
        }
    }

    private void createTables() throws SQLException {
        String sql = """
            CREATE DATABASE IF NOT EXISTS chessgame;
            USE chessgame;
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            );
            CREATE TABLE IF NOT EXISTS games (
                gameID INT PRIMARY KEY AUTO_INCREMENT,
                whiteUsername VARCHAR(255),
                blackUsername VARCHAR(255),
                gameName VARCHAR(255) NOT NULL,
                game JSON NOT NULL,
                FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
            );
            CREATE TABLE IF NOT EXISTS authTokens (
                authToken VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            );
        """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void closeConnection() throws SQLException {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new SQLException("Unable to close connection: " + e.getMessage(), e);
        }
    }

    public void loadProperties(Properties props) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    props.getProperty("database.url"),
                    props.getProperty("database.username"),
                    props.getProperty("database.password")
            );
            conn.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Unable to load properties: " + e.getMessage(), e);
        }
    }

    public void loadPropertiesFromResources() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Properties props = new Properties();
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (in == null) {
                    throw new IOException("db.properties not found");
                }
                props.load(in);
            }
            conn = DriverManager.getConnection(
                    props.getProperty("database.url"),
                    props.getProperty("database.username"),
                    props.getProperty("database.password")
            );
            conn.setAutoCommit(true);
        } catch (IOException | ClassNotFoundException e) {
            throw new SQLException("Unable to load properties from resources: " + e.getMessage(), e);
        }
    }
}