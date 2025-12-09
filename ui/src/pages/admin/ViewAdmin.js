import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../../App";

function ViewAdmins() {
    const [clubs, setClubs] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    // Fetch clubs where the user is admin
    const fetchAdminClubs = async () => {
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

            const clubData = await response.json();
            console.log("Club Data: ", clubData);
            const clubWithAdmins = await Promise.all(
                clubData.map(async (club) => {
                    const adminsResponse = await fetch(
                        `${API_BASE_URL}/get-admins-by-club-id?club_id=${club.club_id}`,
                        {
                            headers: {
                                Authorization: `Bearer ${token}`,
                            },
                        }
                    );
                    if (!adminsResponse.ok) {
                        const error = await response.json();
                        throw new Error(
                            `Error: ${error.error || error.message}`
                        );
                    }
                    const data = await adminsResponse.json();
                    console.log("Admin Response: ", data);

                    return {
                        ...club,
                        admins: data,
                    };
                })
            );
            console.log("Club Admins: ", clubWithAdmins);
            setClubs(clubWithAdmins);
        } catch (error) {
            console.error("Error fetching clubs and admins:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching clubs and admins. " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
        }
    };

    // Handle Remove Admin
    const handleRemoveAdmin = async (studentEmail, clubId) => {
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/remove-admin`, {
                method: "POST",
                body: JSON.stringify({
                    student_email: studentEmail,
                    club_id: clubId,
                }),
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            // Refresh the list after successful removal
            fetchAdminClubs();
        } catch (error) {
            console.error("Error removing admin:", error);
            const errorMessage = String(error).split("Error:");
            setError(
                "Error removing admin. " + errorMessage[errorMessage.length - 1]
            );
        }
    };

    useEffect(() => {
        fetchAdminClubs();
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div>
            <h1>Admin View for Clubs</h1>
            {clubs.length > 0 ? (
                clubs.map((club, clubIndex) => (
                    <div key={clubIndex}>
                        <h2>{club.club_name}</h2>
                        <ul>
                            {club.admins.length > 0 ? (
                                club.admins.map((admin, adminIndex) => (
                                    <li key={adminIndex}>
                                        {admin.first_name} {admin.last_name} (
                                        {admin.email})
                                        <button
                                            style={{ marginLeft: "10px" }}
                                            onClick={() =>
                                                handleRemoveAdmin(
                                                    admin.email,
                                                    club.club_id
                                                )
                                            }
                                        >
                                            Remove Admin
                                        </button>
                                    </li>
                                ))
                            ) : (
                                <p>No admins available</p>
                            )}
                        </ul>
                    </div>
                ))
            ) : (
                <p>No clubs available</p>
            )}
        </div>
    );
}

export default ViewAdmins;
