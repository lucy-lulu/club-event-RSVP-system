import React, { useState } from "react";
import { API_BASE_URL } from "../../App";

function AddAdmin() {
    const [studentEmail, setStudentEmail] = useState("");
    const [clubName, setClubName] = useState(""); // Declare the clubName state
    const [message, setMessage] = useState("");

    const handleAddAdmin = async () => {
        try {
            const token = localStorage.getItem("token");
            const response = await fetch(`${API_BASE_URL}/add-admin`, {
                method: "POST",
                body: JSON.stringify({
                    student_email: studentEmail,
                    club_name: clubName,
                }),
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(`Error: ${error.error || error.message}`);
            }

            setMessage("Admin added successfully.");
        } catch (error) {
            console.log(error);
            const errorMessage = String(error).split("Error:");
            setMessage(
                "Failed to add admin. " + errorMessage[errorMessage.length - 1]
            );
        }
    };

    return (
        <div>
            <h1>Add Admin</h1>

            <div>
                <label>
                    Student Email:
                    <input
                        type="email"
                        value={studentEmail}
                        onChange={(e) => setStudentEmail(e.target.value)}
                        placeholder="Enter student email"
                    />
                </label>
            </div>

            <div>
                <label>
                    Club Name:
                    <input
                        type="text"
                        value={clubName}
                        onChange={(e) => setClubName(e.target.value)} // Update clubName state
                        placeholder="Enter club name"
                    />
                </label>
            </div>

            <button onClick={handleAddAdmin}>Add Admin</button>

            {message && <p>{message}</p>}
        </div>
    );
}

export default AddAdmin;
