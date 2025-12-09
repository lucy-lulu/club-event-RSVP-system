import React from "react";
import { Navigate } from "react-router-dom";

function Dashboard() {
    const token = localStorage.getItem("token"); // Get token from localStorage

    if (!token) {
        return <Navigate to="/" />; // If not logged in, redirect to login
    }

    return (
        <div>
            <h1>Welcome</h1>
        </div>
    );
}

export default Dashboard;
