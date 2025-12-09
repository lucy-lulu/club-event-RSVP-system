package com.unimelb.swen90007.reactexampleapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.unimelb.swen90007.reactexampleapi.model.FundingApplication;
import com.unimelb.swen90007.reactexampleapi.service.FundingApplicationService;
import com.unimelb.swen90007.reactexampleapi.service.JwtTokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "FundingApplicationController", urlPatterns = { "/create-funding", "/get-funding",  "/get-funding-by-id",
        "/get-funding-by-club", "/delete-funding", "/edit-funding", "/admin-get-all", "/admin-approved", "/admin-disapproved", "/get-paginated-funding", "/lock-funding", "/unlock-funding" })
public class FundingApplicationController extends HttpServlet {
    private FundingApplicationService fundingApplicationService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        this.fundingApplicationService = new FundingApplicationService();
        this.objectMapper = new ObjectMapper(); // Initialize Jackson ObjectMapper
    }

    // POST requests
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        if ("/create-funding".equals(path)) {
            try {
                // Get token from request header
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    // Get the student ID from the token
                    String requesterId = JwtTokenUtil.getStudentIdFromToken(token).toString();

                    // Parse JSON body
                    JsonNode jsonNode = objectMapper.readTree(req.getReader());

                    // Get parameters from JSON body
                    int clubId = jsonNode.get("club_id").asInt(); // keep name unchanged
                    double amount = jsonNode.get("amount").asDouble();
                    String description = jsonNode.get("description").asText();
                    String semester = jsonNode.get("semester").asText();

                    // Get the current time as the creation time of the application
                    Timestamp date = Timestamp.from(Instant.now());

                    // Create a FundingApplication object
                    FundingApplication application = new FundingApplication(clubId, amount, description, semester, date,
                            "submitted", 1);

                    // Create the funding application using the service layer, passing the requester
                    // ID
                    fundingApplicationService.createFundingApplication(application, requesterId);

                    // Return a success response
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Funding application created successfully\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
                }
            } catch (SQLException e) {
                // Log database error and return 500 Internal Server Error
                e.printStackTrace(); // can change to logger.error("Database error: ", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Database error occurred. Please try again later.\"}");
            } catch (IOException e) {
                // Log input/output error and return 400 Bad Request
                e.printStackTrace(); // can change to logger.error("IO error: ", e);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request format.\"}");
            } catch (Exception e) {
                // Catch any other unexpected exceptions
                e.printStackTrace(); // can change to logger.error("Unexpected error: ", e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"An unexpected error occurred. Please try again later.\"}");
            }
        } else if ("/edit-funding".equals(path)) {
            try {
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String requesterId = JwtTokenUtil.getStudentIdFromToken(token).toString();

                    JsonNode jsonNode = objectMapper.readTree(req.getReader());
                    int applicationId = jsonNode.get("application_id").asInt();
                    double amount = jsonNode.get("amount").asDouble();
                    String description = jsonNode.get("description").asText();
                    String semester = jsonNode.get("semester").asText();
                    String status = jsonNode.get("status").asText();

                    FundingApplication updatedApplication = new FundingApplication(applicationId, 0, amount, description, semester, Timestamp.from(Instant.now()), status);
                    boolean updated = fundingApplicationService.updateFundingApplication(updatedApplication, requesterId);

                    if (updated) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("{\"message\": \"Funding application updated successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write("{\"error\": \"Requester is not authorized to edit this application.\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("locked by another admin")) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().write("{\"error\": \"The funding application is currently locked by another admin.\"}");
                } else {
                    handleSQLException(resp, e);
                }
            }
        } else if ("/admin-approved".equals(path)) {
            try {
                // Parse JSON body to get the application_id and version
                JsonNode jsonNode = objectMapper.readTree(req.getReader());
                int applicationId = jsonNode.get("application_id").asInt();
                int version = jsonNode.get("version").asInt();

                // Approve the funding application with optimistic lock
                boolean approved = fundingApplicationService.approveFundingApplication(applicationId, version);

                if (approved) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Funding application approved successfully.\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().write("{\"error\": \"Conflict detected. The funding application has been modified by another user.\"}");
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to approve funding application. Error: " + e.getMessage() + "\"}");
            }
        } else if ("/admin-disapproved".equals(path)){
            try {
                // Parse JSON body to get the application_id and version
                JsonNode jsonNode = objectMapper.readTree(req.getReader());
                int applicationId = jsonNode.get("application_id").asInt();
                int version = jsonNode.get("version").asInt();

                // Disapprove the funding application with optimistic lock
                boolean disapproved = fundingApplicationService.disapproveFundingApplication(applicationId, version);

                if (disapproved) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Funding application disapproved successfully.\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.getWriter().write("{\"error\": \"Conflict detected. The funding application has been modified by another user.\"}");
                }
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to disapprove funding application. Error: " + e.getMessage() + "\"}");
            }
        } else if ("/lock-funding".equals(path)){
            try {
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String requesterId = JwtTokenUtil.getStudentIdFromToken(token).toString();

                    JsonNode jsonNode = objectMapper.readTree(req.getReader());
                    int clubId = jsonNode.get("club_id").asInt();
                    int applicationId = jsonNode.get("application_id").asInt();

                    boolean locked = fundingApplicationService.lockFundingApplication(clubId, applicationId, requesterId);
                    if (locked) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("{\"message\": \"Funding application locked successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        resp.getWriter().write("{\"error\": \"Funding application is already locked by another user.\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
                }
            } catch (SQLException e) {
                handleSQLException(resp, e);
            }
        } else if ("/unlock-funding".equals(path)){
            try {
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    JsonNode jsonNode = objectMapper.readTree(req.getReader());
                    int clubId = jsonNode.get("club_id").asInt();
                    int applicationId = jsonNode.get("application_id").asInt();

                    boolean unlocked = fundingApplicationService.unlockFundingApplication(clubId, applicationId);
                    if (unlocked) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("{\"message\": \"Funding application unlocked successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("{\"error\": \"Failed to unlock funding application. Invalid club_id or application_id.\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
                }
            } catch (SQLException e) {
                handleSQLException(resp, e);
            }
        }
    }

    // handle GET request
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        if ("/get-funding".equals(path)) {
            try {
                // get JSON from request body (instead of params)
                JsonNode jsonNode = objectMapper.readTree(req.getReader());
                int applicationId = jsonNode.get("application_id").asInt(); // keep name unchanged

                FundingApplication application = fundingApplicationService.getFundingApplicationById(applicationId);

                if (application != null) {
                    String jsonResponse = objectMapper.writeValueAsString(application);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(jsonResponse);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Funding application not found\"}");
                }
            } catch (NumberFormatException | SQLException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter()
                        .write("{\"error\": \"Failed to get funding application. Error: " + e.getMessage() + "\"}");
            }
        } else if ("/get-funding-by-club".equals(path)) {
            try {
                String clubIdParam = req.getParameter("club_id");
                int clubId = Integer.parseInt(clubIdParam);

                List<FundingApplication> applications = fundingApplicationService.getFundingApplicationsByClubId(clubId);

                if (applications.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("[]");
                } else {
                    String jsonResponse = objectMapper.writeValueAsString(applications);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(jsonResponse);
                }
            } catch (SQLException e) {
                handleSQLException(resp, e);
            }
        } else if ("/get-paginated-funding".equals(path)) {
            try {
                // Set default for page and Size
                int page = 1;
                int pageSize = 10;

                // Fetch page and pageSize parameters from the request if present
                if (req.getParameter("page") != null) {
                    page = Integer.parseInt(req.getParameter("page"));
                }
                if (req.getParameter("pageSize") != null) {
                    pageSize = Integer.parseInt(req.getParameter("pageSize"));
                }
                // Fetch paginated funding applications
                Map<String, Object> fundingResponse = new HashMap<>();
                fundingResponse.put("applications",
                        fundingApplicationService.getPaginatedFundingApplications(page, pageSize));
                fundingResponse.put("totalCount", fundingApplicationService.getNumberOfFundingApplications());

                String jsonResponse = objectMapper.writeValueAsString(fundingResponse);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(jsonResponse);
            } catch (NumberFormatException | SQLException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter()
                        .write("{\"error\": \"Failed to get funding applications. Error: " + e.getMessage() + "\"}");
            }
        }else if ("/admin-get-all".equals(path)) {
            try {
                List<FundingApplication> applications = fundingApplicationService.getAllFundingApplications();
                String jsonResponse = objectMapper.writeValueAsString(applications);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(jsonResponse);
            } catch (SQLException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to retrieve funding applications. Error: " + e.getMessage() + "\"}");
            }
        } else if ("/get-funding-by-id".equals(path)){
            try {
                int applicationId = Integer.parseInt(req.getParameter("application_id"));

                FundingApplication application = fundingApplicationService.getFundingApplicationById(applicationId);

                if (application != null) {
                    String jsonResponse = objectMapper.writeValueAsString(application);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write(jsonResponse);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Funding application not found\"}");
                }
            } catch (SQLException e) {
                handleSQLException(resp, e);
            }
        }
    }

    // handle DELETE request
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json");

        if ("/delete-funding".equals(path)) {
            try {
                String authHeader = req.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    String requesterId = JwtTokenUtil.getStudentIdFromToken(token).toString();

                    // get JSON from request body
                    JsonNode jsonNode = objectMapper.readTree(req.getReader());
                    int applicationId = jsonNode.get("application_id").asInt(); // keep name unchanged

                    boolean deleted = fundingApplicationService.deleteFundingApplication(applicationId, requesterId);

                    if (deleted) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        resp.getWriter().write("{\"message\": \"Funding application deleted successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter()
                                .write("{\"error\": \"Requester is not authorized to delete this application.\"}");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Authorization token missing or invalid.\"}");
                }
            } catch (NumberFormatException | SQLException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter()
                        .write("{\"error\": \"Failed to delete funding application. Error: " + e.getMessage() + "\"}");
            }
        }
    }

    private void handleSQLException(HttpServletResponse resp, SQLException e) throws IOException {
        e.printStackTrace();
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().write("{\"error\": \"Database error occurred. Please try again later.\"}");
    }
}
