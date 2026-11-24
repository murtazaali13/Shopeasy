package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/products")
public class AdminProductsServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

        String sql = """
                SELECT id, name, category, description, price,
                       stock_quantity, active
                FROM products
                ORDER BY id DESC
                """;

        List<Map<String, Object>> products = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                Map<String, Object> product = new LinkedHashMap<>();
                product.put("id", results.getLong("id"));
                product.put("name", results.getString("name"));
                product.put("category", results.getString("category"));
                product.put("description", results.getString("description"));
                product.put("price", results.getBigDecimal("price"));
                product.put("stockQuantity",
                        results.getInt("stock_quantity"));
                product.put("active", results.getBoolean("active"));
                products.add(product);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            JSON.writeValue(response.getWriter(),
                    Map.of("products", products));

        } catch (SQLException exception) {
            log("Could not load admin products.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load products. Please try again.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

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
                    "Please provide product details.");
            return;
        }

        String name = body.path("name").asText("").trim();
        String category = body.path("category").asText("").trim();
        String description = body.path("description").asText("").trim();
        JsonNode priceNode = body.path("price");
        JsonNode stockNode = body.path("stockQuantity");

        if (name.isEmpty() || name.length() > 120
                || category.isEmpty() || category.length() > 80
                || description.isEmpty() || description.length() > 500) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Check the product name, category, and description.");
            return;
        }

        if (!priceNode.isNumber() || priceNode.decimalValue().signum() <= 0) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a price greater than zero.");
            return;
        }

        if (!stockNode.canConvertToInt() || stockNode.asInt() < 0) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a stock quantity of zero or more.");
            return;
        }

        String sql = """
                INSERT INTO products
                    (name, category, description, price, stock_quantity, active)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, name);
            statement.setString(2, category);
            statement.setString(3, description);
            statement.setBigDecimal(4, priceNode.decimalValue());
            statement.setInt(5, stockNode.asInt());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("The product ID was not returned.");
                }

                long productId = generatedKeys.getLong(1);
                response.setStatus(HttpServletResponse.SC_CREATED);
                JSON.writeValue(response.getWriter(), Map.of(
                        "message", "Product added successfully.",
                        "productId", productId
                ));
            }

        } catch (SQLException exception) {
            log("Could not add admin product.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not add the product. Please try again.");
        }
    }

        @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

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
                    "Please provide product details.");
            return;
        }

        long id = body.path("id").asLong(-1);
        String name = body.path("name").asText("").trim();
        String category = body.path("category").asText("").trim();
        String description = body.path("description").asText("").trim();
        JsonNode price = body.path("price");
        JsonNode stock = body.path("stockQuantity");

        if (id <= 0
                || name.isEmpty() || name.length() > 120
                || category.isEmpty() || category.length() > 80
                || description.isEmpty() || description.length() > 500
                || !price.isNumber() || price.decimalValue().signum() <= 0
                || !stock.canConvertToInt() || stock.asInt() < 0) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Check the product name, category, description, price, and stock.");
            return;
        }

        String sql = """
                UPDATE products
                SET name = ?, category = ?, description = ?,
                    price = ?, stock_quantity = ?
                WHERE id = ? AND active = TRUE
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setString(3, description);
            statement.setBigDecimal(4, price.decimalValue());
            statement.setInt(5, stock.asInt());
            statement.setLong(6, id);

            if (statement.executeUpdate() == 0) {
                sendMessage(response, HttpServletResponse.SC_NOT_FOUND,
                        "Active product not found.");
                return;
            }

            sendMessage(response, HttpServletResponse.SC_OK,
                    "Product updated successfully.");
        } catch (SQLException exception) {
            log("Could not update admin product.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not update the product. Please try again.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

        JsonNode body;
        try {
            body = JSON.readTree(request.getInputStream());
        } catch (IOException exception) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "The request was not valid JSON.");
            return;
        }

        long id = body == null ? -1 : body.path("id").asLong(-1);
        if (id <= 0) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "A valid product ID is required.");
            return;
        }

        String sql = """
                UPDATE products
                SET active = FALSE
                WHERE id = ? AND active = TRUE
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            if (statement.executeUpdate() == 0) {
                sendMessage(response, HttpServletResponse.SC_NOT_FOUND,
                        "Active product not found.");
                return;
            }

            sendMessage(response, HttpServletResponse.SC_OK,
                    "Product deactivated successfully.");
        } catch (SQLException exception) {
            log("Could not deactivate admin product.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not deactivate the product. Please try again.");
        }
    }
    private static void prepareResponse(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private static void sendMessage(
            HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        JSON.writeValue(response.getWriter(), Map.of("message", message));
    }
}