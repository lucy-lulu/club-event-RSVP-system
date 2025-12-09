package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.mapper.ClubAdminMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.FundingApplicationMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.PessimisticLockMapper;
import com.unimelb.swen90007.reactexampleapi.model.FundingApplication;
import com.unimelb.swen90007.reactexampleapi.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FundingApplicationService {

    private FundingApplicationMapper fundingApplicationMapper;
    private ClubAdminMapper clubAdminMapper;
    private PessimisticLockMapper pessimisticLockMapper;

    public FundingApplicationService() {
        this.fundingApplicationMapper = new FundingApplicationMapper();
        this.clubAdminMapper = new ClubAdminMapper();
        this.pessimisticLockMapper = new PessimisticLockMapper();
    }

    
    public int getNumberOfFundingApplications() throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.getNumberOfFundingApplications(connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // for faculty admin: Get all funding applications
    public List<FundingApplication> getAllFundingApplications() throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.getAllFundingApplications(connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // for faculty admin: Approve a funding application with optimistic lock
    public boolean approveFundingApplication(int applicationId, int version) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.updateFundingApplicationStatusWithVersion(applicationId, "approved", version, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // for faculty admin: Disapprove a funding application with optimistic lock
    public boolean disapproveFundingApplication(int applicationId, int version) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.updateFundingApplicationStatusWithVersion(applicationId, "disapproved", version, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // create application
    public void createFundingApplication(FundingApplication application, String requesterIdStr) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            UUID requesterId = UUID.fromString(requesterIdStr);

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, application.getClubId(), connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // Check if the club already has a funding application for this semester
            if (fundingApplicationMapper.applicationExistsForSemester(application.getClubId(),
                    application.getSemester(), connection)) {
                throw new SQLException("The club already has a funding application for this semester.");
            }

            // Set the date of the application to the current time
            application.setDate(Timestamp.from(Instant.now()));

            // Insert the funding application
            fundingApplicationMapper.insertFundingApplication(application, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get a funding application by application_id
    public FundingApplication getFundingApplicationById(int applicationId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.getFundingApplicationById(applicationId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get all funding applications for a specific club
    public List<FundingApplication> getFundingApplicationsByClubId(int clubId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.getFundingApplicationsByClubId(clubId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // delete funding application
    public boolean deleteFundingApplication(int applicationId, String requesterIdStr) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            UUID requesterId = UUID.fromString(requesterIdStr);

            // get funding application
            FundingApplication application = fundingApplicationMapper.getFundingApplicationById(applicationId, connection);
            if (application == null) {
                throw new SQLException("Funding application not found.");
            }

            // check whether the requester is admin
            if (!clubAdminMapper.isAdmin(requesterId, application.getClubId(), connection)) {
                return false; // if not, return false
            }

            // delete funding application
            fundingApplicationMapper.deleteFundingApplication(applicationId, connection);
            return true;
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Update a funding application with pessimistic locking and handle lock timeout
    public boolean updateFundingApplication(FundingApplication application, String requesterIdStr) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            connection.setAutoCommit(false);

            UUID requesterId = UUID.fromString(requesterIdStr);
            FundingApplication existingApplication = fundingApplicationMapper.getFundingApplicationById(application.getId(), connection);

            if (existingApplication == null) {
                throw new SQLException("Funding application not found.");
            }

            // Check if it is locked
            UUID lockedBy = pessimisticLockMapper.getLockedBy(existingApplication.getClubId(), existingApplication.getId(), connection);
            if (lockedBy != null && !lockedBy.equals(requesterId)) {
                // If locked by another user, throw an exception
                throw new SQLException("The funding application is currently locked by another admin.");
            }

            // If there is no lock or the lock is held by the current user, lock the funding application
            pessimisticLockMapper.lockApplication(existingApplication.getClubId(), existingApplication.getId(), requesterId, connection);

            // Perform the update
            fundingApplicationMapper.updateFundingApplication(application, connection);

            // Unlock the application
            pessimisticLockMapper.unlockApplication(existingApplication.getClubId(), existingApplication.getId(), connection);
            connection.commit();
            return true;
        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Lock a funding application
    public boolean lockFundingApplication(int clubId, int applicationId, String requesterIdStr) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            UUID requesterId = UUID.fromString(requesterIdStr);

            // Check if the requester is an admin of the club
            if (!clubAdminMapper.isAdmin(requesterId, clubId, connection)) {
                throw new SQLException("Requester is not an admin of the club.");
            }

            // Attempt to lock the application
            pessimisticLockMapper.lockApplication(clubId, applicationId, requesterId, connection);
            return true;
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Unlock a funding application
    public boolean unlockFundingApplication(int clubId, int applicationId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            // Attempt to unlock the application
            pessimisticLockMapper.unlockApplication(clubId, applicationId, connection);
            return true;
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // to get paginated funding applications - lazy load
    public List<FundingApplication> getPaginatedFundingApplications(int page, int pageSize) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return fundingApplicationMapper.getFundingApplicationsPaginated(page, pageSize, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }
}
