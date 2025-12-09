package com.unimelb.swen90007.reactexampleapi.model;

import java.util.UUID;

public class RSVPTicket {
    private int ticketId;
    private UUID attendeeId;  // The student who RSVP'd to the event
    private int eventId;  // The event that the student RSVP'd for
    private UUID applicantId;  // The user who applied the RSVP (can be the same as attendeeId)

    // No-arg constructor
    public RSVPTicket() {}

    // Constructor with essential fields (attendeeId and eventId)
    public RSVPTicket(UUID attendeeId, int eventId) {
        this.attendeeId = attendeeId;
        this.eventId = eventId;
    }

    // Constructor with ticketId, attendeeId, and eventId
    public RSVPTicket(int ticketId, UUID attendeeId, int eventId) {
        this.ticketId = ticketId;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
    }

    // Constructor with ticketId, attendeeId, eventId, and applicantId
    public RSVPTicket(int ticketId, UUID attendeeId, int eventId, UUID applicantId) {
        this.ticketId = ticketId;
        this.attendeeId = attendeeId;
        this.eventId = eventId;
        this.applicantId = applicantId;
    }

    // Getters and Setters
    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(UUID attendeeId) {
        this.attendeeId = attendeeId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public UUID getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(UUID applicantId) {
        this.applicantId = applicantId;
    }

    @Override
    public String toString() {
        return "RSVPTicket{" +
                "ticketId=" + ticketId +
                ", attendeeId=" + attendeeId +
                ", eventId=" + eventId +
                ", applicantId=" + applicantId +
                '}';
    }
}
