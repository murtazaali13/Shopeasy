package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int HASH_ITERATIONS = 120_000;
    private static final int HASH_LENGTH = 256;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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
                    "Please provide your registration details.");
            return;
        }

        String fullName = body.path("fullName").asText("").trim();
        String email = body.path("email").asText("").trim().toLowerCase(Locale.ROOT);
        String password = body.path("password").asText("");

        if (fullName.isEmpty() || fullName.length() > 120) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter your name (up to 120 characters).");
            return;
        }

        if (email.length() > 255 || !EMAIL_PATTERN.matcher(email).matches()) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a valid email address.");
            return;
        }

        if (password.length() < 8 || password.length() > 128) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Your password must be between 8 and 128 characters.");
            return;
        }

        String passwordHash;
        try {
            passwordHash = hashPassword(password);
        } catch (GeneralSecurityException exception) {
            log("Could not protect the registration password.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not create the account. Please try again.");
            return;
        }

        String sql = """
                INSERT INTO users (full_name, email, password_hash)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.executeUpdate();

            sendMessage(response, HttpServletResponse.SC_CREATED,
                    "Registration successful.");

        } catch (SQLIntegrityConstraintViolationException exception) {
            sendMessage(response, HttpServletResponse.SC_CONFLICT,
                    "An account with that email already exists.");
        } catch (SQLException exception) {
            log("Could not save the registered user.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not create the account. Please try again.");
        }
    }

    private static String hashPassword(String password)
            throws GeneralSecurityException {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        PBEKeySpec keySpec =
                new PBEKeySpec(password.toCharArray(), salt, HASH_ITERATIONS, HASH_LENGTH);

        try {
            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(keySpec)
                    .getEncoded();

            Base64.Encoder encoder = Base64.getEncoder();
            return "PBKDF2$"
                    + HASH_ITERATIONS + "$"
                    + encoder.encodeToString(salt) + "$"
                    + encoder.encodeToString(hash);
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