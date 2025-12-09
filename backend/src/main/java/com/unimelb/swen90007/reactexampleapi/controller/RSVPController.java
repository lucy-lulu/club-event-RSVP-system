package com.unimelb.swen90007.reactexampleapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unimelb.swen90007.reactexampleapi.model.Event;
import com.unimelb.swen90007.reactexampleapi.model.RSVPTicket;
import com.unimelb.swen90007.reactexampleapi.service.EventService;
import com.unimelb.swen90007.reactexampleapi.service.RSVPService;
import com.unimelb.swen90007.reactexampleapi.service.JwtTokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "RSVPController", urlPatterns = {"/rsvp-event", "/delete-rsvp", "/delete-student-ticket", "/get-application", "/get-ticket"})
public class RSVPController extends HttpServlet {
    private RSVPService rsvpService;
    private ObjectMapper mapper;
    private EventService eventService;

    @Override
    public void init() throws ServletException {
        this.rsvpService = new RSVPService();
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);  // Enable snake_case mapping
        this.eventService = new EventService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        if ("/rsvp-event".equals(path)) {
            handleRSVPEvent(req, resp);
        } else if ("/delete-rsvp".equals(path)) {
            handleDeleteRSVP(req, resp);
        } else if ("/delete-student-ticket".equals(path)) {
            handleDeleteStudentTicket(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        if ("/get-application".equals(path)) {
            handleGetApplication(req, resp);
        } else if ("/get-ticket".equals(path)) {
            handleGetTickets(req, resp);
        }
    }

    // POST request to RSVP for an event using body
    private void handleRSVPEvent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Deserialize the request body into RSVPRequest object
            RSVPRequest rsvpRequest = mapper.readValue(req.getReader(), RSVPRequest.class);

            // Extract applicant_id from the JWT token
            String token = req.getHeader("Authorization").substring(7);
            UUID applicantId = JwtTokenUtil.getStudentIdFromToken(token);

            // Determine if it's a new RSVP or an update to an existing one
            if (rsvpRequest.getTicketId() == 0) {
                // New RSVP: ticketId is 0 or not provided
                rsvpService.rsvpForEvent(rsvpRequest.getAttendeeEmail(), rsvpRequest.getEventId(), applicantId);
            } else {
                // Existing RSVP: ticketId is provided
                // Note: This branch is prepared for future update functionality
                // Currently, the updateRSVP method is not implemented in the service
                rsvpService.updateRSVP(rsvpRequest.getTicketId(), rsvpRequest.getAttendeeEmail(), rsvpRequest.getEventId(), applicantId);
            }

            // Send success response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"RSVP created or updated successfully\"}");
        } catch (SQLException e) {
            // Handle specific error for duplicate RSVP
            if (e.getMessage().contains("The attendee already has a ticket")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"The attendee already has a ticket for this event.\"}");
            } else {
                // Handle other SQL-related errors
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to RSVP for event. Error: " + e.getMessage() + "\"}");
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid input errors
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid input format.\"}");
        }
    }


    // POST request to delete RSVP using body
    private void handleDeleteRSVP(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Deserialize the request body into DeleteRSVPRequest object
            DeleteRSVPRequest deleteRequest = mapper.readValue(req.getReader(), DeleteRSVPRequest.class);

            // Call the service layer to delete the RSVP using eventId, attendeeEmail, and ticketId
            rsvpService.deleteRSVPByTicketIdAndAttendeeEmail(deleteRequest.getTicketId(), deleteRequest.getAttendeeEmail());

            // Send success response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"RSVP deleted successfully\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Failed to delete RSVP. Error: " + e.getMessage() + "\"}");
        }
    }


    // POST request to delete only from studentrsvp table using body instead of parameters
    private void handleDeleteStudentTicket(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Deserialize the request body into DeleteStudentTicketRequest object
            DeleteStudentTicketRequest deleteRequest = mapper.readValue(req.getReader(), DeleteStudentTicketRequest.class);

            // Extract student_id from the JWT token in the Authorization header
            String token = req.getHeader("Authorization").substring(7);
            UUID studentId = JwtTokenUtil.getStudentIdFromToken(token);

            // Call the service layer to delete the ticket
            rsvpService.deleteStudentTicket(studentId, deleteRequest.getTicketId());

            // Send success response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Student RSVP deleted successfully\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Failed to delete student RSVP. Error: " + e.getMessage() + "\"}");
        }
    }

    // GET request for get-application with event details
    private void handleGetApplicationOld(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Extract applicant_id from the JWT token in the Authorization header
            String authHeader = req.getHeader("Authorization");
            String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
            UUID applicantId = JwtTokenUtil.getStudentIdFromToken(token);

            // Fetch RSVP tickets for the applicant
            List<RSVPTicket> rsvpTickets = rsvpService.getRSVPsByApplicantId(applicantId);

            // Create a combined response to include event details for each RSVP ticket
            List<Map<String, Object>> rsvpWithEventDetails = new ArrayList<>();

            for (RSVPTicket ticket : rsvpTickets) {
                // Fetch the event details for each RSVP ticket
                Event event = eventService.getEventById(ticket.getEventId());

                // Combine RSVP and event details into a single map
                Map<String, Object> ticketWithEventDetails = new HashMap<>();
                ticketWithEventDetails.put("rsvp", ticket);
                ticketWithEventDetails.put("event", event);

                // Add the combined result to the response list
                rsvpWithEventDetails.add(ticketWithEventDetails);
            }

            // Convert the combined list to JSON
            String jsonResponse = mapper.writeValueAsString(rsvpWithEventDetails);

            // Send the response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch RSVPs with event details. Error: " + e.getMessage() + "\"}");
        }
    }

    // GET request for get-application with event and attendee details
    private void handleGetApplication(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Extract applicant_id from the JWT token in the Authorization header
            String authHeader = req.getHeader("Authorization");
            String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
            UUID applicantId = JwtTokenUtil.getStudentIdFromToken(token);

            // Fetch RSVP tickets for the applicant with attendee details
            List<Map<String, Object>> rsvpDetailsList = rsvpService.getRSVPsWithAttendeeDetailsByApplicantId(applicantId);

            // Create a combined response to include event details for each RSVP ticket
            List<Map<String, Object>> rsvpWithEventDetails = new ArrayList<>();

            for (Map<String, Object> rsvpDetails : rsvpDetailsList) {
                // Fetch the event details for each RSVP ticket
                Event event = eventService.getEventById((int) rsvpDetails.get("eventId"));

                // Combine RSVP, event, and attendee details into a single map
                Map<String, Object> ticketWithEventDetails = new HashMap<>();
                ticketWithEventDetails.put("rsvp", rsvpDetails);  // Include the RSVP and attendee details
                ticketWithEventDetails.put("event", event);

                // Add the combined result to the response list
                rsvpWithEventDetails.add(ticketWithEventDetails);
            }

            // Convert the combined list to JSON
            String jsonResponse = mapper.writeValueAsString(rsvpWithEventDetails);

            // Send the response
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch RSVPs with event and attendee details. Error: " + e.getMessage() + "\"}");
        }
    }

    // GET request to retrieve tickets with event details by student_id
    private void handleGetTickets(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Extract student_id from the JWT token in the Authorization header
            String authHeader = req.getHeader("Authorization");
            String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
            UUID studentId = JwtTokenUtil.getStudentIdFromToken(token);

            List<Map<String, Object>> ticketsWithEventDetails = rsvpService.getTicketsByStudentId(studentId);

            // Send success response with the ticket and event details
            String jsonResponse = mapper.writeValueAsString(ticketsWithEventDetails);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Failed to get tickets. Error: " + e.getMessage() + "\"}");
        }
    }

    // Internal classes for deserializing request bodies
    private static class RSVPRequest {
        private int eventId;
        private String attendeeEmail;
        private int ticketId;  // Add ticket_id

        // Getters and setters
        public int getEventId() {
            return eventId;
        }

        public void setEventId(int eventId) {
            this.eventId = eventId;
        }

        public String getAttendeeEmail() {
            return attendeeEmail;
        }

        public void setAttendeeEmail(String attendeeEmail) {
            this.attendeeEmail = attendeeEmail;
        }

        public int getTicketId() {
            return ticketId;
        }

        public void setTicketId(int ticketId) {
            this.ticketId = ticketId;
        }
    }

    private static class DeleteRSVPRequest {
        private int eventId;
        private String attendeeEmail;
        private int ticketId;

        // Getters and setters
        public int getEventId() {
            return eventId;
        }

        public void setEventId(int eventId) {
            this.eventId = eventId;
        }

        public String getAttendeeEmail() {
            return attendeeEmail;
        }

        public void setAttendeeEmail(String attendeeEmail) {
            this.attendeeEmail = attendeeEmail;
        }

        public int getTicketId() {
            return ticketId;
        }

        public void setTicketId(int ticketId) {
            this.ticketId = ticketId;
        }
    }

    private static class DeleteStudentTicketRequest {
        private int ticketId;

        public int getTicketId() {
            return ticketId;
        }

        public void setTicketId(int ticketId) {
            this.ticketId = ticketId;
        }
    }
}