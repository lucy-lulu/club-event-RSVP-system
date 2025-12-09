import React, { useState } from "react";
import { API_BASE_URL } from "../App";
import { useLocation } from "react-router-dom";

function RSVPPage() {
    const [attendees, setAttendees] = useState([]);
    const [errorMessage, setErrorMessage] = useState(null); // For error handling
    const [successMessage, setSuccessMessage] = useState(null); // For success handling
    const location = useLocation();
    const searchParams = new URLSearchParams(location.search);
    const event_id = searchParams.get("eventId");

    const handleSubmit = async (event) => {
        event.preventDefault();
        setErrorMessage(null); // Reset error message
        setSuccessMessage(null); // Reset success message

        const token = localStorage.getItem("token");

        try {
            // Check if event_id is valid
            if (!event_id) {
                throw new Error("Event ID is missing");
            }

            // Filter out empty attendee emails
            const validAttendees = attendees.filter((email) => email);

            // Send RSVP requests for all attendees
            const responses = await Promise.all(
                validAttendees.map(async (attendee) => {
                    const response = await fetch(`${API_BASE_URL}/rsvp-event`, {
                        method: "POST",
                        body: JSON.stringify({
                            event_id: event_id,
                            attendee_email: attendee,
                        }),
                        headers: {
                            Authorization: `Bearer ${token}`,
                            "Content-Type": "application/json",
                        },
                    });

                    if (!response.ok) {
                        const errorData = await response.json();
                        throw new Error(
                            `Error: ${
                                errorData.error ||
                                errorData.message ||
                                "Failed to RSVP"
                            }`
                        );
                    }

                    return response.json();
                })
            );

            console.log("RSVP Responses:", responses);
            setSuccessMessage(
                "RSVPs successfully submitted for all attendees."
            );
        } catch (error) {
            console.error("Error sending RSVPs:", error);
            const errorMessage = String(error).split("Error:");
            setErrorMessage(
                "Error submitting RSVPs. Please try again. " +
                    errorMessage[errorMessage.length - 1]
            );
        }
    };

    const handleInputChange = (index, event) => {
        const newAttendees = [...attendees];
        newAttendees[index] = event.target.value;
        setAttendees(newAttendees);
    };

    const handleAddAttendee = () => {
        setAttendees([...attendees, ""]);
    };

    const handleRemoveAttendee = (index) => {
        const updatedAttendees = [...attendees];
        updatedAttendees.splice(index, 1);
        setAttendees(updatedAttendees);
    };

    return (
        <form onSubmit={handleSubmit}>
            <h2>Enter Emails of Attendees</h2>

            {attendees.map((student, index) => (
                <div key={index}>
                    <input
                        type="email"
                        placeholder="Enter Attendee Email"
                        value={student}
                        onChange={(event) => handleInputChange(index, event)}
                        required
                    />
                    {attendees.length > 1 && (
                        <button
                            type="button"
                            onClick={() => handleRemoveAttendee(index)}
                        >
                            Remove
                        </button>
                    )}
                </div>
            ))}

            <button type="button" onClick={handleAddAttendee}>
                Add Another Attendee
            </button>

            <button
                type="submit"
                disabled={!event_id || attendees.length === 0}
            >
                Submit RSVP
            </button>

            {errorMessage && <p className="error">{errorMessage}</p>}
            {successMessage && <p className="success">{successMessage}</p>}
        </form>
    );
}

export default RSVPPage;
