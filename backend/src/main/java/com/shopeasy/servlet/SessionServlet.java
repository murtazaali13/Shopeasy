package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/session")
public class SessionServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

        HttpSession session = request.getSession(false);
        boolean signedIn = session != null
                && session.getAttribute("userId") != null;

        if (!signedIn) {
            writeJson(response, Map.of("authenticated", false));
            return;
        }

        Object name = session.getAttribute("userName");
        writeJson(response, Map.of(
                "authenticated", true,
                "fullName", String.valueOf(name)
        ));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        writeJson(response, Map.of(
                "authenticated", false,
                "message", "You are signed out."
        ));
    }

    private static void prepareResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private static void writeJson(
            HttpServletResponse response, Map<String, ?> message)
            throws IOException {
        JSON.writeValue(response.getWriter(), message);
    }
}
