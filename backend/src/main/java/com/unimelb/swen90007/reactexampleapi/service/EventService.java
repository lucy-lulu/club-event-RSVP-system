package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.mapper.EventMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.EventLockMapper;
import com.unimelb.swen90007.reactexampleapi.model.Event;
import com.unimelb.swen90007.reactexampleapi.database.DatabaseConnection;
import com.unimelb.swen90007.reactexampleapi.UoW.UnitOfWork;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class EventService {
    private EventMapper eventMapper;
    private EventLockMapper eventLockMapper;

    public EventService() {
        this.eventMapper = new EventMapper();
        this.eventLockMapper = new EventLockMapper();
    }

    // Check if a student is an admin of the club using UUID for studentId
    public boolean isStudentAdminOfClub(UUID studentId, int clubId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.isStudentAdminOfClub(studentId, clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // To create an event
    public void createEvent(Event event) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            eventMapper.createEvent(event, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Remove an event by event_id using Unit of Work pattern
    public void removeEventById(int eventId) throws SQLException {
        UnitOfWork uow = null;
        try {
            Connection connection = DatabaseConnection.getInstance().nextConnection();
            uow = new UnitOfWork(connection);  // Initialize Unit of Work
            uow.beginTransaction();  // Begin transaction

            // Perform the cascading deletes using UoW
            eventMapper.deleteEventById(eventId, uow.getConnection());

            uow.commit();  // Commit transaction if all went well
        } catch (SQLException e) {
            if (uow != null) {
                uow.rollback();  // Rollback transaction on failure
            }
            throw e;
        } finally {
            if (uow != null) {
                uow.close();  // Close and reset auto-commit
            }
        }
    }

    // Update an event with pessimistic lock for concurrency control
    public void updateEvent(Event event, String requesterIdStr) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            connection.setAutoCommit(false); // Begin transaction

            UUID requesterId = UUID.fromString(requesterIdStr);
            Event existingEvent = eventMapper.getEventById(event.getId(), connection);

            if (existingEvent == null) {
                throw new SQLException("Event not found.");
            }

            // Check if it is locked
            UUID lockedBy = eventLockMapper.getLockedBy(existingEvent.getId(), connection);
            if (lockedBy != null && !lockedBy.equals(requesterId)) {
                // If locked by another user, throw an exception
                throw new SQLException("The event is currently locked by another admin.");
            }

            // Lock the event to ensure the current user can edit it
            eventLockMapper.lockEvent(existingEvent.getId(), requesterId, connection);

            // Update the event
            eventMapper.updateEvent(event, connection);

            // Unlock the event
            eventLockMapper.unlockEvent(existingEvent.getId(), connection);

            // Commit the transaction
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback(); // Rollback transaction if an exception occurs
            }
            throw e; // Re-throw the exception
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true); // Restore auto-commit
                DatabaseConnection.getInstance().releaseConnection(connection); // Release the connection
            }
        }
    }

    // Lock an event
    public boolean lockEvent(int eventId, String requesterIdStr) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            UUID requesterId = UUID.fromString(requesterIdStr);

            // Attempt to lock the event
            eventLockMapper.lockEvent(eventId, requesterId, connection);
            return true;
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Unlock an event
    public boolean unlockEvent(int eventId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Attempt to unlock the event
            eventLockMapper.unlockEvent(eventId, connection);
            return true;
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get all events
    public List<Event> getAllEvents() throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getAllEvents(connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get events by club ID
    public List<Event> getEventsByClubId(int clubId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getEventsByClubId(clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get upcoming events
    public List<Event> getUpcomingEvents() throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getUpcomingEvents(connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get club_id by event_id
    public int getClubIdByEventId(int eventId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getClubIdByEventId(eventId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get past events
    public List<Event> getPastEvents() throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getPastEvents(connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get paginated events - lazy load
    public List<Event> getPaginatedEvents(int page, int pageSize) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getEventsPaginated(page, pageSize, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    public List<Event> getPaginatedUpcomingEvents(int page, int pageSize) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getPaginatedUpcomingEvents(page, pageSize, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    public List<Event> getPaginatedPastEvents(int page, int pageSize) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getPaginatedPastEvents(page, pageSize, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Fetch an event by its ID
    public Event getEventById(int eventId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return eventMapper.getEventById(eventId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }
}
