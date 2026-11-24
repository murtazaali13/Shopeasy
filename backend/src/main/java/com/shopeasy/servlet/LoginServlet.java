package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonNode body;
        try {
            body = JSON.readTree(request.getInputStream());
        } catch (IOException exception) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "The request was not valid JSON.");
            return;
        }

        if (body == null || !body.isObject()) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter your email and password.");
            return;
        }

        String email = body.path("email").asText("")
                .trim()
                .toLowerCase(Locale.ROOT);
        String password = body.path("password").asText("");

        if (email.isEmpty() || password.isEmpty()) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Please enter your email and password.");
            return;
        }

        String sql = """
                SELECT id, full_name, password_hash, role
                FROM users
                WHERE email = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    sendMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Email or password is incorrect.");
                    return;
                }

                long userId = results.getLong("id");
                String fullName = results.getString("full_name");
                String storedHash = results.getString("password_hash");
                String userRole = results.getString("role");

                boolean passwordMatches;
                try {
                    passwordMatches = verifyPassword(password, storedHash);
                } catch (GeneralSecurityException exception) {
                    log("Could not check the login password.", exception);
                    sendMessage(response,
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Could not sign in. Please try again.");
                    return;
                }

                if (!passwordMatches) {
                    sendMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "Email or password is incorrect.");
                    return;
                }

                HttpSession session = request.getSession(true);
                request.changeSessionId();
                session.setAttribute("userId", userId);
                session.setAttribute("userName", fullName);
                session.setAttribute("userRole", userRole);
                session.setMaxInactiveInterval(30 * 60);

                response.setStatus(HttpServletResponse.SC_OK);
                JSON.writeValue(response.getWriter(), Map.of(
                        "message", "Login successful.",
                        "fullName", fullName
                ));
            }

        } catch (SQLException exception) {
            log("Could not look up the login account.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not sign in. Please try again.");
        }
    }

    private static boolean verifyPassword(String password, String storedHash)
            throws GeneralSecurityException {
        if (storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            return false;
        }

        if (iterations < 10_000 || iterations > 1_000_000) {
            return false;
        }

        byte[] salt;
        byte[] expectedHash;
        try {
            salt = Base64.getDecoder().decode(parts[2]);
            expectedHash = Base64.getDecoder().decode(parts[3]);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        if (salt.length == 0 || expectedHash.length == 0) {
            return false;
        }

        PBEKeySpec keySpec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                expectedHash.length * 8
        );

        try {
            byte[] actualHash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();

            return MessageDigest.isEqual(expectedHash, actualHash);
        } finally {
            keySpec.clearPassword();
        }
    }

    private static void sendMessage(
            HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        JSON.writeValue(response.getWriter(), Map.of("message", message));
    }
}