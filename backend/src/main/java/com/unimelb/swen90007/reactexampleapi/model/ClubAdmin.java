package com.unimelb.swen90007.reactexampleapi.model;

import java.util.UUID;

public class ClubAdmin {
    private UUID studentId;
    private int clubId;

    // Constructors
    public ClubAdmin(UUID studentId, int clubId) {
        this.studentId = studentId;
        this.clubId = clubId;
    }

    // Getters and Setters
    public UUID getStudentId() {  // 返回类型改为 UUID
        return studentId;
    }

    public void setStudentId(UUID studentId) {  // 参数类型改为 UUID
        this.studentId = studentId;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    @Override
    public String toString() {
        return "ClubAdmin{" +
                "studentId=" + studentId +
                ", clubId=" + clubId +
                '}';
    }
}
