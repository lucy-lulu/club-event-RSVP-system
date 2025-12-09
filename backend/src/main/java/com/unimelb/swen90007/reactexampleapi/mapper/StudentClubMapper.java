package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.StudentClub;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudentClubMapper implements RowMapper<StudentClub> {

    @Override
    public StudentClub mapRow(ResultSet rs, int rowNum) throws SQLException {
        int clubId = rs.getInt("club_id");
        String clubName = rs.getString("club_name");
        return new StudentClub(clubId, clubName);
    }

    // Get all student clubs by student_id
    public List<StudentClub> getStudentClubsByStudentId(UUID studentId, Connection connection) throws SQLException {
        String sql = "SELECT sc.club_id, sc.club_name FROM app.ClubMember cm " +
                "JOIN app.StudentClub sc ON cm.club_id = sc.club_id " +
                "WHERE cm.student_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, studentId);
            ResultSet rs = stmt.executeQuery();
            List<StudentClub> studentClubList = new ArrayList<>();
            while (rs.next()) {
                studentClubList.add(mapRow(rs, rs.getRow()));
            }
            return studentClubList;
        }
    }
}
