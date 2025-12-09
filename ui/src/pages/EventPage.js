import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../App";
import { useNavigate } from "react-router-dom";

function EventPage({ display = "all" }) {
    const [events, setEvents] = useState([]); // Initialize events as an empty array
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(1); // State to track current page
    const [totalPages, setTotalPages] = useState(1); // Manage total pages state
    const [error, setError] = useState(null); // State to handle errors
    const pageSize = 5; // Number of events per page

    const navigate = useNavigate(); // Initialize navigation function

    const fetchEvents = async (page) => {
        setLoading(true);
        const fetchURL =
            display === "all"
                ? "get-paginated-events"
                : display === "past"
                  ? "get-past-events"
                  : "get-upcoming-events";

        try {
            const token = localStorage.getItem("token");
            const response = await fetch(
                `${API_BASE_URL}/${fetchURL}?page=${page}&pageSize=${pageSize}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            const data = await response.json();
            // Log the response data
            console.log("Response data:", data);

            if (data && Array.isArray(data)) {
                setEvents(data); // Set the events

                // Check if we received fewer items than the pageSize. If so, we're on the last page.
                if (data.length < pageSize) {
                    setTotalPages(currentPage); // Last page
                } else {
                    setTotalPages(currentPage + 1); // Assume there's another page
                }
            } else {
                setEvents([]); // Set to an empty array if the structure isn't as expected
                setTotalPages(1); // Default to 1 page
            }
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
        fetchEvents(currentPage); // Fetch events for the current page
    }, [currentPage, display]);

    const handlePageChange = (page) => {
        if (page >= 1 && page <= totalPages) {
            setCurrentPage(page); // Update the current page only if within bounds
        }
    };

    const handleRSVP = (eventId) => {
        // Redirect to the /rsvp page with eventId as a query parameter
        navigate(`/rsvp/get?eventId=${eventId}`);
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>
                {display === "all"
                    ? "All Events"
                    : display === "past"
                      ? "Past Events"
                      : "Upcoming Events"}
            </h1>
            {error && <div className="error">{error}</div>}{" "}
            {/* Display error if any */}
            <ul>
                {events && events.length > 0 ? (
                    events.map((event) => (
                        <li key={event.id}>
                            <h3>{event.eventName}</h3>
                            <p>{event.description}</p>
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
                            <button onClick={() => handleRSVP(event.id)}>
                                RSVP
                            </button>
                        </li>
                    ))
                ) : (
                    <p>No events available</p>
                )}
            </ul>
            <div>
                {/* Pagination Controls */}
                <button
                    disabled={currentPage === 1}
                    onClick={() => handlePageChange(currentPage - 1)}
                >
                    Previous
                </button>
                {Array.from({ length: totalPages }, (_, index) => (
                    <button
                        key={index + 1}
                        className={index + 1 === currentPage ? "active" : ""}
                        onClick={() => handlePageChange(index + 1)}
                    >
                        {index + 1}
                    </button>
                ))}
                <button
                    disabled={
                        events.length < pageSize || currentPage === totalPages
                    } // Check if the current page has fewer than the page size
                    onClick={() => handlePageChange(currentPage + 1)}
                >
                    Next
                </button>
            </div>
        </div>
    );
}

export default EventPage;
