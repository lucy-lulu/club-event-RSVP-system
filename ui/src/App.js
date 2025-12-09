// src/App.js
import React from "react";
import { Layout } from "antd";
import { useLocation } from "react-router-dom";
import AppHeader from "./components/header";
import StudentSidebar from "./components/StudentSidebar";
import AdminSidebar from "./components/AdminSidebar";
import FacultySidebar from "./components/FacultySidebar";
import AppRouter from "./router"; // Import the new router file

const { Content } = Layout;
export const API_BASE_URL = process.env.REACT_APP_API_URL;

function App() {
    const location = useLocation();
    const faculty = localStorage.getItem("faculty");
    const admin = localStorage.getItem("admin");

    // If the user is on the login page, we don't show the layout
    const isLoginPage = location.pathname === "/";

    return (
        <>
            {isLoginPage ? (
                <AppRouter />
            ) : (
                <Layout style={{ minHeight: "100vh" }}>
                    {faculty === "true" ? (
                        <FacultySidebar />
                    ) : admin === "true" ? (
                        <AdminSidebar />
                    ) : (
                        <StudentSidebar />
                    )}
                    <Layout className="site-layout">
                        <AppHeader />
                        <Content style={{ margin: "16px" }}>
                            <AppRouter />
                        </Content>
                    </Layout>
                </Layout>
            )}
        </>
    );
}

export default App;
