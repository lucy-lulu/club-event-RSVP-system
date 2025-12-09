import React from "react";
import { Layout, Menu } from "antd";
import { Link, Navigate } from "react-router-dom";
import {
    DashboardOutlined,
    CalendarOutlined,
    UsergroupAddOutlined,
    TeamOutlined,
} from "@ant-design/icons";

const { Sider } = Layout;
const { SubMenu } = Menu;

const StudentSidebar = () => {
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
                <SubMenu key="2" icon={<CalendarOutlined />} title="Events">
                    <Menu.Item key="2-1">
                        <Link to="/events/upcoming">Upcoming</Link>
                    </Menu.Item>
                    <Menu.Item key="2-2">
                        <Link to="/events/past">Past</Link>
                    </Menu.Item>
                </SubMenu>
                <SubMenu
                    key="3"
                    icon={<UsergroupAddOutlined />}
                    title="My Tickets"
                >
                    <Menu.Item key="3-1">
                        <Link to="/rsvp/attendee">My Tickets</Link>
                    </Menu.Item>
                    <Menu.Item key="3-2">
                        <Link to="/rsvp/applicant">Applied Tickets</Link>
                    </Menu.Item>
                    {/* <SubMenu key="3-2" title="Cancellations">
                        <Menu.Item key="3-2-1">
                            <Link to="/rsvp/cancel/applicant">
                                Applicant View
                            </Link>
                        </Menu.Item>
                        {/* <Menu.Item key="3-2-2">
                            <Link to="/rsvp/cancel/attendee">
                                Attendee View
                            </Link>
                        </Menu.Item> *
                    </SubMenu> */}
                </SubMenu>
                <Menu.Item key="4" icon={<TeamOutlined />}>
                    <Link to="/club">Club Administration</Link>
                </Menu.Item>
                <Menu.Item key="5">
                    <Link to="/logout">Logout</Link>
                </Menu.Item>
            </Menu>
        </Sider>
    );
};

export default StudentSidebar;
