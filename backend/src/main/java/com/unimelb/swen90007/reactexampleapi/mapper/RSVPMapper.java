package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.RSVPTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RSVPMapper {

    private static final String SQL_INSERT_RSVP_TICKET = "INSERT INTO app.rsvp_ticket (attendee_id, event_id, applicant_id) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_STUDENT_RSVP = "INSERT INTO app.studentrsvp (student_id, ticket_id) VALUES (?, ?)";

    private static final String SQL_DELETE_RSVP_TICKET = "DELETE FROM app.rsvp_ticket WHERE ticket_id = ? AND attendee_id = ?";
    private static final String SQL_DELETE_STUDENT_RSVP = "DELETE FROM app.studentrsvp WHERE student_id = ? AND ticket_id = ?";

    private static final String SQL_SELECT_RSVP_BY_APPLICANT_ID = "SELECT * FROM app.rsvp_ticket WHERE applicant_id = ?";
    private static final String SQL_SELECT_TICKETS_BY_STUDENT_ID = "SELECT ticket_id FROM app.studentrsvp WHERE student_id = ?";
    private static final String SQL_CHECK_TICKET_EXISTS = "SELECT COUNT(*) FROM app.rsvp_ticket WHERE attendee_id = ? AND event_id = ?";

    private static final String SQL_SELECT_RSVP_WITH_STUDENT_BY_APPLICANT_ID =
            "SELECT rsvp.ticket_id, rsvp.attendee_id, rsvp.event_id, rsvp.applicant_id, " +
                    "student.first_name, student.last_name, student.email " +
                    "FROM app.rsvp_ticket rsvp " +
                    "JOIN app.student student ON rsvp.attendee_id = student.student_id " +
                    "WHERE rsvp.applicant_id = ?";


    // Method to insert RSVP ticket and student RSVP within a Unit of Work
    // make sure the method use same link
    public void insertRSVPTicket(UUID attendeeId, int eventId, UUID applicantId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_RSVP_TICKET, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, attendeeId);
            statement.setInt(2, eventId);
            statement.setObject(3, applicantId);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int ticketId = generatedKeys.getInt(1);
                    insertStudentRSVP(attendeeId, ticketId, connection);
                }
            }
        }
    }

    // Method to insert into student RSVP table
    private void insertStudentRSVP(UUID studentId, int ticketId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_STUDENT_RSVP)) {
            statement.setObject(1, studentId);
            statement.setInt(2, ticketId);
            statement.executeUpdate();
        }
    }



    public void deleteRSVPByTicketIdAndAttendeeId(UUID attendeeId, int ticketId,  Connection connection) throws SQLException {
        // First, delete from studentrsvp
        try (PreparedStatement stmtStudentRSVP = connection.prepareStatement(SQL_DELETE_STUDENT_RSVP)) {
            stmtStudentRSVP.setObject(1, attendeeId, java.sql.Types.OTHER);  // UUID needs to be set as Object with type OTHER
            stmtStudentRSVP.setInt(2, ticketId);
            stmtStudentRSVP.executeUpdate();
        }

        // Then, delete from rsvp_ticket
        try (PreparedStatement stmtRSVPTicket = connection.prepareStatement(SQL_DELETE_RSVP_TICKET)) {
            stmtRSVPTicket.setInt(1, ticketId);
            stmtRSVPTicket.setObject(2, attendeeId, java.sql.Types.OTHER);  // UUID needs to be set as Object with type OTHER
            stmtRSVPTicket.executeUpdate();
        }
    }

    // Method to delete only from studentrsvp by student_id and ticket_id
    public void deleteStudentRSVP(UUID studentId, int ticketId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_STUDENT_RSVP)) {
            statement.setObject(1, studentId);
            statement.setInt(2, ticketId);
            statement.executeUpdate();
        }
    }

    // Method to fetch RSVPs by applicant_id
    public List<RSVPTicket> getRSVPsByApplicantId(UUID applicantId, Connection connection) throws SQLException {
        List<RSVPTicket> rsvpTickets = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_RSVP_BY_APPLICANT_ID)) {
            statement.setObject(1, applicantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    RSVPTicket ticket = new RSVPTicket(
                            resultSet.getInt("ticket_id"),
                            UUID.fromString(resultSet.getString("attendee_id")),
                            resultSet.getInt("event_id"),
                            UUID.fromString(resultSet.getString("applicant_id"))
                    );
                    rsvpTickets.add(ticket);
                }
            }
        }
        return rsvpTickets;
    }

