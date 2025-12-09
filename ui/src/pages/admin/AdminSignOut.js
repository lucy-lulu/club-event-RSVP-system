import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

function AdminSignout() {
    const navigate = useNavigate();

    useEffect(() => {
        // Clear user token and other relevant data
        localStorage.removeItem("admin");

        // Redirect to login page after logout
        navigate("/dashboard");
    }, []);

    return (
        <div>
            <h1>Signing out as Admin...</h1>
        </div>
    );
}

export default AdminSignout;
