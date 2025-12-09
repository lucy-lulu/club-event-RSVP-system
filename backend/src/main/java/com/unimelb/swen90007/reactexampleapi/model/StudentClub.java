package com.unimelb.swen90007.reactexampleapi.model;

public class StudentClub extends DomainModel {
    private String clubName;

    // No-arg constructor
    public StudentClub() {
        super();  // Calls the parent constructor
    }

    // Parameterized constructor
    public StudentClub(int clubId, String clubName) {
        super(clubId);  // Calls the parent constructor to set the clubId
        this.clubName = clubName;
    }

    // Getters
    public int getClubId() {
        return getId();  // Inherited method for ID
    }

    public String getClubName() {
        return clubName;
    }

    // Setters
    public void setClubId(int clubId) {
        setId(clubId);  // Inherited method to set the ID
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    @Override
    public String toString() {
        return "StudentClub{" +
                "clubId=" + getId() +  // Use the inherited getId() method
                ", clubName='" + clubName + '\'' +
                '}';
    }
}
