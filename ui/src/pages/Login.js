import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { API_BASE_URL } from "../App.js";

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleLogin = async (e, userType) => {
        e.preventDefault();

        localStorage.removeItem("email");
        localStorage.removeItem("token");
        localStorage.removeItem("faculty");

        try {
            const loginPath = userType === "staff" ? "staff-login" : "login";
            const response = await fetch(`${API_BASE_URL}/${loginPath}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    email: email,
                    password: password,
                }),
            });

            if (response.ok) {
                const data = await response.json();
                const token = data.token;
                const user = data.user;

                // Store the token and email in localStorage
                localStorage.setItem("email", email);
                localStorage.setItem("token", token);
                console.log("User token from LocalStorage: ", token);
                console.log("User Status: ", user);
                if (user === "staff") localStorage.setItem("faculty", "true");
                // Redirect to dashboard after successful login
                navigate("/dashboard");
            } else {
                setError("Invalid email or password");
            }
        } catch (err) {
            console.error("Login error: ", err);
            setError("Something went wrong. Please try again later.");
        }
    };
    return (
        <div>
            <h1>Login</h1>
            <form>
                <div>
                    <label>Email:</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <div>
                    <label>Password:</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>

                <button onClick={(e) => handleLogin(e, "student")}>
                    Student Sign In
                </button>
                <button onClick={(e) => handleLogin(e, "staff")}>
                    Staff Sign In
                </button>
                {error && (
                    <div style={{ color: "red" }}>
                        <h2>{error}</h2>
                    </div>
                )}
            </form>
        </div>
    );
}

export default Login;
