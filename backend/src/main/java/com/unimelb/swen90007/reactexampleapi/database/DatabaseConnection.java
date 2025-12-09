package com.unimelb.swen90007.reactexampleapi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class DatabaseConnection {
    private static final int MAX_CONNECTIONS = 10;
    private static final Duration ACQUIRE_CONNECTION_TIMEOUT = Duration.ofMillis(100);
    private static final String PROPERTY_JDBC_URI = "jdbc.uri";
    private static final String PROPERTY_JDBC_USERNAME = "jdbc.username";
    private static final String PROPERTY_JDBC_PASSWORD = "jdbc.password";
    private final String url;
    private final String username;
    private final String password;
    private final BlockingDeque<Connection> connectionPool;

    // Singleton instance
    private static DatabaseConnection instance;

    // Private constructor to prevent instantiation from other classes
    private DatabaseConnection() {
        this.url = System.getProperty(PROPERTY_JDBC_URI);
        this.username = System.getProperty(PROPERTY_JDBC_USERNAME);
        this.password = System.getProperty(PROPERTY_JDBC_PASSWORD);
        this.connectionPool = new LinkedBlockingDeque<>();
        init();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                System.getProperty(PROPERTY_JDBC_URI),
                System.getProperty(PROPERTY_JDBC_USERNAME),
                System.getProperty(PROPERTY_JDBC_PASSWORD)
        );
    }

    // Singleton pattern to get a single instance of DatabaseConnectionManager
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void init() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (connectionPool.size() < MAX_CONNECTIONS) {
            connectionPool.offer(connect());
        }
    }

    private Connection connect() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection nextConnection() {
        try {
            return connectionPool.poll(ACQUIRE_CONNECTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void releaseConnection(Connection connection) {
        try {
            connectionPool.offer(connection, ACQUIRE_CONNECTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}