import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter as Router } from "react-router-dom";
import App from "./App";

// 获取根元素
const container = document.getElementById("root");

// 创建根实例
const root = createRoot(container);

// 渲染应用
root.render(
    <Router>
        <App />
    </Router>
);