//    // Method to fetch tickets by student_id
//    public List<Integer> getTicketsByStudentId(UUID studentId, Connection connection) throws SQLException {
//        List<Integer> ticketIds = new ArrayList<>();
//        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_TICKETS_BY_STUDENT_ID)) {
//            statement.setObject(1, studentId);
//            try (ResultSet resultSet = statement.executeQuery()) {
//                while (resultSet.next()) {
//                    ticketIds.add(resultSet.getInt("ticket_id"));
//                }
//            }
//        }
//        return ticketIds;
//    }

    public List<Map<String, Object>> getTicketsByStudentId(UUID studentId, Connection connection) throws SQLException {
        String SQL_GET_TICKETS_BY_STUDENT_ID =
                "SELECT rt.ticket_id, rt.event_id, e.title, e.description, e.date, e.time, e.venue_id " +
                        "FROM app.studentrsvp sr " +
                        "JOIN app.rsvp_ticket rt ON sr.ticket_id = rt.ticket_id " +
                        "JOIN app.event e ON rt.event_id = e.event_id " +
                        "WHERE sr.student_id = ?";

        List<Map<String, Object>> ticketsWithEventDetails = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_TICKETS_BY_STUDENT_ID)) {
            statement.setObject(1, studentId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Map<String, Object> ticketDetails = new HashMap<>();
                ticketDetails.put("ticket_id", resultSet.getInt("ticket_id"));
                ticketDetails.put("event_id", resultSet.getInt("event_id"));
                ticketDetails.put("event_title", resultSet.getString("title"));
                ticketDetails.put("event_description", resultSet.getString("description"));
                ticketDetails.put("event_date", resultSet.getTimestamp("date"));
                ticketDetails.put("event_time", resultSet.getInt("time"));
                ticketDetails.put("venue_id", resultSet.getInt("venue_id"));

                ticketsWithEventDetails.add(ticketDetails);
            }
        }
        return ticketsWithEventDetails;
    }

    //  check if an attendee already has a ticket for the event
    public boolean doesAttendeeHaveTicket(UUID attendeeId, int eventId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_CHECK_TICKET_EXISTS)) {
            statement.setObject(1, attendeeId, java.sql.Types.OTHER); // Set attendee UUID
            statement.setInt(2, eventId); // Set event ID
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;  // If the count is greater than 0, the attendee already has a ticket
                }
            }
        }
        return false;
    }

    //Uses pessimistic locking and retry mechanisms to handle concurrency.
    // When checking for duplicate RSVPs, the “FOR UPDATE” clause is used to lock the row in question,
    // preventing other transactions from inserting duplicate records in the meantime
    private static final String SQL_CHECK_TICKET_EXISTS_WITH_LOCK =
            "SELECT ticket_id FROM app.rsvp_ticket WHERE attendee_id = ? AND event_id = ? FOR UPDATE";

    public boolean doesAttendeeHaveTicketWithLock(UUID attendeeId, int eventId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_CHECK_TICKET_EXISTS_WITH_LOCK)) {
            statement.setObject(1, attendeeId, java.sql.Types.OTHER);
            statement.setInt(2, eventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // If there's a result, the ticket exists
            }
        }
    }

    // Method to fetch RSVPs and attendee details by applicant_id
    public List<Map<String, Object>> getRSVPsWithAttendeeDetailsByApplicantId(UUID applicantId, Connection connection) throws SQLException {
        List<Map<String, Object>> rsvpDetailsList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_RSVP_WITH_STUDENT_BY_APPLICANT_ID)) {
            statement.setObject(1, applicantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> rsvpDetails = new HashMap<>();
                    rsvpDetails.put("ticketId", resultSet.getInt("ticket_id"));
                    rsvpDetails.put("attendeeId", resultSet.getObject("attendee_id"));
                    rsvpDetails.put("eventId", resultSet.getInt("event_id"));
                    rsvpDetails.put("applicantId", resultSet.getObject("applicant_id"));

                    // Add attendee details
                    rsvpDetails.put("firstName", resultSet.getString("first_name"));
                    rsvpDetails.put("lastName", resultSet.getString("last_name"));
                    rsvpDetails.put("email", resultSet.getString("email"));

                    rsvpDetailsList.add(rsvpDetails);
                }
            }
        }
        return rsvpDetailsList;
    }

    // Method to get the number of RSVPs for an event
    public int getRSVPCountForEvent(int eventId, Connection connection) throws SQLException {
        String SQL_COUNT_RSVPS_FOR_EVENT = "SELECT COUNT(*) FROM app.rsvp_ticket WHERE event_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(SQL_COUNT_RSVPS_FOR_EVENT)) {
            statement.setInt(1, eventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);  // Return the count of RSVPs
                }
            }
        }
        return 0;  // Return 0 if no RSVPs are found
    }

}