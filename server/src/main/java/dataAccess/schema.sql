-- Creating the chess database
CREATE DATABASE IF NOT EXISTS chess_db;

USE chess_db;

-- Users table to store user information
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- Games table to store game information, with ChessGame state as JSON
CREATE TABLE IF NOT EXISTS games (
    gameID INT PRIMARY KEY AUTO_INCREMENT,
    whiteUsername VARCHAR(255),
    blackUsername VARCHAR(255),
    gameName VARCHAR(255) NOT NULL,
    game JSON NOT NULL,
    FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
    FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
);

-- AuthTokens table to store authentication tokens
CREATE TABLE IF NOT EXISTS authTokens (
    authToken VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);