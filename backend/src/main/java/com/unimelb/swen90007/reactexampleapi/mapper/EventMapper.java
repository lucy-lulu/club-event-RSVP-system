package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventMapper {
    private static final String SQL_INSERT_EVENT = "INSERT INTO app.event (event_name, club_id, title, description, cost, time, date, venue_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    // Unit of work in event delete
    private static final String SQL_DELETE_EVENT = "DELETE FROM app.event WHERE event_id = ?";
    private static final String SQL_DELETE_RSVP_TICKET_BY_EVENT = "DELETE FROM app.rsvp_ticket WHERE event_id = ?";
    private static final String SQL_DELETE_STUDENT_RSVP_BY_TICKET = "DELETE FROM app.studentrsvp WHERE ticket_id IN (SELECT ticket_id FROM app.rsvp_ticket WHERE event_id = ?)";

    private static final String SQL_UPDATE_EVENT = "UPDATE app.event SET event_name = ?, title = ?, description = ?, cost = ?, time = ?, date = ?, venue_id = ? WHERE event_id = ?";
    private static final String SQL_GET_ALL_EVENTS = "SELECT * FROM app.event ORDER BY event_id ASC";

    private static final String SQL_GET_EVENTS_BY_CLUB_ID = "SELECT * FROM app.event WHERE club_id = ? ORDER BY event_id ASC";

    private static final String SQL_GET_UPCOMING_EVENTS = "SELECT *  FROM app.event WHERE date > CURRENT_TIMESTAMP LIMIT ? OFFSET ?";
    private static final String SQL_GET_PAST_EVENTS = "SELECT * FROM app.event WHERE date < CURRENT_TIMESTAMP LIMIT ? OFFSET ?";

    // SQL query to check if the student is an admin of the club
    private static final String SQL_CHECK_ADMIN = "SELECT COUNT(*) FROM app.clubadmin WHERE student_id = ? AND club_id = ?";

    // SQl for lazy load of paginated event
    private static final String SQL_GET_EVENTS_PAGINATED = "SELECT * FROM app.event LIMIT ? OFFSET ?";

    // SQl for get club by event
    private static final String SQL_GET_CLUB_ID_BY_EVENT_ID = "SELECT club_id FROM app.event WHERE event_id = ?";

    private static final String SQL_GET_EVENT_BY_ID = "SELECT * FROM app.event WHERE event_id = ?";

    private static final String SQL_GET_PAGINATED_UPCOMING_EVENTS =
            "SELECT * FROM app.event WHERE date > CURRENT_TIMESTAMP LIMIT ? OFFSET ?";

    private static final String SQL_GET_PAGINATED_PAST_EVENTS =
            "SELECT * FROM app.event WHERE date < CURRENT_TIMESTAMP LIMIT ? OFFSET ?";


    // Method to check if a student (using UUID) is an admin of a club
    public boolean isStudentAdminOfClub(UUID studentId, int clubId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_CHECK_ADMIN)) {
            statement.setObject(1, studentId);  // Use setObject for UUID
            statement.setInt(2, clubId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;  // Return true if the count is greater than 0, meaning the student is an admin
            }
            return false;
        }
    }

    // Method to insert a new event into the database
    public void createEvent(Event event, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_EVENT)) {
            statement.setString(1, event.getEventName());
            statement.setInt(2, event.getClubId());
            statement.setString(3, event.getTitle());
            statement.setString(4, event.getDescription());
            statement.setBigDecimal(5, event.getCost());
            statement.setInt(6, event.getTime());
            statement.setTimestamp(7, event.getDate());
            statement.setInt(8, event.getVenueId());
            statement.executeUpdate();
        }
    }

    // Method to delete an event by event_id using Unit of Work
    public void deleteEventById(int eventId, Connection connection) throws SQLException {
        // Remove student RSVP records based on the ticket_id related to the event
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_STUDENT_RSVP_BY_TICKET)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        }

        // Remove RSVP tickets related to the event
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RSVP_TICKET_BY_EVENT)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        }

        // Remove the event itself
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_EVENT)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        }
    }

    // Method to update an event by event_id
    public void updateEvent(Event event, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_EVENT)) {
            statement.setString(1, event.getEventName());
            statement.setString(2, event.getTitle());
            statement.setString(3, event.getDescription());
            statement.setBigDecimal(4, event.getCost());
            statement.setInt(5, event.getTime());
            statement.setTimestamp(6, event.getDate());
            statement.setInt(7, event.getVenueId());
            statement.setInt(8, event.getId());
            statement.executeUpdate();
        }
    }

    // Method to get all events
    public List<Event> getAllEvents(Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_ALL_EVENTS)) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                events.add(mapResultToEvent(results));
            }
        }
        return events;
    }

    // Method to get events by club ID
    public List<Event> getEventsByClubId(int clubId, Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_EVENTS_BY_CLUB_ID)) {
            statement.setInt(1, clubId);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                events.add(mapResultToEvent(results));
            }
        }
        return events;
    }

    // Method to get upcoming events
    public List<Event> getUpcomingEvents(Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_UPCOMING_EVENTS)) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                events.add(mapResultToEvent(results));
            }
        }
        return events;
    }

    // Method to get past events
    public List<Event> getPastEvents(Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_PAST_EVENTS)) {
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                events.add(mapResultToEvent(results));
            }
        }
        return events;
    }

    private Event mapResultToEvent(ResultSet results) throws SQLException {
        return new Event(
                results.getInt("event_id"),
                results.getString("event_name"),
                results.getInt("club_id"),
                results.getString("title"),
                results.getString("description"),
                results.getBigDecimal("cost"),
                results.getTimestamp("date"),
                results.getInt("time"),
                results.getInt("venue_id")
        );
    }

    private Event mapResultToEventLazy(ResultSet results) throws SQLException {
        return new Event(
                results.getInt("event_id"),
                results.getString("event_name"),
                results.getInt("club_id"),
                results.getString("title"),
                results.getTimestamp("date")
        );
    }

    // Method to retrieve club_id by event_id
    public int getClubIdByEventId(int eventId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_CLUB_ID_BY_EVENT_ID)) {
            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("club_id");  // Return the club_id
            } else {
                throw new SQLException("Event not found with event_id: " + eventId);
            }
        }
    }

    // Method to get paginated events
    public List<Event> getEventsPaginated(int page, int pageSize, Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_EVENTS_PAGINATED)) {
            statement.setInt(1, pageSize);
            statement.setInt(2, (page - 1) * pageSize);  // Calculate offset
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                events.add(mapResultToEvent(resultSet));  // Map the result to the Event object
            }
        }
        return events;
    }

    public List<Event> getPaginatedUpcomingEvents(int page, int pageSize, Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_PAGINATED_UPCOMING_EVENTS)) {
            statement.setInt(1, pageSize);
            statement.setInt(2, (page - 1) * pageSize);  // Calculate offset
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                events.add(mapResultToEvent(resultSet));  // Map the result to the Event object
            }
        }
        return events;
    }

    public List<Event> getPaginatedPastEvents(int page, int pageSize, Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_PAGINATED_PAST_EVENTS)) {
            statement.setInt(1, pageSize);
            statement.setInt(2, (page - 1) * pageSize);  // Calculate offset
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                events.add(mapResultToEvent(resultSet));  // Map the result to the Event object
            }
        }
        return events;
    }

    public Event getEventById(int eventId, Connection connection) throws SQLException {
        Event event = null;
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_EVENT_BY_ID)) {
            statement.setInt(1, eventId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    event = mapResultToEvent(rs);
                }
            }
        }
        return event;
    }

    // SQL to get venue_id for a given event
    private static final String SQL_GET_VENUE_ID_FOR_EVENT = "SELECT venue_id FROM app.event WHERE event_id = ?";

    // SQL to get venue capacity from venue table
    private static final String SQL_GET_VENUE_CAPACITY = "SELECT capacity FROM app.venue WHERE venue_id = ?";

    // Method to get venue_id for a given event
    public int getVenueIdForEvent(int eventId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_VENUE_ID_FOR_EVENT)) {
            statement.setInt(1, eventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("venue_id");  // Return the venue_id if found
                } else {
                    throw new SQLException("Venue ID not found for event ID: " + eventId);
                }
            }
        }
    }

    // Method to get venue capacity based on venue_id
    public int getVenueCapacityById(int venueId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_VENUE_CAPACITY)) {
            statement.setInt(1, venueId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("capacity");  // Return the venue capacity
                } else {
                    throw new SQLException("Capacity not found for venue ID: " + venueId);
                }
            }
        }
    }

}
