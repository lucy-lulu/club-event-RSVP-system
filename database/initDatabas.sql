BEGIN;
CREATE SCHEMA IF NOT EXISTS app AUTHORIZATION tv_addicts_react_owner;
SET search_path TO app;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";  

--Student
CREATE TABLE Student (
    student_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(), 
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

--StudentClub
CREATE TABLE StudentClub (
    club_id SERIAL PRIMARY KEY,
    club_name VARCHAR(100) NOT NULL
);


--Venue
CREATE TABLE Venue (
    venue_id SERIAL PRIMARY KEY,
    capacity INT NOT NULL,
    location VARCHAR(255) NOT NULL
);

--Event
CREATE TABLE Event (
    event_name VARCHAR(100) NOT NULL,
    event_id SERIAL PRIMARY KEY,
    club_id INT NOT NULL,
    venue_id INT,  
    title VARCHAR(100) NOT NULL,
    description text,
    cost DECIMAL(10, 2),
    date DATE,
    time INT,
    CONSTRAINT fk_club
        FOREIGN KEY (club_id) REFERENCES StudentClub(club_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_venue
        FOREIGN KEY (venue_id) REFERENCES Venue(venue_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

--RSVP_Ticket
CREATE TABLE RSVP_Ticket (
    ticket_id SERIAL PRIMARY KEY,
    attendee_id UUID NOT NULL,
    event_id INT NOT NULL,
    applicant_id UUID NOT NULL,
    CONSTRAINT fk_attendee
        FOREIGN KEY (attendee_id) REFERENCES Student(student_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_event
        FOREIGN KEY (event_id) REFERENCES Event(event_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_applicant
        FOREIGN KEY (applicant_id) REFERENCES Student(student_id)
            ON UPDATE CASCADE
            ON DELETE CASCADE
);

--StudentRSVP
CREATE TABLE StudentRSVP (
    student_id UUID NOT NULL,
    ticket_id INT NOT NULL,
    CONSTRAINT fk_student
        FOREIGN KEY (student_id) REFERENCES Student(student_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_ticket
        FOREIGN KEY (ticket_id) REFERENCES RSVP_Ticket(ticket_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (student_id, ticket_id)
);

--ClubMember
CREATE TABLE ClubMember (
    student_id UUID NOT NULL,
    club_id INT NOT NULL,
    CONSTRAINT fk_student_member
        FOREIGN KEY (student_id) REFERENCES Student(student_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_club_member
        FOREIGN KEY (club_id) REFERENCES StudentClub(club_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (student_id, club_id)
);

--ClubAdmin
CREATE TABLE ClubAdmin (
    student_id UUID NOT NULL,
    club_id INT NOT NULL,
    CONSTRAINT fk_student_admin
        FOREIGN KEY (student_id) REFERENCES Student(student_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_club_admin
        FOREIGN KEY (club_id) REFERENCES StudentClub(club_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (student_id, club_id)
);

--FundingApplication
CREATE TABLE FundingApplication (
    application_id SERIAL PRIMARY KEY,
    club_id INT NOT NULL,
    amount DECIMAL(10, 2),
    description TEXT,
    date DATE,
    status VARCHAR(50),
    semester CHAR(6),  
    CONSTRAINT fk_club_funding
        FOREIGN KEY (club_id) REFERENCES StudentClub(club_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

--FacultyAdministrator
CREATE TABLE FacultyAdministrator (
    staff_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    staff_name VARCHAR(100) NOT NULL
);
COMMIT;