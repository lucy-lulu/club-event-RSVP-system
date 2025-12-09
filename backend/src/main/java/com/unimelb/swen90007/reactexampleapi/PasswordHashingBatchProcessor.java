package com.unimelb.swen90007.reactexampleapi;

import com.unimelb.swen90007.reactexampleapi.service.PasswordHashingService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PasswordHashingBatchProcessor {
//    private static final String PROPERTY_JDBC_URI = "jdbc.uri";
//    private static final String PROPERTY_JDBC_USERNAME = "jdbc.username";
//    private static final String PROPERTY_JDBC_PASSWORD = "jdbc.password";

    public static void main(String[] args) {
        // Create DataSource and initialize JdbcTemplate
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl("jdbc:postgresql://localhost:5432/tv_addicts_react");
        dataSource.setUsername("tv_addicts_react_owner");
        dataSource.setPassword("yy991224");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        PasswordHashingService passwordHashingService = new PasswordHashingService();

        // Query all users
        String selectSql = "SELECT student_id, password FROM app.Student";
        List<Map<String, Object>> users = jdbcTemplate.queryForList(selectSql);

        // Loop through users and update passwords
        for (Map<String, Object> user : users) {
            UUID studentId = (UUID) user.get("student_id");
            String plainPassword = (String) user.get("password");

            // Hash the plain password
            String hashedPassword = passwordHashingService.hashPassword(plainPassword);

            // Update the password in the database
            String updateSql = "UPDATE app.Student SET password = ? WHERE student_id = ?";
            jdbcTemplate.update(updateSql, hashedPassword, studentId);

            System.out.println("Updated password for student: " + studentId);
        }
    }
}
