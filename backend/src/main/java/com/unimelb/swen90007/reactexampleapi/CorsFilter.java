package com.unimelb.swen90007.reactexampleapi;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {
    private String allowedOrigin;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        allowedOrigin = System.getProperty("cors.allowedOrigin", "http://localhost:3000");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        res.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.setHeader("Access-Control-Max-Age", "3600");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
