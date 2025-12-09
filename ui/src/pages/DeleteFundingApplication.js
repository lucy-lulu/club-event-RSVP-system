import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../App";
import { notification, Button, Spin } from "antd";

const DeleteFundingApplication = () => {
    const [clubs, setClubs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    // Fetch clubs where the user is admin and their funding applications
    const fetchAdminClubs = async () => {
        setLoading(true);
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/get-admin-clubs`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(
                    `Error fetching Clubs. Error: ${error.error || error.message}`
                );
            }

            const clubData = await response.json();
            console.log("Club Data: ", clubData);
            const clubWithApplications = await Promise.all(
                clubData.map(async (club) => {
                    const applicationsResponse = await fetch(
                        `${API_BASE_URL}/get-funding-by-club?club_id=${club.club_id}`,
                        {
                            headers: {
                                "Content-Type": "application/json",
                                Authorization: `Bearer ${token}`,
                            },
                        }
                    );

                    if (!applicationsResponse.ok) {
                        const error = await response.json();
                        throw new Error(
                            `Error fetching Applications. Error: ${error.error || error.message}`
                        );
                    }

                    const applicationsData = await applicationsResponse.json();
                    console.log("Applications Data: ", applicationsData);
                    return {
                        ...club,
                        applications: applicationsData,
                    };
                })
            );
            setClubs(clubWithApplications);
        } catch (error) {
            console.error(
                "Error fetching clubs and funding applications:",
                error
            );
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching clubs and funding applications " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
        }
    };

    // Function to delete a funding application
    const deleteApplication = async (application_id) => {
        if (!application_id) {
            console.error("Application ID is undefined or null");
            notification.error({
                message: "Error",
                description:
                    "Invalid application ID. Cannot delete application.",
            });
            return;
        }

        console.log("Deleting application with ID:", application_id);

        setLoading(true);
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/delete-funding`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    application_id: application_id,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                console.error("Error response data:", errorData);
                throw new Error(
                    error.error ||
                        error.message ||
                        "Failed to delete the funding application"
                );
            }

            notification.success({
                message: "Success",
                description: "Funding application deleted successfully!",
            });
            fetchAdminClubs(); // Refresh the list after deletion
        } catch (error) {
            console.error("Error deleting application:", error);
            notification.error({
                message: "Error",
                description:
                    "Failed to delete the funding application. " + error,
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAdminClubs();
    }, []);

    if (loading) {
        return <Spin />;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div>
            <h1>Delete Funding Applications</h1>
            {clubs.length > 0 ? (
                clubs.map((club, clubIndex) => (
                    <div key={clubIndex}>
                        <h2>{club.club_name}</h2>
                        <ul>
                            {club.applications &&
                            club.applications.length > 0 ? (
                                club.applications.map(
                                    (application, appIndex) => {
                                        console.log(
                                            "Application ID:",
                                            application.application_id
                                        ); // Add this line for debugging
                                        return (
                                            <li key={appIndex}>
                                                <h3>
                                                    Semester:{" "}
                                                    {application.semester}
                                                </h3>
                                                <p>
                                                    Amount: {application.amount}
                                                </p>
                                                <p>
                                                    Description:{" "}
                                                    {application.description}
                                                </p>{" "}
                                                <p>
                                                    Status: {application.status}
                                                </p>
                                                <Button
                                                    type="primary"
                                                    danger
                                                    onClick={() =>
                                                        deleteApplication(
                                                            application.application_id
                                                        )
                                                    }
                                                >
                                                    Delete
                                                </Button>
                                            </li>
                                        );
                                    }
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
};

export default DeleteFundingApplication;
