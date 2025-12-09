package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.ClubAdmin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClubAdminMapper {
    private static final String SQL_INSERT_ADMIN = "INSERT INTO app.clubadmin (student_id, club_id) VALUES (?, ?)";
    private static final String SQL_DELETE_ADMIN = "DELETE FROM app.clubadmin WHERE student_id = ? AND club_id = ?";
    private static final String SQL_IS_ADMIN = "SELECT 1 FROM app.clubadmin WHERE student_id = ? AND club_id = ?";
    private static final String SQL_IS_MEMBER = "SELECT 1 FROM app.clubmember WHERE student_id = ? AND club_id = ?";
    private static final String SQL_SELECT_ADMINS_BY_STUDENT = "SELECT * FROM app.clubadmin WHERE student_id = ?";
    private static final String SQL_SELECT_CLUBS_BY_ADMIN =
            "SELECT sc.club_id, sc.club_name FROM app.clubadmin ca " +
                    "JOIN app.studentclub sc ON ca.club_id = sc.club_id " +
                    "WHERE ca.student_id = ?";

    private static final String SQL_GET_STUDENT_ID_BY_EMAIL = "SELECT student_id FROM app.student WHERE email = ?";

    private static final String SQL_GET_ADMINS_BY_CLUB_ID =
            "SELECT s.student_id, s.email, s.first_name, s.last_name " +
                    "FROM app.clubadmin ca " +
                    "JOIN app.student s ON ca.student_id = s.student_id " +
                    "WHERE ca.club_id = ?";

    // Add an admin
    public void addAdmin(ClubAdmin admin, Connection connection) throws SQLException {
        // Insert a new admin
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_ADMIN)) {
            statement.setObject(1, admin.getStudentId(), java.sql.Types.OTHER);
            statement.setInt(2, admin.getClubId());
            statement.executeUpdate();
        }
    }

    // Delete an admin
    public void removeAdmin(UUID studentId, int clubId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_ADMIN)) {
            statement.setObject(1, studentId, java.sql.Types.OTHER);
            statement.setInt(2, clubId);
            statement.executeUpdate();
        }
    }

    // Check if a student is a member of the club
    public boolean isMember(UUID studentId, int clubId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_IS_MEMBER)) {
            statement.setObject(1, studentId, java.sql.Types.OTHER);
            statement.setInt(2, clubId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        }
    }

    // Check if a student is an admin of the club
    public boolean isAdmin(UUID studentId, int clubId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_IS_ADMIN)) {
            statement.setObject(1, studentId, java.sql.Types.OTHER);
            statement.setInt(2, clubId);
            ResultSet rs = statement.executeQuery();
            return rs.next();
        }
    }


    // Get ClubAdmin list by studentId
    public List<ClubAdmin> getClubAdminsByStudentId(UUID studentId, Connection connection) throws SQLException {
        List<ClubAdmin> clubAdminList = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ADMINS_BY_STUDENT)) {
            statement.setObject(1, studentId, java.sql.Types.OTHER);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                UUID studentUUID = (UUID) rs.getObject("student_id");
                int clubId = rs.getInt("club_id");
                clubAdminList.add(new ClubAdmin(studentUUID, clubId));
            }
        }
        return clubAdminList;
    }

    public List<Map<String, Object>> getClubsByAdmin(UUID studentId, Connection connection) throws SQLException {
        List<Map<String, Object>> clubs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CLUBS_BY_ADMIN)) {
            statement.setObject(1, studentId, java.sql.Types.OTHER);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String, Object> club = new HashMap<>();
                club.put("club_id", rs.getInt("club_id"));
                club.put("club_name", rs.getString("club_name"));
                clubs.add(club);
            }
        }
        return clubs;
    }

    public UUID getStudentIdByEmail(String email, Connection connection) throws SQLException {
        UUID studentId = null;
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_STUDENT_ID_BY_EMAIL)) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                studentId = (UUID) rs.getObject("student_id");
            }
        }
        return studentId;
    }

    public List<Map<String, Object>> getAdminStudentsByClubId(int clubId, Connection connection) throws SQLException {
        List<Map<String, Object>> admins = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_ADMINS_BY_CLUB_ID)) {
            statement.setInt(1, clubId);
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                Map<String, Object> admin = new HashMap<>();
                admin.put("student_id", rs.getObject("student_id"));
                admin.put("email", rs.getString("email"));
                admin.put("first_name", rs.getString("first_name"));
                admin.put("last_name", rs.getString("last_name"));
                admins.add(admin);
            }
        }
        return admins;
    }

    public Integer getClubIdByName(String clubName, Connection connection) throws SQLException {
        Integer clubId = null;
        String SQL_GET_CLUB_ID_BY_NAME = "SELECT club_id FROM app.studentclub WHERE club_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_CLUB_ID_BY_NAME)) {
            statement.setString(1, clubName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                clubId = resultSet.getInt("club_id");
            }
        }
        return clubId;
    }
}