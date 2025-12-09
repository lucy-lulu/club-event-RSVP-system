package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.ClubMember;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClubMemberMapper implements RowMapper<ClubMember> {
    private static final String SQL_INSERT_MEMBER =
            "INSERT INTO app.clubmember (student_id, club_id) VALUES (?, ?)";

    @Override
    public ClubMember mapRow(ResultSet rs, int rowNum) throws SQLException {
        UUID studentId = (UUID) rs.getObject("student_id");
        int clubId = rs.getInt("club_id");
        return new ClubMember(studentId, clubId);
    }

    // Get all club members by student_id
    public List<ClubMember> getClubMembersByStudentId(UUID studentId, Connection connection) throws SQLException {
        String sql = "SELECT * FROM app.ClubMember WHERE student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, studentId);
            ResultSet rs = stmt.executeQuery();
            List<ClubMember> clubMemberList = new ArrayList<>();
            while (rs.next()) {
                clubMemberList.add(mapRow(rs, rs.getRow()));
            }
            return clubMemberList;
        }
    }
    public void insertMember(UUID studentId, int clubId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MEMBER)) {
            statement.setObject(1, studentId, java.sql.Types.OTHER);
            statement.setInt(2, clubId);
            statement.executeUpdate();
        }
    }
}
