package com.unimelb.swen90007.reactexampleapi.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Event extends DomainModel {
    private String event_name;
    private int clubId;
    private int venue_id;
    private String title;
    private String description;
    private BigDecimal cost;
    private Timestamp date;
    private int time;

    // Constructors
    public Event() {}

    public Event(int clubId, String event_name, String title, String description, BigDecimal cost, Timestamp date, int time, int venue_id) {
        this.clubId = clubId;
        this.event_name = event_name;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.date = date;
        this.time = time;
        this.venue_id = venue_id;
    }

    public Event(int eventId, String event_name, int clubId, String title, String description, BigDecimal cost, Timestamp date, int time, int venue_id) {
        super(eventId);  // Pass the event ID to the base class (DomainModel)
        this.event_name = event_name;
        this.clubId = clubId;
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.date = date;
        this.time = time;
        this.venue_id = venue_id;
    }

    // construction for lazy load
    public Event(int eventId, String eventName, int clubId, String title, Timestamp date) {
    }

    // Getters and Setters
    public String getEventName() {
        return event_name;
    }

    public void setEventName(String event_name) {
        this.event_name = event_name;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getVenueId() {
        return venue_id;
    }

    public void setVenueId(int venue_id) {
        this.venue_id = venue_id;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +  // Inherited from DomainModel
                ", eventName='" + event_name + '\'' +
                ", clubId=" + clubId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                ", date=" + date +
                ", time=" + time +
                ", venueId=" + venue_id +
                '}';
    }
}
