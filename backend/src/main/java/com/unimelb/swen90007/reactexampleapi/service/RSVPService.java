package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.UoW.UnitOfWork;
import com.unimelb.swen90007.reactexampleapi.database.DatabaseConnection;
import com.unimelb.swen90007.reactexampleapi.mapper.RSVPMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.StudentMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.EventMapper;
import com.unimelb.swen90007.reactexampleapi.model.RSVPTicket;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RSVPService {
    private RSVPMapper rsvpMapper;
    private StudentMapper studentMapper;
    private EventMapper eventMapper;


    public RSVPService() {
        this.rsvpMapper = new RSVPMapper();
        this.studentMapper = new StudentMapper();
        this.eventMapper = new EventMapper();
    }

//    public void rsvpForEvent(String attendeeEmail, int eventId, UUID applicantId) throws SQLException {
//        UnitOfWork uow = null;
//        Connection connection = null;
//
//        try {
//            connection = DatabaseConnection.getInstance().nextConnection();
//            uow = new UnitOfWork(connection);
//            uow.beginTransaction();
//
//            // Get the attendee's student_id by email
//            UUID attendeeId = studentMapper.getStudentIdByEmail(attendeeEmail, connection);
//
//            // Check if the attendee already has a ticket for the event
//            if (rsvpMapper.doesAttendeeHaveTicket(attendeeId, eventId, connection)) {
//                throw new SQLException("The attendee already has a ticket for this event.");
//            }
//
//            // both operations (RSVP Ticket + Student RSVP) within the same transaction
//            rsvpMapper.insertRSVPTicket(attendeeId, eventId, applicantId, uow.getConnection());
//
//            uow.commit();  // Commit if everything goes well
//        } catch (SQLException e) {
//            if (uow != null) {
//                uow.rollback();  // Rollback the transaction in case of an error
//            }
//            throw e;  // Rethrow the exception
//        } finally {
//            if (uow != null) {
//                uow.close();  // Ensure the connection is closed and auto-commit is reset
//            }
//        }
//    }

    //try to use pessimistic lock
    private static final int MAX_RETRIES = 3;

    public void rsvpForEvent(String attendeeEmail, int eventId, UUID applicantId) throws SQLException {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            Connection connection = null;
            try {
                connection = DatabaseConnection.getInstance().nextConnection();
                connection.setAutoCommit(false);

                UUID attendeeId = studentMapper.getStudentIdByEmail(attendeeEmail, connection);

                if (rsvpMapper.doesAttendeeHaveTicketWithLock(attendeeId, eventId, connection)) {
                    connection.rollback();
                    throw new SQLException("The attendee already has a ticket for this event.");
                }
                // Check if venue has capacity for new RSVP
                checkVenueCapacity(eventId, connection);
                rsvpMapper.insertRSVPTicket(attendeeId, eventId, applicantId, connection);

                connection.commit();
                return;
            } catch (SQLException e) {
                if (connection != null) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackException) {
                        // 记录回滚异常
                    }
                }
                retries++;
                if (retries == MAX_RETRIES) {
                    throw e; // 重试次数用完，抛出异常
                }
                // 可以在这里添加一些日志记录
            } finally {
                if (connection != null) {
                    try {
                        connection.setAutoCommit(true);
                        connection.close();
                    } catch (SQLException e) {
                        // 记录关闭连接异常
                    }
                }
            }
        }
    }

    // delete RSVP by ticket ID and attendee email using Unit of Work
    public void deleteRSVPByTicketIdAndAttendeeEmail(int ticketId, String attendeeEmail) throws SQLException {
        UnitOfWork uow = null;
        try {
            Connection connection = DatabaseConnection.getInstance().nextConnection();
            uow = new UnitOfWork(connection);
            uow.beginTransaction();

            // Lookup attendee_id using the attendee's email
            UUID attendeeId = studentMapper.getStudentIdByEmail(attendeeEmail, connection);
            System.out.println("service:"+attendeeEmail+attendeeId+ticketId);

            // Perform the deletion of RSVP and StudentRSVP
            rsvpMapper.deleteRSVPByTicketIdAndAttendeeId(attendeeId, ticketId, uow.getConnection());
            uow.commit();  // Commit the transaction if everything goes well
        } catch (SQLException e) {
            if (uow != null) {
                uow.rollback();  // Rollback transaction on failure
            }
            throw e;
        } finally {
            if (uow != null) {
                uow.close();  // Ensure the connection is closed and auto-commit is reset
            }
        }
    }

    // not using this function anymore,use delete by email; can be easily change back to this function anytime
    // delete RSVP by ticket ID and attendee ID using Unit of Work
    public void deleteRSVPByTicketIdAndAttendeeId(int ticketId, UUID attendeeId) throws SQLException {
        UnitOfWork uow = null;
        try {
            Connection connection = DatabaseConnection.getInstance().nextConnection();
            uow = new UnitOfWork(connection);  // Initialize Unit of Work
            uow.beginTransaction();  // Begin transaction

            // Perform the deletion of RSVP and StudentRSVP
            rsvpMapper.deleteRSVPByTicketIdAndAttendeeId( attendeeId,ticketId, uow.getConnection());

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

    // Method to delete only from studentrsvp
    public void deleteStudentTicket(UUID studentId, int ticketId) throws SQLException {
        Connection connection = null;
        UnitOfWork uow = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            uow = new UnitOfWork(connection);
            uow.beginTransaction();

            // Call the mapper to delete only from studentrsvp
            rsvpMapper.deleteStudentRSVP(studentId, ticketId, uow.getConnection());

            uow.commit();
        } catch (SQLException e) {
            if (uow != null) uow.rollback();
            throw e;
        } finally {
            if (uow != null) uow.close();
        }
    }

    // Get RSVPs by applicant_id
    public List<RSVPTicket> getRSVPsByApplicantId(UUID applicantId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().nextConnection()) {
            return rsvpMapper.getRSVPsByApplicantId(applicantId, connection);
        }
    }

