package com.unimelb.swen90007.reactexampleapi.service;

import com.unimelb.swen90007.reactexampleapi.model.ClubAdmin;
import com.unimelb.swen90007.reactexampleapi.model.ClubMember;
import com.unimelb.swen90007.reactexampleapi.model.FacultyAdministrator;
import com.unimelb.swen90007.reactexampleapi.model.Student;
import com.unimelb.swen90007.reactexampleapi.model.StudentClub;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtService {
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public static SecretKey getSecretKey() {
        return SECRET_KEY;
    }

    // Generate a token and include all relevant information
    public String generateToken(Object user,
                                List<ClubAdmin> clubAdminList,
                                List<ClubMember> clubMemberList,
                                FacultyAdministrator facultyAdmin,
                                List<StudentClub> studentClubList) {
        Claims claims = Jwts.claims();

        if (user instanceof Student) {
            Student student = (Student) user;
            claims.setSubject(student.getStudentId().toString());

            // Add student basic information
            claims.put("student", Map.of(
                    "studentId", student.getStudentId(),
                    "firstName", student.getFirstName(),
                    "lastName", student.getLastName(),
                    "email", student.getEmail()
            ));

            // Add ClubAdmin information
            if (clubAdminList != null) {
                claims.put("clubAdmins", clubAdminList.stream()
                        .map(admin -> Map.of("clubId", admin.getClubId()))
                        .collect(Collectors.toList()));
            }

            // Add ClubMember information
            if (clubMemberList != null) {
                claims.put("clubMembers", clubMemberList.stream()
                        .map(member -> Map.of("clubId", member.getClubId()))
                        .collect(Collectors.toList()));
            }

            // Add StudentClub information
            if (studentClubList != null) {
                claims.put("studentClubs", studentClubList.stream()
                        .map(club -> Map.of("clubId", club.getClubId(), "clubName", club.getClubName()))
                        .collect(Collectors.toList()));
            }

        } else if (user instanceof FacultyAdministrator) {
            FacultyAdministrator staff = (FacultyAdministrator) user;
            claims.setSubject(staff.getStaffId().toString());

            // Add faculty administrator information
            claims.put("facultyAdmin", Map.of(
                    "staffId", staff.getStaffId(),
                    "facultyAdminName", staff.getFacultyAdminName(),
                    "email", staff.getEmail()
            ));
        }

        // Generate JWT Token
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SECRET_KEY)
                .compact();
    }
}
