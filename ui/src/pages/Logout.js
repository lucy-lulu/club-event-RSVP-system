import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

function Logout() {
    const navigate = useNavigate();

    useEffect(() => {
        // Clear user token and other relevant data
        localStorage.removeItem("token");

        // Optionally, clear other user-specific data
        localStorage.removeItem("email");

        // Redirect to login page after logout
        navigate("/");
    }, []);

    return (
        <div>
            <h1>Logging out...</h1>
        </div>
    );
}

export default Logout;
