package com.unimelb.swen90007.reactexampleapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.UUID;

public class FundingApplication extends DomainModel {
    private int clubId;
    private double amount;
    private String description;
    private String semester; // eg. 2023s2
    private Timestamp date;
    private String status;
    private int version;  // Optimistic lock version field
    private UUID lockedBy;

    // Constructors
    public FundingApplication(int clubId, double amount, String description, String semester, Timestamp date, String status, int version) {
        this.clubId = clubId;
        this.amount = amount;
        this.description = description;
        this.semester = semester;
        this.date = date;
        this.status = status;
        this.version = version; // for optimistic lock
    }

    public FundingApplication(int applicationId, int clubId, double amount, String description, String semester, Timestamp date, String status) {
        super(applicationId);  // Pass the ID to the base class (DomainModel)
        this.clubId = clubId;
        this.amount = amount;
        this.description = description;
        this.semester = semester;
        this.date = date;
        this.status = status;
        this.version = 1; // for optimistic lock
    }

    public FundingApplication(int applicationId, int clubId, double amount, String description, String semester, Timestamp date, String status, int version) {
        super(applicationId);  // Pass the ID to the base class (DomainModel)
        this.clubId = clubId;
        this.amount = amount;
        this.description = description;
        this.semester = semester;
        this.date = date;
        this.status = status;
        this.version = version; // for optimistic lock
    }

    // Getters and Setters
    @JsonProperty("application_id")
    public int getId() {
        return super.getId(); // 获取 DomainModel 中的 ID
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // for optimistic lock
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public UUID getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(UUID lockedBy) {
        this.lockedBy = lockedBy;
    }

    @Override
    public String toString() {
        return "FundingApplication{" +
                "application_id=" + getId() +  // Inherited from DomainModel
                ", clubId=" + clubId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", semester='" + semester + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", lockedBy=" + lockedBy +
                '}';
    }
}
