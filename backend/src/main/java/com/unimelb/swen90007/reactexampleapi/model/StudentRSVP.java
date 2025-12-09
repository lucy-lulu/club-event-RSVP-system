package com.unimelb.swen90007.reactexampleapi.model;

import java.util.UUID;

public class StudentRSVP {
    private UUID studentId;  // The student who RSVP'd (same as attendeeId)
    private int ticketId;  // The ticket issued for the RSVP

    // No-arg constructor
    public StudentRSVP() {}

    // Constructor with all fields
    public StudentRSVP(UUID studentId, int ticketId) {
        this.studentId = studentId;
        this.ticketId = ticketId;
    }

    // Getters and Setters
    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    @Override
    public String toString() {
        return "StudentRSVP{" +
                "studentId=" + studentId +
                ", ticketId=" + ticketId +
                '}';
    }
}
