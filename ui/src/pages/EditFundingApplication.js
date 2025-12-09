import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../App";
import { notification, Button, Spin, Input, Alert } from "antd";
import { jwtDecode } from "jwt-decode";

const EditFundingApplication = () => {
    const [clubs, setClubs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [editingApplication, setEditingApplication] = useState(null); // Track editing state
    const [formValues, setFormValues] = useState({
        amount: "",
        description: "",
        semester: "",
        status: "",
    });
    const [lockError, setLockError] = useState(""); // To track lock errors

    // Define fetchAdminClubs inside the component
    const fetchAdminClubs = async () => {
        setLoading(true);
        setError("");
        setLockError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/get-admin-clubs`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                try {
                    const errorData = await response.json();
                    if (
                        errorData.message &&
                        errorData.message.includes("locked")
                    ) {
                        setLockError(
                            "This funding application is currently being edited by another admin."
                        );
                    }
                    throw new Error(
                        errorData.error ||
                            errorData.message ||
                            "Failed to edit the funding application"
                    );
                } catch (jsonError) {
                    throw new Error(
                        "Failed to process error response from server."
                    );
                }
            }

            const clubData = await response.json();
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
                        const error = await applicationsResponse.json();
                        throw new Error(
                            `Error fetching Applications. Error: ${error.error || error.message}`
                        );
                    }

                    const applicationsData = await applicationsResponse.json();
                    return {
                        ...club,
                        applications: applicationsData,
                    };
                })
            );
            setClubs(clubWithApplications);
            const sortedClubs = clubWithApplications.map((club) => ({
                ...club,
                applications: club.applications.sort(
                    (a, b) => a.application_id - b.application_id
                ), // Sort by application_id or created_at
            }));

            setClubs(sortedClubs);
        } catch (error) {
            const errorMessage = String(error).split("Error:");
            setError(
                "Error fetching clubs and funding applications " +
                    errorMessage[errorMessage.length - 1]
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAdminClubs(); // Use the defined fetchAdminClubs function here
    }, []);
    // Function to fetch the latest funding application data from the backend
    const fetchLatestFundingApplication = async (application_id) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(
                `${API_BASE_URL}/get-funding-by-id?application_id=${application_id}`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error(
                    "Failed to fetch the latest funding application data."
                );
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error("Error fetching latest funding application:", error);
            setError("Error fetching the latest funding application data.");
            return null;
        }
    };

    // Function to lock a funding application
    const lockApplication = async (application_id, club_id) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/lock-funding`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    application_id: application_id,
                    club_id: club_id,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                console.error("Error locking application:", errorData);
                throw new Error(
                    errorData.error ||
                        errorData.message ||
                        "Failed to lock the funding application"
                );
            }

            return true; // If lock succeeds, return true
        } catch (error) {
            setLockError(
                "This funding application is currently being edited by another admin."
            );
            return false; // If lock fails, return false
        }
    };

    // Function to unlock a funding application
    const unlockApplication = async (application_id, club_id) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/unlock-funding`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    application_id: application_id,
                    club_id: club_id,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error("Failed to unlock the funding application.");
            }

            return true;
        } catch (error) {
            console.error("Failed to unlock the funding application.", error);
            return false;
        }
    };

    // Function to edit a funding application
    const editApplication = async (application_id) => {
        if (!application_id) {
            notification.error({
                message: "Error",
                description: "Invalid application ID. Cannot edit application.",
            });
            return;
        }

        setLoading(true);
        setLockError(""); // Reset any previous lock errors
        try {
            const token = localStorage.getItem("token");

            const currentClub = clubs.find((club) =>
                club.applications.some(
                    (app) => app.application_id === application_id
                )
            );

            if (!currentClub || !currentClub.club_id) {
                throw new Error("club_id is missing or undefined.");
            }

            const response = await fetch(`${API_BASE_URL}/edit-funding`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    application_id: application_id,
                    amount: formValues.amount,
                    description: formValues.description,
                    semester: formValues.semester,
                    status: formValues.status,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                if (errorData.message && errorData.message.includes("locked")) {
                    setLockError(
                        "This funding application is currently being edited by another admin."
                    );
                }
                throw new Error(
                    errorData.error ||
                        errorData.message ||
                        "Failed to edit the funding application"
                );
            }

            notification.success({
                message: "Success",
                description: "Funding application edited successfully!",
            });
            fetchAdminClubs(); // Refresh the list after editing
            setEditingApplication(null); // Exit edit mode after successful edit

            await unlockApplication(application_id, currentClub.club_id);
        } catch (error) {
            notification.error({
                message: "Error",
                description: "Failed to edit the funding application. " + error,
            });
        } finally {
            setLoading(false);
        }
    };

    const startEditing = async (application, club_id) => {
        const token = localStorage.getItem("token");
        const decodedToken = jwtDecode(token);
        const currentUserId = decodedToken.sub;

        if (application.locked_by && application.locked_by === currentUserId) {
            setEditingApplication(application.application_id);
            setFormValues({
                amount: application.amount,
                description: application.description,
                semester: application.semester,
                status: application.status,
            });
            return;
        }

        // Otherwise, proceed with the lock operation
        const lockSuccess = await lockApplication(
            application.application_id,
            club_id
        );
        if (!lockSuccess) return; // If locking fails, exit

        // Fetch the latest funding application data
        const latestApplicationData = await fetchLatestFundingApplication(
            application.application_id
        );
        if (latestApplicationData) {
            setEditingApplication(application.application_id);
            setFormValues({
                amount: latestApplicationData.amount,
                description: latestApplicationData.description,
                semester: latestApplicationData.semester,
                status: latestApplicationData.status,
            });
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormValues({ ...formValues, [name]: value });
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
            <h1>Edit Funding Applications</h1>
            {lockError && (
                <Alert
                    message="Error"
                    description={lockError}
                    type="error"
                    showIcon
                />
            )}
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
                                            </p>
                                            <p>Status: {application.status}</p>

                                            {editingApplication ===
                                            application.application_id ? (
                                                <>
                                                    <Input
                                                        name="amount"
                                                        value={
                                                            formValues.amount
                                                        }
                                                        onChange={
                                                            handleInputChange
                                                        }
                                                        placeholder="Amount"
                                                        disabled={loading}
                                                    />
                                                    <Input
                                                        name="description"
                                                        value={
                                                            formValues.description
                                                        }
                                                        onChange={
                                                            handleInputChange
                                                        }
                                                        placeholder="Description"
                                                        disabled={loading}
                                                    />
                                                    <Input
                                                        name="semester"
                                                        value={
                                                            formValues.semester
                                                        }
                                                        onChange={
                                                            handleInputChange
                                                        }
                                                        placeholder="Semester"
                                                        disabled={loading}
                                                    />
                                                    <Input
                                                        name="status"
                                                        value={
                                                            formValues.status
                                                        }
                                                        onChange={
                                                            handleInputChange
                                                        }
                                                        placeholder="Status"
                                                        disabled={loading}
                                                    />
                                                    <Button
                                                        type="primary"
                                                        onClick={() =>
                                                            editApplication(
                                                                application.application_id
                                                            )
                                                        }
                                                        disabled={loading}
                                                    >
                                                        {loading
                                                            ? "Saving..."
                                                            : "Save"}
                                                    </Button>
                                                </>
                                            ) : (
                                                <>
                                                    <Button
                                                        type="primary"
                                                        onClick={() =>
                                                            startEditing(
                                                                application,
                                                                club.club_id
                                                            )
                                                        }
                                                    >
                                                        Edit
                                                    </Button>
                                                </>
                                            )}
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
};

export default EditFundingApplication;
