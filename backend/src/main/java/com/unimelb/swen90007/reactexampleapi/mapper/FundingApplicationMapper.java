package com.unimelb.swen90007.reactexampleapi.mapper;

import com.unimelb.swen90007.reactexampleapi.model.FundingApplication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FundingApplicationMapper {

    private static final String SQL_COUNT_FUNDING_APPLICATIONS = "SELECT COUNT(*) AS total_funding_applications FROM app.fundingapplication";
    private static final String SQL_INSERT_APPLICATION = "INSERT INTO app.fundingapplication (club_id, amount, description, semester, date, status) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_APPLICATION_BY_ID = "SELECT * FROM app.fundingapplication WHERE application_id = ?";
    private static final String SQL_CHECK_APPLICATION_EXISTS = "SELECT 1 FROM app.fundingapplication WHERE club_id = ? AND semester ILIKE ?";
    private static final String SQL_SELECT_FUNDING_BY_CLUB_ID = "SELECT * FROM app.fundingapplication WHERE club_id = ? ORDER BY application_id ASC"; // add ORDER BY application id
    private static final String SQL_DELETE_APPLICATION_BY_ID = "DELETE FROM app.fundingapplication WHERE application_id = ?";
    private static final String SQL_UPDATE_APPLICATION = "UPDATE app.fundingapplication SET amount = ?, description = ?, semester = ?, date = ?, status = ? WHERE application_id = ?";
    // for faculty admin
    private static final String SQL_SELECT_ALL_APPLICATIONS = "SELECT * FROM app.fundingapplication";
    private static final String SQL_UPDATE_APPLICATION_STATUS = "UPDATE app.fundingapplication SET status = ? WHERE application_id = ?";
    // SQL query to update application with optimistic locking
    private static final String SQL_UPDATE_APPLICATION_STATUS_OPTIMISTIC = "UPDATE app.fundingapplication SET status = ?, version = version + 1 WHERE application_id = ? AND version = ?";

    // for faculty admin: Get all funding applications
    public List<FundingApplication> getAllFundingApplications(Connection connection) throws SQLException {
        List<FundingApplication> applications = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL_APPLICATIONS)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                applications.add(mapToFundingApplication(resultSet));
            }
        }
        return applications;
    }

    // for faculty admin: Update the status of a funding application
    public void updateFundingApplicationStatus(int applicationId, String status, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_APPLICATION_STATUS)) {
            statement.setString(1, status);
            statement.setInt(2, applicationId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No funding application found with the given application ID.");
            }
        }
    }

    // Concurrency: Update the status of a funding application with optimistic locking
    public boolean updateFundingApplicationStatusWithVersion(int applicationId, String status, int version, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_APPLICATION_STATUS_OPTIMISTIC)) {
            statement.setString(1, status);
            statement.setInt(2, applicationId);
            statement.setInt(3, version);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0; // If no rows are affected, the version mismatch indicates a conflict
        }
    }

    // SQl for lazy load of paginated funding applications
    private static final String SQL_GET_FUNDING_PAGINATED = "SELECT * FROM app.fundingapplication LIMIT ? OFFSET ?";

    // Method to get total number of funding applications for pagination
    public int getNumberOfFundingApplications(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_COUNT_FUNDING_APPLICATIONS)) {
            ResultSet results = statement.executeQuery();
            if (results.next())
                return results.getInt("total_funding_applications");
        }
        return 0;
    }

    // Insert a new funding application
    public void insertFundingApplication(FundingApplication application, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_APPLICATION)) {
            statement.setInt(1, application.getClubId());
            statement.setDouble(2, application.getAmount());
            statement.setString(3, application.getDescription());
            statement.setString(4, application.getSemester());
            statement.setTimestamp(5, application.getDate());
            statement.setString(6, application.getStatus()); // Use the status from the application
            statement.executeUpdate();
        }
    }

    // Select a funding application by application_id
    public FundingApplication getFundingApplicationById(int applicationId, Connection connection) throws SQLException {
        FundingApplication application = null;
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_APPLICATION_BY_ID)) {
            statement.setInt(1, applicationId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                application = mapToFundingApplication(rs);
            }
        }
        return application;
    }

    // Check if an application exists for the club in the semester
    public boolean applicationExistsForSemester(int clubId, String semester, Connection connection)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_CHECK_APPLICATION_EXISTS)) {
            statement.setInt(1, clubId);
            statement.setString(2, semester);
            ResultSet rs = statement.executeQuery();
            return rs.next(); // If there is a result, the application exists
        }
    }

    // Get all funding applications for a specific club
    public List<FundingApplication> getFundingApplicationsByClubId(int clubId, Connection connection)
            throws SQLException {
        List<FundingApplication> applications = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_SELECT_FUNDING_BY_CLUB_ID)) {
            statement.setInt(1, clubId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                applications.add(mapToFundingApplication(resultSet));
            }
        }
        return applications;
    }

    // Delete a funding application by application_id
    public void deleteFundingApplication(int applicationId, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_APPLICATION_BY_ID)) {
            statement.setInt(1, applicationId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No funding application found with the given application ID.");
            }
        }
    }

    // Map ResultSet to FundingApplication object
    private FundingApplication mapToFundingApplication(ResultSet resultSet) throws SQLException {
        int applicationId = resultSet.getInt("application_id");
        int clubId = resultSet.getInt("club_id");
        double amount = resultSet.getDouble("amount");
        String description = resultSet.getString("description");
        String semester = resultSet.getString("semester");
        Timestamp date = resultSet.getTimestamp("date");
        String status = resultSet.getString("status");
        int version = resultSet.getInt("version");  // Map version from the ResultSet

        // Use the constructor with the version
        return new FundingApplication(applicationId, clubId, amount, description, semester, date, status, version);
    }


    // edit function
    public void updateFundingApplication(FundingApplication application, Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_APPLICATION)) {
            statement.setDouble(1, application.getAmount());
            statement.setString(2, application.getDescription());
            statement.setString(3, application.getSemester());
            statement.setTimestamp(4, application.getDate());
            statement.setString(5, application.getStatus());
            statement.setInt(6, application.getId());
            System.out.println(statement.executeUpdate());

        }
    }

    // Method to get paginated funding applications
    public List<FundingApplication> getFundingApplicationsPaginated(int page, int pageSize, Connection connection)
            throws SQLException {
        List<FundingApplication> fundingApplications = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_GET_FUNDING_PAGINATED)) {
            statement.setInt(1, pageSize);
            statement.setInt(2, (page - 1) * pageSize); // Calculate offset
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fundingApplications.add(mapToFundingApplication(resultSet));
                // Map the result to the Funding Application object
            }
        }
        return fundingApplications;
    }
}
