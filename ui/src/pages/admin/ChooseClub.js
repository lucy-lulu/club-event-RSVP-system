import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../../App";
import { useNavigate } from "react-router-dom";

function ChooseClub() {
    const [clubs, setClubs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null); // State to handle errors

    const navigate = useNavigate(); // Initialize navigation function

    const fetchClubs = async () => {
        setLoading(true);
        setError("");

        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/get-admin-clubs`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }
            const data = await response.json();
            setClubs(data);
            // Log the response data
            console.log("Response data:", data);
        } catch (error) {
            console.error("Error fetching clubs:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching clubs " + errorMessage[errorMessage.length - 1]
            ); // Set error message
        } finally {
            setLoading(false); // Hide loading once fetch is complete
        }
    };

    useEffect(() => {
        fetchClubs();
    }, []);

    const chooseClub = async (club_id) => {
        localStorage.setItem("admin", "true");
        localStorage.setItem("club", club_id);
        navigate(`/dashboard`);
    };

    // const handleRSVP = (eventId) => {
    //     // Redirect to the /rsvp page with eventId as a query parameter
    //     navigate(`/rsvp/get?eventId=${eventId}`);
    // };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div>
            <h1>Choose Club to Administrate</h1>
            {error && <div className="error">{error}</div>}{" "}
            {/* Display error if any */}
            <ul>
                {clubs && clubs.length > 0 ? (
                    clubs.map((club) => (
                        <li key={club.club_id}>
                            <h3>{club.club_name}</h3>
                            <button onClick={() => chooseClub(club.club_id)}>
                                Choose
                            </button>
                        </li>
                    ))
                ) : (
                    <p>No clubs available</p>
                )}
            </ul>
        </div>
    );
}

export default ChooseClub;
