package com.shopeasy.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter("/api/admin/*")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            sendError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Please sign in first.");
            return;
        }

        Object role = session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) {
            sendError(httpResponse, HttpServletResponse.SC_FORBIDDEN,
                    "Admin access is required.");
            return;
        }

        chain.doFilter(request, response);
    }

    private static void sendError(
            HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"message\":\"" + message + "\"}");
    }
}