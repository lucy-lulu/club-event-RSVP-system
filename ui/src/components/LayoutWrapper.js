import React from "react";
import { Layout } from "antd";
import { Outlet } from "react-router-dom";
import AppHeader from "./header";
import Sidebar from "./sidebar";

const { Content } = Layout;

const LayoutWrapper = () => {
    return (
        <Layout style={{ minHeight: "100vh" }}>
            <Sidebar />
            <Layout className="site-layout">
                <AppHeader />
                <Content
                    style={{
                        margin: "24px 16px",
                        padding: 24,
                        background: "#fff",
                    }}
                >
                    <Outlet /> {/* This will render the child route */}
                </Content>
            </Layout>
        </Layout>
    );
};

export default LayoutWrapper;
