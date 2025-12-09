import React from "react";
import { Layout, Avatar, Badge } from "antd";
import { BellOutlined } from "@ant-design/icons";
//import './index.css';

const { Header } = Layout;

const AppHeader = () => {
    return (
        <Header className="header">
            <div className="logo">
                <h1 style={{ color: "white" }}>
                    Club & Event Management System
                </h1>
            </div>
            <div className="header-right">
                {/*<Badge count={11}>*/}
                {/*    <BellOutlined style={{ fontSize: '16px', color: 'white' }} />*/}
                {/*</Badge>*/}
                {/*<Avatar*/}
                {/*    style={{ marginLeft: 20 }}*/}
                {/*    src="https://randomuser.me/api/portraits/men/32.jpg"*/}
                {/*/>*/}
            </div>
        </Header>
    );
};

export default AppHeader;
