<div align="center">

# Campus Club Event RSVP Platform

A full-stack event management system for university clubs, enabling students to discover club events, RSVP, receive tickets, and manage club participation through a web-based platform.

<br/>

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-brightgreen?style=for-the-badge&logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?style=for-the-badge&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue?style=for-the-badge&logo=postgresql)
![JWT](https://img.shields.io/badge/JWT-Authentication-purple?style=for-the-badge&logo=jsonwebtokens)

<br/>

**Full-stack web app · Student club management · RSVP workflow · Ticketing · Role-based access**

</div>

---

## Overview

Campus Club Event RSVP Platform is a full-stack web application designed for university student clubs and societies.

The system supports a practical event-management workflow where students can browse clubs, join club communities, RSVP to events, and receive event tickets. Club administrators can manage club-related operations, while the backend handles authentication, membership, RSVP records, and ticket-related data.

This project demonstrates full-stack application development using a React frontend, Spring Boot backend, PostgreSQL database, RESTful APIs, JWT-based authentication, and role-aware business logic.

---

## Product Motivation

Student clubs often manage events through fragmented tools such as group chats, spreadsheets, forms, and manual attendance lists. This creates friction for both students and club organisers.

This platform explores a more structured solution:

- students can discover clubs and events in one place
- club membership and event registration are handled consistently
- RSVP and ticket records are stored in the backend
- club administrators can manage operations with clear permissions
- the system can be extended into a broader campus engagement platform

---

## Core Features

### Student Experience

- Browse available student clubs
- View event information
- RSVP to club events
- Receive and manage event tickets
- Track personal club memberships

### Club Administration

- Manage club membership
- Add club administrators
- Control access to club-level operations
- Manage RSVP and ticket-related workflows

### RSVP and Ticketing

- Create RSVP records for events
- Generate associated ticket records
- Delete RSVP and ticket data according to business rules
- Maintain consistency between registration and ticket ownership

### Authentication and Access Control

- User login workflow
- JWT-based authentication
- Role-aware backend operations
- Permission checks for administrator actions

---

## Tech Stack

### Frontend

- React 18
- React Router
- Axios
- Material UI
- Ant Design
- JWT Decode
- ESLint
- Prettier

### Backend

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Maven
- WAR packaging

### Database

- PostgreSQL
- SQL schema and seed scripts
- Relational modelling for students, clubs, memberships, events, RSVPs, and tickets

---

## Architecture

```text
React Frontend
     |
     | HTTP / JSON
     v
Spring Boot REST API
     |
     | Authentication + Business Logic
     v
JPA Persistence Layer
     |
     v
PostgreSQL Database
```

The frontend communicates with the backend through RESTful APIs. The backend manages authentication, authorisation, RSVP workflows, ticket operations, and database persistence.

---

## Repository Structure

```text
.
├── backend/                 # Spring Boot backend application
│   ├── src/                 # Java source code
│   ├── pom.xml              # Maven configuration
│   └── Dockerfile           # Backend container setup
│
├── ui/                      # React frontend application
│   ├── public/
│   ├── src/
│   └── package.json
│
├── database/                # Database schema and seed scripts
│   ├── initDatabas.sql
│   └── load_db_part2_fin.sql
│
├── docs/                    # Project documentation and diagrams
│   ├── diagrams/
│   ├── meetings/
│   ├── part1/
│   └── part2/
│
└── README.md
```

---

## Getting Started

### Prerequisites

Make sure you have the following installed:

- Java 17
- Maven
- Node.js
- npm
- PostgreSQL

---

## Backend Setup

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

For Windows:

```bash
cd backend
mvnw.cmd clean install
mvnw.cmd spring-boot:run
```

The backend is built with Spring Boot and expects a PostgreSQL database connection. Update the database configuration in the backend application settings before running locally.

---

## Frontend Setup

```bash
cd ui
npm install
npm start
```

The frontend runs in development mode at:

```text
http://localhost:3000
```

---

## Database Setup

The `database/` folder contains SQL scripts for initialising and loading the database.

Recommended setup flow:

```bash
createdb club_event_rsvp
psql -d club_event_rsvp -f database/initDatabas.sql
psql -d club_event_rsvp -f database/load_db_part2_fin.sql
```

Adjust the database name and connection details based on your local PostgreSQL configuration.

---

## Example Demo Data

The original project included sample student accounts and club memberships for testing. For portfolio use, credentials are intentionally not highlighted as public login information.

Example seeded users include:

- John Doe
- Jane Smith
- Robert Johnson

Example clubs include:

- Basketball Club
- Football Club
- Tennis Club

Each seeded student is associated with club membership data, and selected users have administrator permissions for specific clubs.

---

## My Contribution

My work focused on full-stack delivery and backend-oriented system design, including:

- implementing backend business logic
- supporting REST API development
- working with database schema and seed data
- integrating frontend pages with backend APIs
- handling RSVP, ticket, and club-management workflows
- documenting setup details and user-facing behaviour
- debugging deployment and integration issues across frontend, backend, and database layers

---

## Team Members

 - Luyun Li
 - Mingda Zheng
 - Haitian Wang
 - Sameer Sikka
