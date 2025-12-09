import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../../App";

function AttendeeTickets() {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const fetchTickets = async () => {
        setLoading(true);
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/get-ticket`, {
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
            setTickets(data);
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

    const handleCancel = async (ticketId) => {
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(
                `${API_BASE_URL}/delete-student-ticket`,
                {
                    method: "POST",
                    body: JSON.stringify({
                        ticket_id: ticketId,
                    }),
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            fetchTickets();

            const data = await response.json();
            console.log(data);
        } catch (error) {
            console.error("Error deleting ticket:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error deleting ticket " + errorMessage[errorMessage.length - 1]
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
                {tickets && tickets.length > 0 ? (
                    tickets.map((ticket, index) => (
                        <li key={index}>
                            <h3> {ticket.event_title}</h3>
                            <p>Status: Confirmed</p>
                            <p>
                                Date:{" "}
                                {new Date(
                                    ticket.event_date
                                ).toLocaleDateString()}{" "}
                                {parseInt(ticket.event_time / 100)
                                    .toString()
                                    .padStart(2, "0")}
                                {":"}
                                {(ticket.event_time % 100)
                                    .toString()
                                    .padStart(2, "0")}
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
                )}
            </ul>
        </div>
    );
}

export default AttendeeTickets;
