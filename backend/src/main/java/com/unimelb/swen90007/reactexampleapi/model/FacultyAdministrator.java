package com.unimelb.swen90007.reactexampleapi.model;

import java.util.UUID;

public class FacultyAdministrator {
    private UUID staffId;
    private String facultyAdminName;
    private String email; // 新增字段：email
    private String hashPassword;

    // Constructors
    public FacultyAdministrator() {}

    public FacultyAdministrator(UUID staffId, String facultyAdminName, String email, String hashPassword) {
        this.staffId = staffId;
        this.facultyAdminName = facultyAdminName;
        this.email = email; // 新增字段的初始化
        this.hashPassword = hashPassword;
    }

    // Getters and Setters
    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(UUID staffId) {
        this.staffId = staffId;
    }

    public String getFacultyAdminName() {
        return facultyAdminName;
    }

    public void setFacultyAdminName(String facultyAdminName) {
        this.facultyAdminName = facultyAdminName;
    }

    public String getEmail() { // 新增 getEmail 方法
        return email;
    }

    public void setEmail(String email) { // 新增 setEmail 方法
        this.email = email;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }
}
