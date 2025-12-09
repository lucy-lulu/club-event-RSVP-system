package com.unimelb.swen90007.reactexampleapi.model;

import java.util.UUID;

public class Student {
    private final UUID studentId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;

    public Student(UUID studentId, String firstName, String lastName, String email, String password) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    // 只保留 getter 方法
    public UUID getStudentId() {
        return studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
