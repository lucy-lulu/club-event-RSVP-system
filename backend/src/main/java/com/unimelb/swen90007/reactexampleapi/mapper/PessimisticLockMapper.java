package com.unimelb.swen90007.reactexampleapi.mapper;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class PessimisticLockMapper {

    private static final String SQL_INSERT_LOCK = "INSERT INTO app.LockTable (club_id, application_id, lock_time, locked_by) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_LOCK = "DELETE FROM app.LockTable WHERE club_id = ? AND application_id = ?";
    private static final String SQL_SELECT_LOCK = "SELECT * FROM app.LockTable WHERE club_id = ? AND application_id = ? FOR UPDATE";

    // Set the lock timeout to 2 hours
    private static final Duration LOCK_TIMEOUT = Duration.ofHours(2);

    // Create a pessimistic lock
    // lockApplication method in PessimisticLockMapper.java
    public UUID lockApplication(int clubId, int applicationId, UUID lockedBy, Connection connection) throws SQLException {
        String selectSQL = "SELECT * FROM app.LockTable WHERE club_id = ? AND application_id = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(selectSQL)) {
            checkStatement.setInt(1, clubId);
            checkStatement.setInt(2, applicationId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                UUID existingLockedBy = UUID.fromString(resultSet.getString("locked_by"));
                if (existingLockedBy.equals(lockedBy)) {
                    // The current user already holds the lock for this application
                    return existingLockedBy;
                } else {
                    throw new SQLException("A lock already exists for this application.");
                }
            } else {
                // No existing lock, insert a new lock record
                try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_LOCK)) {
                    statement.setInt(1, clubId);
                    statement.setInt(2, applicationId);
                    statement.setTimestamp(3, Timestamp.from(Instant.now()));
                    statement.setObject(4, lockedBy);
                    statement.executeUpdate();
                }
                return lockedBy;
            }
        }
    }

    // Retrieve the current lock holder
    public UUID getLockedBy(int clubId, int applicationId, Connection connection) throws SQLException {
        String selectSQL = "SELECT locked_by FROM app.LockTable WHERE club_id = ? AND application_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setInt(1, clubId);
            statement.setInt(2, applicationId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String lockedByStr = resultSet.getString("locked_by");
                return lockedByStr != null ? UUID.fromString(lockedByStr) : null;
            } else {
                return null; // Return null if no lock record is found
            }
        }
    }

    // Unlock the application
    public void unlockApplication(int clubId, int applicationId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_LOCK)) {
            statement.setInt(1, clubId);
            statement.setInt(2, applicationId);
            statement.executeUpdate();
        }
    }

    // Check if the application is locked and verify if the lock has expired
    public boolean isLocked(int clubId, int applicationId, Connection connection, UUID requesterId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_LOCK)) {
            statement.setInt(1, clubId);
            statement.setInt(2, applicationId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                UUID lockedBy = UUID.fromString(resultSet.getString("locked_by"));
                // Check if the lock is held by the current requester
                if (lockedBy.equals(requesterId)) {
                    return false; // The current user holds the lock, allow operation
                }

                // Lock time check logic
                Timestamp lockTime = resultSet.getTimestamp("lock_time");
                Instant lockInstant = lockTime.toInstant();
                Instant now = Instant.now();

                if (Duration.between(lockInstant, now).compareTo(LOCK_TIMEOUT) > 0) {
                    // Lock has expired, release the lock
                    unlockApplication(clubId, applicationId, connection);
                    return false;
                }
                return true; // Lock has not expired and is held by another user
            }
            return false; // No lock record found
        }
    }
}
