import React from "react";
import { Layout, Menu } from "antd";
import { Link, Navigate } from "react-router-dom";
import {
    DashboardOutlined,
    TeamOutlined,
    MoneyCollectOutlined,
} from "@ant-design/icons";

const { Sider } = Layout;
const { SubMenu } = Menu;

const FacultySidebar = () => {
    const token = localStorage.getItem("token"); // Get token from localStorage

    if (!token) {
        return <Navigate to="/" />; // If not logged in, redirect to login
    }

    return (
        <Sider>
            <div className="logo" />
            <Menu theme="dark" mode="inline">
                <Menu.Item key="1" icon={<DashboardOutlined />}>
                    <Link to="/dashboard">Dashboard</Link>
                </Menu.Item>
                <Menu.Item key="2" icon={<MoneyCollectOutlined />}>
                    <Link to="/faculty/view">View Funding Applications</Link>
                </Menu.Item>
                <Menu.Item key="3">
                    <Link to="/signout">Sign out of Admin View</Link>
                </Menu.Item>
            </Menu>
        </Sider>
    );
};

export default FacultySidebar;
