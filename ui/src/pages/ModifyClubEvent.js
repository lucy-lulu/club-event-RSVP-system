import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../App";
import { useNavigate } from "react-router-dom";

function ModifyClubEvent({ isCreate = true }) {
    const [eventName, setEventName] = useState("");
    const [description, setDescription] = useState("");
    const [title, setTitle] = useState("");
    const [cost, setCost] = useState("");
    const [previousDate, setPrevDate] = useState("");
    const [date, setDate] = useState("");
    const [previousTime, setPrevTime] = useState("");
    const [time, setTime] = useState("");
    const [venueId, setVenueId] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [lockError, setLockError] = useState(""); // Track lock errors
    const [latestEvent, setLatestEvent] = useState(null); // Store latest event data
    const navigate = useNavigate();

    function formatDate(date) {
        let d = new Date(date);
        let day = d.getDate().toString().padStart(2, "0");
        let month = (d.getMonth() + 1).toString().padStart(2, "0");
        let year = d.getFullYear();
        return `${year}-${month}-${day}`;
    }

    // Fetch latest event data
    const fetchLatestEventData = async (eventId) => {
        const token = localStorage.getItem("token");
        try {
            const response = await fetch(
                `${API_BASE_URL}/get-event-by-id?event_id=${eventId}`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to fetch the latest event data.");
            }

            const data = await response.json();
            setLatestEvent(data);
            return data;
        } catch (error) {
            console.error("Error fetching event data:", error);
            setError("Error fetching the latest event data.");
            return null;
        }
    };

    // Unlock the event after editing
    const unlockEvent = async (eventId, clubId) => {
        const token = localStorage.getItem("token");
        try {
            const response = await fetch(`${API_BASE_URL}/unlock-event`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    event_id: eventId,
                    club_id: clubId,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(
                    errorData.error || "Failed to unlock the event"
                );
            }

            return true;
        } catch (error) {
            console.error("Error unlocking event:", error);
            return false;
        }
    };

    useEffect(() => {
        const modifyEvent = JSON.parse(localStorage.getItem("event"));

        if (!isCreate && modifyEvent) {
            const eventId = modifyEvent.id;

            // Fetch latest event data and update the UI
            fetchLatestEventData(eventId).then((latestEventData) => {
                if (latestEventData) {
                    setEventName(latestEventData.eventName);
                    setTitle(latestEventData.title);
                    setDescription(latestEventData.description);
                    setCost(latestEventData.cost);
                    setPrevDate(formatDate(latestEventData.date));
                    setDate(formatDate(latestEventData.date));
                    setPrevTime(latestEventData.time);
                    const hrs = parseInt(latestEventData.time / 100);
                    const mins = latestEventData.time % 100;
                    setTime(
                        `${hrs.toString().padStart(2, "0")}:${mins
                            .toString()
                            .padStart(2, "0")}`
                    );
                    setVenueId(latestEventData.venueId);
                }
            });
        }
    }, [isCreate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError("");

        try {
            const token = localStorage.getItem("token");
            const clubId = localStorage.getItem("club");
            const eventId = latestEvent.id;
            setCost(parseFloat(cost));
            const splitDate = date + " " + time + ":00";
            const splitTime = time.split(":").join("");
            setVenueId(parseInt(venueId));

            let response = null;
            if (!isCreate) {
                // Update event
                response = await fetch(`${API_BASE_URL}/edit-event`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                    method: "POST",
                    body: JSON.stringify({
                        club_id: clubId,
                        event_id: eventId,
                        event_name: eventName,
                        title: title,
                        description: description,
                        cost: cost,
                        time: splitTime,
                        date: splitDate,
                        venue_id: venueId,
                    }),
                });

                await unlockEvent(eventId, clubId);
                localStorage.removeItem("event");
            } else {
                // Create new event
                response = await fetch(`${API_BASE_URL}/create-event`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                    method: "POST",
                    body: JSON.stringify({
                        club_id: clubId,
                        event_name: eventName,
                        title: title,
                        description: description,
                        cost: cost,
                        time: splitTime,
                        date: splitDate,
                        venue_id: venueId,
                    }),
                });
            }

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            navigate("/club/event"); // Navigate to event page
        } catch (error) {
            console.error("Error submitting event:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error submitting event. " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1>{!isCreate ? "Edit Event" : "Create Event"}</h1>
            {lockError && <p className="error">{lockError}</p>}
            {error && <p className="error">{error}</p>}
            <form onSubmit={handleSubmit}>
                <div>
                    <label>Event Name</label>
                    <input
                        type="text"
                        value={eventName}
                        onChange={(e) => setEventName(e.target.value)}
                        required
                    />
                </div>

                <div>
                    <label>Description</label>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Title</label>
                    <textarea
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        required
                    />
                </div>

                <div>
                    <label>Cost ($)</label>
                    <input
                        type="number"
                        value={cost}
                        onChange={(e) => setCost(e.target.value)}
                        required
                    />
                </div>

                <div>
                    <label>Date</label>
                    <input
                        type="date"
                        value={date}
                        onChange={(e) => setDate(e.target.value)}
                        required
                    />
                    {previousDate && `Previously ${previousDate}`}
                </div>

                <div>
                    <label>Time</label>
                    <input
                        type="time"
                        value={time}
                        onChange={(e) => setTime(e.target.value)}
                        required
                    />
                    {previousTime && `Previously ${previousTime}`}
                </div>

                <div>
                    <label>Venue ID</label>
                    <input
                        type="text"
                        value={venueId}
                        onChange={(e) => setVenueId(e.target.value)}
                        required
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading
                        ? "Saving..."
                        : !isCreate
                          ? "Update Event"
                          : "Create Event"}
                </button>
            </form>
        </div>
    );
}

export default ModifyClubEvent;
