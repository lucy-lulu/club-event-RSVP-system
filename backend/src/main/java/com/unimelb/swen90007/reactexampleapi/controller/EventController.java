package com.unimelb.swen90007.reactexampleapi.controller;

import com.unimelb.swen90007.reactexampleapi.model.Event;
import com.unimelb.swen90007.reactexampleapi.service.EventService;
import com.unimelb.swen90007.reactexampleapi.service.JwtTokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "EventController", urlPatterns = {"/create-event", "/remove-event", "/edit-event", "/lock-event", "/unlock-event", "/get-paginated-events", "/get-all-events", "/get-events-by-club", "/get-upcoming-events", "/get-past-events", "/get-event-by-id"})
public class EventController extends HttpServlet {
    private EventService eventService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        this.eventService = new EventService();
        this.objectMapper = new ObjectMapper();
    }

    // POST requests for creating, removing, editing, locking, and unlocking an event
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID studentId = JwtTokenUtil.getStudentIdFromToken(token);

            switch (path) {
                case "/remove-event":
                    handleRemoveEvent(req, resp, studentId);
                    break;
                case "/edit-event":
                    handleEditEvent(req, resp, studentId);
                    break;
                case "/create-event":
                    handleCreateEvent(req, resp, studentId);
                    break;
                case "/lock-event":
                    handleLockEvent(req, resp, studentId);
                    break;
                case "/unlock-event":
                    handleUnlockEvent(req, resp);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"Invalid request.\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
        }
    }

    // Handle creating an event
    private void handleCreateEvent(HttpServletRequest req, HttpServletResponse resp, UUID studentId) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());

            int clubId = jsonNode.get("club_id").asInt();
            if (!eventService.isStudentAdminOfClub(studentId, clubId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"Only admin can create events.\"}");
                return;
            }

            String eventName = jsonNode.get("event_name").asText();
            String title = jsonNode.get("title").asText();
            String description = jsonNode.get("description").asText();
            BigDecimal cost = new BigDecimal(jsonNode.get("cost").asText());
            int time = jsonNode.get("time").asInt();
            Timestamp date = Timestamp.valueOf(jsonNode.get("date").asText());
            int venueId = jsonNode.get("venue_id").asInt();

            Event event = new Event(clubId, eventName, title, description, cost, date, time, venueId);
            eventService.createEvent(event);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Event created successfully.\"}");
        } catch (SQLException e) {
            handleSQLException(resp, e);
        }
    }

    // Handle editing an event with locking mechanism
    private void handleEditEvent(HttpServletRequest req, HttpServletResponse resp, UUID studentId) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());

            int eventId = jsonNode.get("event_id").asInt();
            String eventName = jsonNode.get("event_name").asText();
            String title = jsonNode.get("title").asText();
            String description = jsonNode.get("description").asText();
            BigDecimal cost = new BigDecimal(jsonNode.get("cost").asText());
            int time = jsonNode.get("time").asInt();
            Timestamp date = Timestamp.valueOf(jsonNode.get("date").asText());
            int venueId = jsonNode.get("venue_id").asInt();

            Event event = new Event();
            event.setId(eventId);
            event.setEventName(eventName);
            event.setTitle(title);
            event.setDescription(description);
            event.setCost(cost);
            event.setTime(time);
            event.setDate(date);
            event.setVenueId(venueId);

            // Update event with lock control concurrency
            eventService.updateEvent(event, studentId.toString());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Event updated successfully.\"}");
        } catch (SQLException e) {
            if (e.getMessage().contains("locked by another admin")) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("{\"error\": \"The event is currently locked by another admin.\"}");
            } else {
                handleSQLException(resp, e);
            }
        }
    }

    // Handle locking an event for editing
    private void handleLockEvent(HttpServletRequest req, HttpServletResponse resp, UUID studentId) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            int eventId = jsonNode.get("event_id").asInt();
            int clubId = eventService.getClubIdByEventId(eventId);

            if (!eventService.isStudentAdminOfClub(studentId, clubId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"Only admin can lock the event.\"}");
                return;
            }

            boolean locked = eventService.lockEvent(eventId, studentId.toString());
            if (locked) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\": \"Event locked successfully.\"}");
            }
        } catch (SQLException e) {
            // Check if there is a lock conflict
            if (e.getMessage().contains("locked by another user")) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 返回409状态码
                resp.getWriter().write("{\"error\": \"Event is already locked by another user.\"}");
            } else {
                handleSQLException(resp, e); // 处理其他数据库错误
            }
        }
    }

    // Handle unlocking an event
    private void handleUnlockEvent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            int eventId = jsonNode.get("event_id").asInt();

            boolean unlocked = eventService.unlockEvent(eventId);
            if (unlocked) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\": \"Event unlocked successfully.\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Failed to unlock event.\"}");
            }
        } catch (SQLException e) {
            handleSQLException(resp, e);
        }
    }

    // Handle removing an event
    private void handleRemoveEvent(HttpServletRequest req, HttpServletResponse resp, UUID studentId) throws IOException {
        try {
            JsonNode jsonNode = objectMapper.readTree(req.getInputStream());
            int eventId = jsonNode.get("event_id").asInt();
            int clubId = eventService.getClubIdByEventId(eventId);

            if (!eventService.isStudentAdminOfClub(studentId, clubId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"Only admin can remove the event.\"}");
                return;
            }

            eventService.removeEventById(eventId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Event removed successfully.\"}");
        } catch (SQLException e) {
            handleSQLException(resp, e);
        }
    }

    // GET requests for retrieving events
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        ObjectMapper mapper = new ObjectMapper();

        try {
            switch (path) {
                case "/get-all-events":
                    List<Event> events = eventService.getAllEvents();
                    resp.getWriter().write(mapper.writeValueAsString(events));
                    break;
                case "/get-events-by-club":
                    int clubId = Integer.parseInt(req.getParameter("club_id"));
                    events = eventService.getEventsByClubId(clubId);
                    resp.getWriter().write(mapper.writeValueAsString(events));
                    break;
                case "/get-event-by-id":
                    // 处理获取单个事件的逻辑
                    int eventId = Integer.parseInt(req.getParameter("event_id"));
                    Event event = eventService.getEventById(eventId);
                    if (event != null) {
                        resp.getWriter().write(mapper.writeValueAsString(event));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().write("{\"error\": \"Event not found.\"}");
                    }
                    break;
                case "/get-upcoming-events":
                    int page = getRequestParam(req, "page", 1);
                    int pageSize = getRequestParam(req, "pageSize", 10);
                    events = eventService.getPaginatedUpcomingEvents(page, pageSize);
                    resp.getWriter().write(mapper.writeValueAsString(events));
                    break;
                case "/get-past-events":
                    page = getRequestParam(req, "page", 1);
                    pageSize = getRequestParam(req, "pageSize", 10);
                    events = eventService.getPaginatedPastEvents(page, pageSize);
                    resp.getWriter().write(mapper.writeValueAsString(events));
                    break;
                case "/get-paginated-events":
                    page = getRequestParam(req, "page", 1);
                    pageSize = getRequestParam(req, "pageSize", 10);
                    events = eventService.getPaginatedEvents(page, pageSize);
                    resp.getWriter().write(mapper.writeValueAsString(events));
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"Invalid request.\"}");
            }
        } catch (SQLException e) {
            handleSQLException(resp, e);
        }
    }

    private int getRequestParam(HttpServletRequest req, String param, int defaultValue) {
        String value = req.getParameter(param);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private void handleSQLException(HttpServletResponse resp, SQLException e) throws IOException {
        e.printStackTrace();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("{\"error\": \"Database error occurred. Please try again later.\"}");
    }
}
