package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.mapper.ClubAdminMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.StudentMapper;
import com.unimelb.swen90007.reactexampleapi.model.ClubAdmin;
import com.unimelb.swen90007.reactexampleapi.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClubAdminService {
    private final ClubAdminMapper clubAdminMapper;
    private final StudentMapper studentMapper;

    public ClubAdminService() {
        this.clubAdminMapper = new ClubAdminMapper();
        this.studentMapper = new StudentMapper();
    }

    // Add a new admin to the club
    public void addAdmin(ClubAdmin admin, String token) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Get the student_id of the requester from the token
            UUID requesterId = JwtTokenUtil.getStudentIdFromToken(token);

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, admin.getClubId(), connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // C
            if (!clubAdminMapper.isMember(admin.getStudentId(), admin.getClubId(), connection)) {
                throw new SQLException("Student is not a member of the club.");
            }

            // Add the new admin
            clubAdminMapper.addAdmin(admin, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Delete an admin from the club
    public void removeAdmin(UUID studentId, int clubId, String token) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Get the student_id of the requester from the token
            UUID requesterId = JwtTokenUtil.getStudentIdFromToken(token);

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, clubId, connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // Check if the student to be removed is a member of the club
            if (!clubAdminMapper.isMember(studentId, clubId, connection)) {
                throw new SQLException("The student to be removed is not a member of the club and cannot be removed as an admin.");
            }

            // Delete the admin
            clubAdminMapper.removeAdmin(studentId, clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    public List<Map<String, Object>> getClubsByAdmin(String token) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            UUID studentId = JwtTokenUtil.getStudentIdFromToken(token);
            return clubAdminMapper.getClubsByAdmin(studentId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Add a new admin to the club using email
    public void addAdminByEmail(String studentEmail, int clubId, String token) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Get the student_id of the requester from the token
            UUID requesterId = JwtTokenUtil.getStudentIdFromToken(token);

            // Get the student_id from email
            UUID studentId = studentMapper.getStudentIdByEmail(studentEmail, connection);
            if (studentId == null) {
                throw new SQLException("Student with email not found.");
            }

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, clubId, connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // Check if the student is a member of the club
            if (!clubAdminMapper.isMember(studentId, clubId, connection)) {
                throw new SQLException("Student is not a member of the club.");
            }

            // Add the new admin
            clubAdminMapper.addAdmin(new ClubAdmin(studentId, clubId), connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Remove an admin by email
    public void removeAdminByEmail(String studentEmail, int clubId, String token) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Get the student_id of the requester from the token
            UUID requesterId = JwtTokenUtil.getStudentIdFromToken(token);

            // Get the student_id from email
            UUID studentId = studentMapper.getStudentIdByEmail(studentEmail, connection);
            if (studentId == null) {
                throw new SQLException("Student with email not found.");
            }

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, clubId, connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // Check if the student is a member of the club
            if (!clubAdminMapper.isMember(studentId, clubId, connection)) {
                throw new SQLException("The student to be removed is not a member of the club and cannot be removed as an admin.");
            }

            // Remove the admin
            clubAdminMapper.removeAdmin(studentId, clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Add a new admin to the club using email and club name
    public void addAdminByEmailAndClubName(String studentEmail, String clubName, String token) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Get the student_id of the requester from the token
            UUID requesterId = JwtTokenUtil.getStudentIdFromToken(token);

            // Get the student_id from email
            UUID studentId = studentMapper.getStudentIdByEmail(studentEmail, connection);
            if (studentId == null) {
                throw new SQLException("Student with email not found.");
            }

            // Get the club_id from the club name
            Integer clubId = clubAdminMapper.getClubIdByName(clubName, connection);
            if (clubId == null) {
                throw new SQLException("Club with name not found.");
            }

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, clubId, connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // Check if the student is a member of the club
            if (!clubAdminMapper.isMember(studentId, clubId, connection)) {
                throw new SQLException("Student is not a member of the club.");
            }

            // Add the new admin
            clubAdminMapper.addAdmin(new ClubAdmin(studentId, clubId), connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    public List<Map<String, Object>> getAdminStudentsByClubId(int clubId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return clubAdminMapper.getAdminStudentsByClubId(clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }
}
