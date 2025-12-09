package com.unimelb.swen90007.reactexampleapi.model;

public abstract class DomainModel {
    protected int id;

    // Constructor with ID
    public DomainModel(int id) {
        this.id = id;
    }

    // Default constructor
    public DomainModel() {}

    // Getter for ID
    public int getId() {
        return id;
    }

    // Setter for ID
    public void setId(int id) {
        this.id = id;
    }
}
