package com.unimelb.swen90007.reactexampleapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unimelb.swen90007.reactexampleapi.model.ClubAdmin;
import com.unimelb.swen90007.reactexampleapi.service.ClubAdminService;
import com.unimelb.swen90007.reactexampleapi.service.JwtTokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "ClubAdminController", urlPatterns = {"/add-admin", "/remove-admin", "/get-admin-clubs", "/get-admins-by-club-id"})
public class ClubAdminController extends HttpServlet {
    private ClubAdminService clubAdminService;

    @Override
    public void init() throws ServletException {
        this.clubAdminService = new ClubAdminService();
    }

    // POST requests
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        try {
            // Get token from request header
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Get the student ID from the token
                String requesterId = JwtTokenUtil.getStudentIdFromToken(token).toString();
                System.out.println("requesterId:" + requesterId);

                // Read the request body as JSON
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> requestBody = objectMapper.readValue(req.getInputStream(), Map.class);

                if ("/add-admin".equals(path)) {
                    // Extract student_email and club_name from the request body
                    String studentEmail = (String) requestBody.get("student_email");
                    String clubName = (String) requestBody.get("club_name");

                    // Add admin by email and club name
                    clubAdminService.addAdminByEmailAndClubName(studentEmail, clubName, token);

                    // Return success response
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Admin added successfully\"}");
                } else if ("/remove-admin".equals(path)) {
                    String studentEmail = (String) requestBody.get("student_email");
                    String clubIdStr = (String) requestBody.get("club_id");  // Get club_id as String
                    int clubId = Integer.parseInt(clubIdStr);  // Convert to Integer

                    // Remove admin by email
                    clubAdminService.removeAdminByEmail(studentEmail, clubId, token);

                    // Return success response
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Admin removed successfully\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
            }
        } catch (IllegalArgumentException | SQLException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Failed to process request. Error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        try {
            // Extract the token from the header
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if ("/get-admin-clubs".equals(path)) {
                    // Call service to get the list of clubs the student is an admin of
                    List<Map<String, Object>> adminClubs = clubAdminService.getClubsByAdmin(token);

                    // Convert the result to JSON and send it back
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResponse = mapper.writeValueAsString(adminClubs);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(jsonResponse);
                }
                else if("/get-admins-by-club-id".equals(path)) {
                    // Fetch the club ID from request parameters
                    int clubId = Integer.parseInt(req.getParameter("club_id"));

                    // Call service to get the list of admin students by club ID
                    List<Map<String, Object>> admins = clubAdminService.getAdminStudentsByClubId(clubId);

                    // Convert the result to JSON and send it back
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonResponse = mapper.writeValueAsString(admins);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(jsonResponse);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to retrieve clubs. Error: " + e.getMessage() + "\"}");
        }
    }
}
