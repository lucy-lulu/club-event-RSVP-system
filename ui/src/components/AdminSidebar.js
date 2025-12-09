import React from "react";
import { Layout, Menu } from "antd";
import { Link, Navigate } from "react-router-dom";
import { DashboardOutlined, TeamOutlined } from "@ant-design/icons";

const { Sider } = Layout;
const { SubMenu } = Menu;

const AdminSidebar = () => {
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
                <SubMenu key="2" title="Manage Admins">
                    <Menu.Item key="2-1">
                        <Link to="/club/admin">View Admins</Link>
                    </Menu.Item>
                    <Menu.Item key="2-2">
                        <Link to="/club/admin/add">Add Admins</Link>
                    </Menu.Item>
                </SubMenu>
                <SubMenu key="3" title="Events">
                    <Menu.Item key="3-1">
                        <Link to="/club/event">View Events</Link>
                    </Menu.Item>
                    <Menu.Item key="3-2">
                        <Link to="/club/event/create">Create Event</Link>
                    </Menu.Item>
                </SubMenu>
                <SubMenu key="4" title="Funding Applications">
                    <Menu.Item key="4-1">
                        <Link to="/club/funding">View</Link>
                    </Menu.Item>
                    <Menu.Item key="4-2">
                        <Link to="/club/funding/create">Create</Link>
                    </Menu.Item>
                    <Menu.Item key="4-3">
                        <Link to="/club/funding/edit">Edit</Link>{" "}
                        {/* Add Edit Options */}
                    </Menu.Item>
                    <Menu.Item key="4-4">
                        <Link to="/club/funding/delete">Delete</Link>
                    </Menu.Item>
                </SubMenu>
                <Menu.Item key="5">
                    <Link to="/signout">Sign out of Admin View</Link>
                </Menu.Item>
            </Menu>
        </Sider>
    );
};

export default AdminSidebar;