//    // Get ticket IDs by student_id
//    public List<Integer> getTicketsByStudentId(UUID studentId) throws SQLException {
//        try (Connection connection = DatabaseConnection.getInstance().nextConnection()) {
//            return rsvpMapper.getTicketsByStudentId(studentId, connection);
//        }
//    }

    public List<Map<String, Object>> getTicketsByStudentId(UUID studentId) throws SQLException {
        Connection connection = null;
        UnitOfWork uow = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            uow = new UnitOfWork(connection);
            uow.beginTransaction();

            // Call mapper to get the tickets and corresponding event details
            List<Map<String, Object>> ticketsWithEventDetails = rsvpMapper.getTicketsByStudentId(studentId, uow.getConnection());

            uow.commit();
            return ticketsWithEventDetails;
        } catch (SQLException e) {
            if (uow != null) uow.rollback();
            throw e;
        } finally {
            if (uow != null) uow.close();
        }
    }

    // Get RSVPs with attendee details by applicant_id
    public List<Map<String, Object>> getRSVPsWithAttendeeDetailsByApplicantId(UUID applicantId) throws SQLException {
        try (Connection connection = DatabaseConnection.getInstance().nextConnection()) {
            return rsvpMapper.getRSVPsWithAttendeeDetailsByApplicantId(applicantId, connection);
        }
    }

    // Add additional method if ticketId is needed in future logic
    public void updateRSVP(int ticketId, String attendeeEmail, int eventId, UUID applicantId) throws SQLException {
        // Logic to update existing RSVP based on ticketId, or any additional logic you want to handle.
    }

    private void checkVenueCapacity(int eventId, Connection connection) throws SQLException {
        // Fetch venue_id for the event
        int venueId = eventMapper.getVenueIdForEvent(eventId, connection);
        // Fetch venue capacity
        int venueCapacity = eventMapper.getVenueCapacityById(venueId, connection);
        // Fetch current RSVP count for the event
        int currentRSVPCount = rsvpMapper.getRSVPCountForEvent(eventId, connection);

        // Check if venue has reached its capacity
        if (currentRSVPCount >= venueCapacity) {
            throw new SQLException("The venue capacity for this event has been reached.");
        }
    }

}