package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.mapper.ClubAdminMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.ClubMemberMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.StudentClubMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.StudentMapper;
import com.unimelb.swen90007.reactexampleapi.mapper.FacultyAdministratorMapper; // 新增的Mapper
import com.unimelb.swen90007.reactexampleapi.model.ClubAdmin;
import com.unimelb.swen90007.reactexampleapi.model.ClubMember;
import com.unimelb.swen90007.reactexampleapi.model.Student;
import com.unimelb.swen90007.reactexampleapi.model.StudentClub;
import com.unimelb.swen90007.reactexampleapi.model.FacultyAdministrator; // 新增的Model
import com.unimelb.swen90007.reactexampleapi.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class UserService {

    private final StudentMapper studentMapper;
    private final ClubAdminMapper clubAdminMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final StudentClubMapper studentClubMapper;
    private final FacultyAdministratorMapper facultyAdministratorMapper; // 新增的Mapper

    public UserService() {
        this.studentMapper = new StudentMapper();
        this.clubAdminMapper = new ClubAdminMapper();
        this.clubMemberMapper = new ClubMemberMapper();
        this.studentClubMapper = new StudentClubMapper();
        this.facultyAdministratorMapper = new FacultyAdministratorMapper(); // 初始化
    }

    // Student login with email and password
    public Student authenticateStudent(String email, String plainPassword) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            boolean isAuthenticated = studentMapper.verifyStudentLogin(email, plainPassword, connection);

            if (isAuthenticated) {
                // Get student information
                return studentMapper.getStudentByEmail(email, connection);
            } else {
                return null;
            }
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Faculty Administrator login with email and password
    public FacultyAdministrator authenticateStaff(String email, String plainPassword) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();

            boolean isAuthenticated = facultyAdministratorMapper.verifyStaffLogin(email, plainPassword, connection);

            if (isAuthenticated) {
                // Get staff information
                return facultyAdministratorMapper.getFacultyAdministratorByEmail(email, connection);
            } else {
                return null;
            }
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get ClubAdmin information for the student
    public List<ClubAdmin> getClubAdminInfo(UUID studentId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return clubAdminMapper.getClubAdminsByStudentId(studentId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get ClubMember information for the student
    public List<ClubMember> getClubMemberInfo(UUID studentId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return clubMemberMapper.getClubMembersByStudentId(studentId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }

    // Get StudentClub information for the student
    public List<StudentClub> getStudentClubsForStudent(UUID studentId) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getInstance().nextConnection();
            return studentClubMapper.getStudentClubsByStudentId(studentId, connection);
        } finally {
            if (connection != null) {
                DatabaseConnection.getInstance().releaseConnection(connection);
            }
        }
    }
}
