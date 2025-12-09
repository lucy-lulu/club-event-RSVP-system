package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.mapper.ClubMemberMapper;
import com.unimelb.swen90007.reactexampleapi.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class ClubMemberService {
    private final ClubMemberMapper clubMemberMapper;

    public ClubMemberService() {
        this.clubMemberMapper = new ClubMemberMapper();
    }

    // Insert a new member to the club
    public void addMemberToClub(String token, int clubId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Get the student_id of the requester from the token
            UUID studentId = JwtTokenUtil.getStudentIdFromToken(token);

            // Update the club member
            clubMemberMapper.insertMember(studentId, clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }
}
