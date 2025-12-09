import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../App";

function FundingApplications() {
    const [clubs, setClubs] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    // Fetch clubs where the user is admin and their funding applications
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
                throw new Error(
                    `Failed to fetch clubs. Error: ${error.error || error.message}`
                );
            }

            const clubData = await response.json(); // Parse the response as JSON
            console.log("Club Data: ", clubData);
            const clubWithApplications = await Promise.all(
                clubData.map(async (club) => {
                    const applicationsResponse = await fetch(
                        `${API_BASE_URL}/get-funding-by-club?club_id=${club.club_id}`,
                        {
                            headers: {
                                Authorization: `Bearer ${token}`,
                            },
                        }
                    );

                    if (!applicationsResponse.ok) {
                        const error = await applicationsResponse.json();
                        throw new Error(
                            `Failed to fetch applications. Error: ${error.error || error.message}`
                        );
                    }

                    const applicationsData = await applicationsResponse.json(); // Parse as JSON
                    console.log("Applications Data: ", applicationsData);
                    return {
                        ...club,
                        applications: applicationsData, // Ensure this is an array
                    };
                })
            );
            console.log(clubWithApplications);
            setClubs(clubWithApplications);
            const sortedClubs = clubWithApplications.map((club) => ({
                ...club,
                applications: club.applications.sort(
                    (a, b) => a.application_id - b.application_id
                ), // Sort by application_id or created_at
            }));

            setClubs(sortedClubs);
        } catch (error) {
            console.error(
                "Error fetching clubs and funding applications:",
                error
            );
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching clubs and funding applications. " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
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
            <h1>Funding Applications for Clubs</h1>
            {clubs.length > 0 ? (
                clubs.map((club, clubIndex) => (
                    <div key={clubIndex}>
                        <h2>{club.club_name}</h2>
                        <ul>
                            {club.applications &&
                            club.applications.length > 0 ? (
                                club.applications.map(
                                    (application, appIndex) => (
                                        <li key={appIndex}>
                                            <h3>
                                                Semester: {application.semester}
                                            </h3>
                                            <p>Amount: {application.amount}</p>
                                            <p>
                                                Description:{" "}
                                                {application.description}
                                            </p>{" "}
                                            <p>Status: {application.status}</p>
                                        </li>
                                    )
                                )
                            ) : (
                                <p>No applications available for this club</p>
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

export default FundingApplications;
