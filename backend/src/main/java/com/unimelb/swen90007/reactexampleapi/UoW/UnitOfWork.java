package com.unimelb.swen90007.reactexampleapi.UoW;

import java.sql.Connection;
import java.sql.SQLException;

public class UnitOfWork {
    private Connection connection;

    public UnitOfWork(Connection connection) {
        this.connection = connection;
    }

    // Begin a transaction
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    // Commit the transaction
    public void commit() throws SQLException {
        connection.commit();
    }

    // Rollback the transaction
    public void rollback() throws SQLException {
        connection.rollback();
    }

    // Close the connection and reset auto-commit
    public void close() throws SQLException {
        connection.setAutoCommit(true);
        connection.close();
    }

    // Getter for the connection
    public Connection getConnection() {
        return connection;
    }
}
