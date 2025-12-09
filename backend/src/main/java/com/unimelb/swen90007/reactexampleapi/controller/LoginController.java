package com.unimelb.swen90007.reactexampleapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unimelb.swen90007.reactexampleapi.model.ClubAdmin;
import com.unimelb.swen90007.reactexampleapi.model.ClubMember;
import com.unimelb.swen90007.reactexampleapi.model.Student;
import com.unimelb.swen90007.reactexampleapi.model.FacultyAdministrator; // 引入 FacultyAdministrator 类
import com.unimelb.swen90007.reactexampleapi.model.StudentClub;
import com.unimelb.swen90007.reactexampleapi.service.JwtService;
import com.unimelb.swen90007.reactexampleapi.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@WebServlet(name = "LoginController", urlPatterns = { "/login", "/staff-login" })
public class LoginController extends HttpServlet {

    private UserService userService;
    private JwtService jwtService = new JwtService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        // 设置响应的Content-Type为JSON
        resp.setContentType("application/json");

        // 从请求体中读取数据
        LoginRequest loginRequest;
        try {
            loginRequest = objectMapper.readValue(req.getReader(), LoginRequest.class);
        } catch (IOException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"message\":\"Invalid request body\"}");
            return;
        }

        // String staffIdParam = loginRequest.getStaffId();
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        Object user = null;
        try {
            // 使用 staffId 或 email 进行身份验证
            // if (staffIdParam != null && !staffIdParam.isEmpty()) {
            // System.out.println("Authenticate using Staff ID...");
            // UUID staffId = UUID.fromString(staffIdParam);
            // // 实现 Staff ID 的身份验证逻辑
            // } else
            if (email != null && !email.isEmpty()) {
                System.out.println("Authenticate using Email...");
                if ("/login".equals(path)) {
                    System.out.println("Trying to authenticate as Student...");
                    user = userService.authenticateStudent(email, password);
                } else if ("/staff-login".equals(path)) {
                    System.out.println("Trying to authenticate as Faculty Administrator...");
                    user = userService.authenticateStaff(email, password);
                }
            }

            // After attempting to login
            if (user == null) {
                System.out.println("Wrong username or password");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"message\":\"Wrong username or password\"}");
                return;
            } else if (user instanceof FacultyAdministrator) {
                FacultyAdministrator staff = (FacultyAdministrator) user;
                System.out.println("Authentication successful, staff: " + staff.getEmail());

                // 生成 token
                String token = jwtService.generateToken(staff, null, null, null, null);
                resp.getWriter().write("{\"token\":\"" + token + "\",\"user\":\"staff\"}");
            } else {
                Student student = (Student) user;
                System.out.println("Authentication successful, user: " + student.getEmail());

                // 获取 club admin, club member 和 student club 信息
                List<ClubAdmin> clubAdminList = userService.getClubAdminInfo(student.getStudentId());
                List<ClubMember> clubMemberList = userService.getClubMemberInfo(student.getStudentId());
                List<StudentClub> studentClubList = userService.getStudentClubsForStudent(student.getStudentId());

                // 生成 token
                String token = jwtService.generateToken(student, clubAdminList, clubMemberList, null,
                        studentClubList);
                resp.getWriter().write("{\"token\":\"" + token + "\",\"user\":\"student\"}");
            }
            resp.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"message\":\"Server Error\"}");
        }
    }

    // 定义一个用于解析请求体的内部类
    private static class LoginRequest {
        private String staffId;
        private String email;
        private String password;

        // Getters and setters
        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
