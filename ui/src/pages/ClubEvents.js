import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../App";
import { useNavigate } from "react-router-dom";
import { notification } from "antd";

function ClubEvents() {
    const [events, setEvents] = useState([]); // Initialize events as an empty array
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null); // State to handle errors
    const navigate = useNavigate(); // Initialize navigation function
    const [lastEditedEventId, setLastEditedEventId] = useState(null); // Track the last edited event ID

    // Fetch events and keep them sorted by event_id or another field
    const fetchEvents = async () => {
        setLoading(true);
        setError("");
        try {
            const club_id = localStorage.getItem("club");
            const response = await fetch(
                `${API_BASE_URL}/get-events-by-club?club_id=${club_id}`
            );
            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }
            const data = await response.json();

            // Sort events by event_id to keep them in a consistent order
            const sortedData = data.sort((a, b) => a.event_id - b.event_id); // Sort by event_id or use another field if needed

            setEvents(sortedData); // Set the sorted events
        } catch (error) {
            console.error("Error fetching events:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching events " + errorMessage[errorMessage.length - 1]
            ); // Set error message
        } finally {
            setLoading(false); // Hide loading once fetch is complete
        }
    };

    useEffect(() => {
        fetchEvents(); // Fetch events for the current page, sorted by event_id
    }, []);

    // Edit event handler
    const handleEdit = async (editEvent) => {
        setLoading(true);
        try {
            const club_id = localStorage.getItem("club");
            const token = localStorage.getItem("token");

            // Get the latest event data from backend
            const eventResponse = await fetch(
                `${API_BASE_URL}/get-event-by-id?event_id=${editEvent.id}`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!eventResponse.ok) {
                throw new Error("Failed to fetch the latest event data.");
            }

            const latestEvent = await eventResponse.json(); // Get the latest data

            // Lock the event for editing
            const lockResponse = await fetch(`${API_BASE_URL}/lock-event`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    event_id: latestEvent.id,
                    club_id: club_id,
                }),
            });

            if (lockResponse.status === 409) {
                // Handle lock conflict
                const errorData = await lockResponse.json();
                throw new Error(
                    errorData.error ||
                        "This event is currently being edited by another admin."
                );
            }

            if (!lockResponse.ok) {
                const errorData = await lockResponse.json();
                throw new Error(errorData.error || "Failed to lock the event");
            }

            // Store the latest event data and navigate to the edit page
            localStorage.setItem("event", JSON.stringify(latestEvent));
            navigate(`/club/event/edit`);
            setLastEditedEventId(latestEvent.id); // Track the last edited event
        } catch (error) {
            console.error("Error locking event:", error);
            notification.error({
                message: "Error",
                description:
                    error.message ||
                    "An error occurred while locking the event.",
            });
        } finally {
            setLoading(false);
        }
    };

    // Delete event handler
    const handleDelete = async (eventId) => {
        setLoading(true);
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/remove-event`, {
                method: "POST",
                body: JSON.stringify({
                    event_id: eventId,
                }),
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }
            fetchEvents(); // Refresh the event list after deleting
        } catch (error) {
            console.error("Error deleting event:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error deleting event " + errorMessage[errorMessage.length - 1]
            ); // Set error message
        } finally {
            setLoading(false); // Hide loading once fetch is complete
        }
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>All Club Events</h1>
            {error && <div className="error">{error}</div>}{" "}
            {/* Display error if any */}
            <ul>
                {events && events.length > 0 ? (
                    events.map((event) => (
                        <li
                            key={event.id}
                            className={
                                event.id === lastEditedEventId
                                    ? "highlight"
                                    : ""
                            }
                        >
                            <h3>{event.eventName}</h3>
                            <p>{event.description}</p>
                            <p>{event.title}</p>
                            <p>Cost: ${event.cost}</p>
                            <p>
                                Date:{" "}
                                {new Date(event.date).toLocaleDateString()}
                            </p>
                            <p>
                                Time:{" "}
                                {parseInt(event.time / 100)
                                    .toString()
                                    .padStart(2, "0")}
                                {":"}
                                {(event.time % 100).toString().padStart(2, "0")}
                            </p>
                            <p>Venue ID: {event.venueId}</p>
                            <button onClick={() => handleEdit(event)}>
                                Edit
                            </button>
                            <button onClick={() => handleDelete(event.id)}>
                                Delete
                            </button>
                        </li>
                    ))
                ) : (
                    <p>No events available</p>
                )}
            </ul>
        </div>
    );
}

export default ClubEvents;
