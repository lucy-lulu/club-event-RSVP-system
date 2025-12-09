BEGIN;
INSERT INTO app.student (student_id, first_name, last_name, email, password)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 'Michael', 'Taylor', 'michael.taylor@gmail.com', '$2a$10$tDnrEChrRyJQzxyuVx3DwecwygGtsuFkGG./Z9beXgfOmbmPFHJae'),
    ('550e8400-e29b-41d4-a716-446655440001', 'Emily', 'Davis', 'emily.davis@gmail.com', '$2a$10$oEFISM/bVZjWWB06HTXzRe/yFKB6gq8X1kUoUtHsfs1P35SS9q5aC'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Chris', 'Martin', 'chris.martin@gmail.com', '$2a$10$dcbXyAwkHnk9GkdI9cdTTOp6qqwq.E3ij8egFUmKNpLfgA438V/SW'),
    ('550e8400-e29b-41d4-a716-446655440003', 'Jessica', 'Williams', 'jessica.williams@gmail.com', '$2a$10$LecFTH572juvsrjmUvkjqeBT0vhyNQM4WupI7YvQVlP2XbrdJutdG');

INSERT INTO app.studentclub (club_id, club_name)
VALUES 
    (1, 'Music Club'),
    (2, 'Drama Club'),
    (3, 'Science Club'),
    (4, 'Chess Club');

INSERT INTO app.venue (venue_id, capacity, location)
VALUES 
    (1, 150, 'Music Hall'),
    (2, 60, 'Drama Theater'),
    (3, 250, 'Science Lecture Room'),
    (4, 40, 'Chess Club Room');


INSERT INTO app.fundingapplication (application_id, club_id, amount, description, date, status, semester)
VALUES 
    (1, 2, 1500.00, 'Funding for workshop', '2024-09-16', 'submitted', '2024S2'),
    (2, 3, 5000.00, 'Funding for club activities', '2024-03-16', 'submitted', '2024S1'),
    (3, 1, 750.00, 'Funding for new equipment', '2023-09-17', 'approved', '2023S2');

INSERT INTO app.facultyadministrator (staff_id, staff_name)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440008', 'Dr. Sarah Lee'),
    ('550e8400-e29b-41d4-a716-446655440009', 'Prof. John Adams'),
    ('550e8400-e29b-41d4-a716-446655440010', 'Dr. Emily White'),
    ('550e8400-e29b-41d4-a716-446655440011', 'Dr. Michael Johnson');

INSERT INTO app.clubmember (student_id, club_id)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 2),
    ('550e8400-e29b-41d4-a716-446655440002', 1),
    ('550e8400-e29b-41d4-a716-446655440002', 2),
    ('550e8400-e29b-41d4-a716-446655440001', 3),
    ('550e8400-e29b-41d4-a716-446655440003', 2);

INSERT INTO app.clubadmin (student_id, club_id)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 2),
    ('550e8400-e29b-41d4-a716-446655440002', 1),
    ('550e8400-e29b-41d4-a716-446655440003', 2);

INSERT INTO app.event (event_id, club_id, venue_id, event_name, title, description, cost, date, time)
VALUES 
    (1, 1, 1, 'Music Concert', 'Classical Music Night', 'An evening of classical music performances', 150.00, '2024-11-01', 1900),
    (2, 2, 2, 'Drama Play', 'Theater Performance', 'A live theater performance by Drama Club members', 75.00, '2024-11-05', 1800),
    (3, 3, 3, 'Science Symposium', 'Future of Science', 'A symposium discussing the future of scientific research', 200.00, '2024-11-10', 0900),
    (4, 4, 4, 'Chess Tournament', 'Annual Chess Competition', 'A competitive chess tournament for club members', 50.00, '2024-11-15', 1400);


INSERT INTO app.rsvp_ticket (ticket_id, attendee_id, event_id,applicant_id)
VALUES 
    (1, '550e8400-e29b-41d4-a716-446655440000', 1, '550e8400-e29b-41d4-a716-446655440000'),
    (2, '550e8400-e29b-41d4-a716-446655440001', 2, '550e8400-e29b-41d4-a716-446655440000'),
    (3, '550e8400-e29b-41d4-a716-446655440002', 3, '550e8400-e29b-41d4-a716-446655440002'),
    (4, '550e8400-e29b-41d4-a716-446655440003', 1, '550e8400-e29b-41d4-a716-446655440003');

INSERT INTO app.studentrsvp (student_id, ticket_id)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 1),
    ('550e8400-e29b-41d4-a716-446655440001', 2),
    ('550e8400-e29b-41d4-a716-446655440002', 3),
    ('550e8400-e29b-41d4-a716-446655440003', 4);
commit;