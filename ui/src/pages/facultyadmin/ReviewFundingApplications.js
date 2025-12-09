import React, { useState, useEffect } from "react";
import { API_BASE_URL } from "../../App";
import Pagination from "../../components/Pagination";

function ReviewAllFundingApplications() {
    const [applications, setApplications] = useState([]);
    const [totalApplications, setTotalApplications] = useState(0); // Total number of applications
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const pageSize = 15;

    // Fetch funding applications based on the current page
    const fetchFundingApplications = async (page) => {
        setLoading(true);
        setError("");
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(
                `${API_BASE_URL}/get-paginated-funding?page=${page}&pageSize=${pageSize}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                const error = await response.json();
                throw new Error(
                    `Failed to fetch applications. Error: ${error.error || error.message}`
                );
            }

            const data = await response.json();
            console.log(data);
            if (data && data.totalCount > 0) {
                setApplications(data.applications);
                setTotalApplications(data.totalCount);
            }
        } catch (error) {
            console.error("Error fetching funding applications:", error);
            setError("Error fetching funding applications.");
        } finally {
            setLoading(false);
        }
    };

    // Approve funding application
    const handleApprove = async (application) => {
        try {
            console.log(
                "id",
                application.application_id,
                "version",
                application.version
            );
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/admin-approved`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    application_id: application.application_id,
                    version: application.version, // Send the correct version
                }),
            });

            if (response.ok) {
                // Update the application status in the UI
                const updatedApplications = applications.map((app) =>
                    app.application_id === application.application_id
                        ? { ...app, status: "approved" }
                        : app
                );
                setApplications(updatedApplications);
            } else if (response.status === 409) {
                // Handle conflict error
                throw new Error(
                    "Conflict detected. The funding application has been modified by another user."
                );
            }
        } catch (error) {
            console.error("Error approving funding application:", error);
            setError("Error approving funding application: " + error.message);
        }
    };

    // Disapprove funding application
    const handleDisapprove = async (application) => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/admin-disapproved`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    application_id: application.application_id,
                    version: application.version, // Send the correct version
                }),
            });

            if (response.ok) {
                // Update the application status in the UI
                const updatedApplications = applications.map((app) =>
                    app.application_id === application.application_id
                        ? { ...app, status: "disapproved" }
                        : app
                );
                setApplications(updatedApplications);
            } else if (response.status === 409) {
                // Handle conflict error
                throw new Error(
                    "Conflict detected. The funding application has been modified by another user."
                );
            }
        } catch (error) {
            console.error("Error disapproving funding application:", error);
            setError(
                "Error disapproving funding application: " + error.message
            );
        }
    };

    // Initial data fetch
    useEffect(() => {
        fetchFundingApplications(1); // Fetch data for the first page on component load
    }, []);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div>
            <h1>Funding applications</h1>
            <table>
                <thead>
                    <tr>
                        <th>Club ID</th>
                        <th>Amount</th>
                        <th>Last Updated</th>
                        <th>Semester</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                    {applications.map((application) => (
                        <tr key={application.application_id}>
                            <td>{application.clubId}</td>
                            <td>{application.amount}</td>
                            <td>{application.date}</td>
                            <td>{application.semester}</td>
                            <td>{application.status}</td>
                            <td>
                                <button
                                    onClick={() => handleApprove(application)}
                                    disabled={
                                        application.status !== "submitted"
                                    }
                                >
                                    Approve
                                </button>
                                <button
                                    onClick={() =>
                                        handleDisapprove(application)
                                    }
                                    disabled={
                                        application.status !== "submitted"
                                    }
                                >
                                    Disapprove
                                </button>
                            </td>
                        </tr>
                    ))}
                </thead>
            </table>

            {/* Pagination Component */}
            <Pagination
                totalItems={totalApplications} // Total items count
                pageSize={pageSize} // Number of items per page
                onPageChange={fetchFundingApplications} // Handle page changes
            />
        </div>
    );
}

export default ReviewAllFundingApplications;
