import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../../App";

function ApplicantTickets() {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const fetchTickets = async () => {
        setLoading(true);
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/get-application`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            const data = await response.json();
            console.log(data);
            setEvents(data);
        } catch (error) {
            console.error("Error fetching tickets:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching tickets " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTickets();
    }, []);

    const handleCancel = async (ticketId, emailId) => {
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/delete-rsvp`, {
                method: "POST",
                body: JSON.stringify({
                    attendee_email: emailId,
                    ticket_id: ticketId,
                }),
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            fetchTickets();

            const data = await response.json();
            console.log(data);
        } catch (error) {
            console.log(error);
            const errorMessage = String(error).split("Error:");
            console.error("Error deleting ticket:", error);
            setError(
                "Error deleting ticket. " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div>
            <h1>My Tickets</h1>
            <ul>
                {events && events.length > 0 ? (
                    events.map((event) => (
                        <li key={event.rsvp.ticketId}>
                            <h1>{event.event.event_name}</h1>
                            <h3>
                                {event.rsvp.firstName} {event.rsvp.lastName}
                            </h3>
                            <p>
                                Date:{" "}
                                {new Date(
                                    event.event.date
                                ).toLocaleDateString()}{" "}
                                {parseInt(event.event.time / 100)
                                    .toString()
                                    .padStart(2, "0")}
                                {":"}
                                {(event.event.time % 100)
                                    .toString()
                                    .padStart(2, "0")}
                            </p>
                            <button
                                onClick={() =>
                                    handleCancel(
                                        event.rsvp.ticketId,
                                        event.rsvp.email
                                    )
                                }
                            >
                                Cancel
                            </button>
                        </li>
                    ))
                ) : (
                    <p>No tickets available</p>
                )}
                {/* {tickets.length > 0 ? (
                    tickets.map((ticket, index) => (
                        <li key={index}>
                            <h3> {ticket.event_title}</h3>
                            <p>Status: Confirmed</p>
                            <p>
                                Date:{" "}
                                {new Date(ticket.event_date).toLocaleString()}
                            </p>
                            <p>Venue: {ticket.venue_id}</p>
                            <button
                                onClick={() => handleCancel(ticket.ticket_id)}
                            >
                                Cancel
                            </button>
                        </li>
                    ))
                ) : (
                    <p>No tickets available</p>
                )} */}
            </ul>
        </div>
    );
}

export default ApplicantTickets;
