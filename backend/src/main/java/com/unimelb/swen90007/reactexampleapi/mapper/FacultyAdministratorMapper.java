package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.FacultyAdministrator;
import com.unimelb.swen90007.reactexampleapi.service.PasswordHashingService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FacultyAdministratorMapper {

    private static final String SQL_GET_FACULTY_ADMIN_BY_EMAIL = "SELECT * FROM app.facultyadministrator WHERE email = ?";
    private PasswordHashingService passwordHashingService = new PasswordHashingService();

    // Method to verify faculty administrator login using hashed password
    public boolean verifyStaffLogin(String email, String plainPassword, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_FACULTY_ADMIN_BY_EMAIL)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                // 验证密码是否匹配
                return passwordHashingService.matches(plainPassword, storedHashedPassword);
            }

            return false;
        }
    }

    // Method to get FacultyAdministrator by email
    public FacultyAdministrator getFacultyAdministratorByEmail(String email, Connection connection) throws SQLException {
        String sql = "SELECT staff_id, staff_name, email, password FROM app.facultyadministrator WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new FacultyAdministrator(
                        (UUID) rs.getObject("staff_id"), // 获取 staff_id 字段
                        rs.getString("staff_name"),      // 获取 staff_name 字段
                        rs.getString("email"),           // 获取 email 字段
                        rs.getString("password")         // 获取 password 字段（即 hashPassword）
                );
            }
        }
        return null;
    }
}

