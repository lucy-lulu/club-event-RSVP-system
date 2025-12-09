package com.unimelb.swen90007.reactexampleapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unimelb.swen90007.reactexampleapi.service.ClubMemberService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name = "ClubMemberController", urlPatterns = {"/add-member"})
public class ClubMemberController extends HttpServlet {
    private ClubMemberService clubMemberService;
    private ObjectMapper objectMapper;  // For reading the request body as JSON

    @Override
    public void init() throws ServletException {
        this.clubMemberService = new ClubMemberService();
        this.objectMapper = new ObjectMapper();  // Initialize ObjectMapper
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            // Get the token from the request header
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Read the JSON request body
                Map<String, Object> requestBody = objectMapper.readValue(req.getInputStream(), Map.class);

                // Get the club ID from the JSON body
                int clubId = Integer.parseInt(requestBody.get("club_id").toString());

                // Add the member to the club
                clubMemberService.addMemberToClub(token, clubId);

                // Return success response
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"message\": \"Member added successfully to the club.\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
            }
        } catch (SQLException | NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Failed to add member to the club. Error: " + e.getMessage() + "\"}");
        }
    }
}
