//package com.unimelb.swen90007.reactexampleapi;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//import java.io.IOException;
//import java.sql.*;
//
//@WebServlet(name = "test", urlPatterns = "/test")
//public class TestResource extends HttpServlet {
//    // Database connection parameters
//    private static final String PROPERTY_JDBC_URI = "jdbc:postgresql://localhost:5432/tv_addicts_react";
//    private static final String PROPERTY_JDBC_USERNAME = "tv_addicts_react_owner";
//    private static final String PROPERTY_JDBC_PASSWORD = "yy991224";
//    private static final String SQL_GET_TEST = "SELECT * FROM app.\"Students\";";
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setContentType("text/html");
//
//        // Open a connection and fetch the row from the 'student' table
//        try (Connection connection = DriverManager.getConnection(
//                PROPERTY_JDBC_URI, PROPERTY_JDBC_USERNAME, PROPERTY_JDBC_PASSWORD)) {
//
//            try (PreparedStatement statement = connection.prepareStatement(SQL_GET_TEST)) {
//                ResultSet results = statement.executeQuery();
//                if (results.next()) {
//                    // Print the firstName column from the 'student' table
//                    resp.getWriter().println("Student first name: " + results.getString("firstName"));
//                } else {
//                    resp.getWriter().println("No students found.");
//                }
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public void init() throws ServletException {
//        // Load the PostgreSQL driver
//        try {
//            Class.forName("org.postgresql.Driver");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        super.init();
//    }
//}
