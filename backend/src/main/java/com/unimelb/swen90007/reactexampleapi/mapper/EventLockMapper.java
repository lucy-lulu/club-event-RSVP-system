package com.unimelb.swen90007.reactexampleapi.mapper;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class EventLockMapper {

    private static final String SQL_INSERT_LOCK = "INSERT INTO app.EventLockTable (event_id, lock_time, locked_by) VALUES (?, ?, ?) ON CONFLICT (event_id) DO UPDATE SET locked_by = EXCLUDED.locked_by, lock_time = EXCLUDED.lock_time";
    private static final String SQL_DELETE_LOCK = "DELETE FROM app.EventLockTable WHERE event_id = ?";
    private static final String SQL_SELECT_LOCK = "SELECT * FROM app.EventLockTable WHERE event_id = ? FOR UPDATE";

    // Set lock timeout duration to 2 hours
    private static final Duration LOCK_TIMEOUT = Duration.ofHours(2);

    // Create a pessimistic lock
    public UUID lockEvent(int eventId, UUID lockedBy, Connection connection) throws SQLException {
        String selectSQL = "SELECT * FROM app.EventLockTable WHERE event_id = ?";
        try (PreparedStatement checkStatement = connection.prepareStatement(selectSQL)) {
            checkStatement.setInt(1, eventId);
            ResultSet resultSet = checkStatement.executeQuery();

            if (resultSet.next()) {
                UUID existingLockedBy = UUID.fromString(resultSet.getString("locked_by"));
                Timestamp lockTime = resultSet.getTimestamp("lock_time");

                // Check if the lock has expired
                Instant lockInstant = lockTime.toInstant();
                Instant now = Instant.now();

                if (Duration.between(lockInstant, now).compareTo(LOCK_TIMEOUT) > 0) {
                    // Lock has expired, unlock and allow current user to lock
                    unlockEvent(eventId, connection);
                } else {
                    // Lock has not expired, check if it's locked by the current user
                    if (existingLockedBy.equals(lockedBy)) {
                        // Current user already holds the lock
                        return existingLockedBy;
                    } else {
                        // Event is locked by another user, throw an exception
                        throw new SQLException("Event is already locked by another user.");
                    }
                }
            }

            // No lock record found, or lock expired, re-lock
            try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_LOCK)) {
                statement.setInt(1, eventId);
                statement.setTimestamp(2, Timestamp.from(Instant.now()));
                statement.setObject(3, lockedBy);
                statement.executeUpdate();
            }
            return lockedBy;
        }
    }

    // Get the current lock holder
    public UUID getLockedBy(int eventId, Connection connection) throws SQLException {
        String selectSQL = "SELECT locked_by FROM app.EventLockTable WHERE event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String lockedByStr = resultSet.getString("locked_by");
                return lockedByStr != null ? UUID.fromString(lockedByStr) : null;
            } else {
                return null; // No lock record found
            }
        }
    }

    // Unlock the event
    public void unlockEvent(int eventId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_LOCK)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        }
    }

    // Check if the event is locked and verify if the lock has expired
    public boolean isLocked(int eventId, Connection connection, UUID requesterId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_LOCK)) {
            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                UUID lockedBy = UUID.fromString(resultSet.getString("locked_by"));
                // Check if the current requester holds the lock
                if (lockedBy.equals(requesterId)) {
                    return false; // Current user holds the lock, allow operation
                }

                // Check if the lock has expired
                Timestamp lockTime = resultSet.getTimestamp("lock_time");
                Instant lockInstant = lockTime.toInstant();
                Instant now = Instant.now();

                if (Duration.between(lockInstant, now).compareTo(LOCK_TIMEOUT) > 0) {
                    // Lock has expired, release the lock
                    unlockEvent(eventId, connection);
                    return false;
                }
                return true; // Lock has not expired and is held by another user
            }
            return false; // No lock record found
        }
    }
}
