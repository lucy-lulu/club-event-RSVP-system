package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.Student;
import com.unimelb.swen90007.reactexampleapi.model.StudentClub;
import com.unimelb.swen90007.reactexampleapi.service.PasswordHashingService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudentMapper {

    private static final String SQL_GET_STUDENT_BY_EMAIL = "SELECT * FROM app.Student WHERE email = ?";
    private PasswordHashingService passwordHashingService = new PasswordHashingService();

    private static final String SQL_GET_STUDENT_ID_BY_EMAIL = "SELECT student_id FROM app.student WHERE email = ?";


    // Method to verify student login using hashed password
    public boolean verifyStudentLogin(String email, String plainPassword, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_STUDENT_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                return passwordHashingService.matches(plainPassword, storedHashedPassword);
            }

            return false;
        }
    }

    // Get student by email
    public List<StudentClub> getStudentClubsByStudentId(UUID studentId, Connection connection) throws SQLException {
        String sql = "SELECT sc.club_id, sc.club_name FROM app.ClubMember cm " +
                "JOIN app.StudentClub sc ON cm.club_id = sc.club_id " +
                "WHERE cm.student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, studentId, java.sql.Types.OTHER);
            ResultSet rs = stmt.executeQuery();
            List<StudentClub> studentClubs = new ArrayList<>();
            while (rs.next()) {
                StudentClub club = new StudentClub(rs.getInt("club_id"), rs.getString("club_name"));
                studentClubs.add(club);
            }
            return studentClubs;
        }
    }

    // Get student by email
    public Student getStudentByEmail(String email, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_STUDENT_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Student(
                        (UUID) rs.getObject("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }

            return null;
        }
    }

    // Method to get student_id by email
    public UUID getStudentIdByEmail(String email, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_STUDENT_ID_BY_EMAIL)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return UUID.fromString(resultSet.getString("student_id"));
                } else {
                    throw new SQLException("Student with email " + email + " not found.");
                }
            }
        }
    }
}
