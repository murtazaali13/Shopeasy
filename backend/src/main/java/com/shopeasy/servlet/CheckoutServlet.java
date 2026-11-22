package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@WebServlet("/api/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+()\\s-]{7,30}$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        prepareResponse(response);

        HttpSession session = request.getSession(false);
        Object sessionUserId = session == null ? null : session.getAttribute("userId");

        if (!(sessionUserId instanceof Number userIdNumber)) {
            sendMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Please sign in before placing an order.");
            return;
        }

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
                    "Please provide checkout details.");
            return;
        }

        String address = body.path("shippingAddress").asText("").trim();
        String phone = body.path("phoneNumber").asText("").trim();
        JsonNode items = body.path("items");

        if (address.isEmpty() || address.length() > 500) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a shipping address (up to 500 characters).");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Enter a valid phone number.");
            return;
        }

        if (!items.isArray() || items.size() == 0 || items.size() > 50) {
            sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Your cart must contain between 1 and 50 products.");
            return;
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();

        for (JsonNode item : items) {
            long productId = item.path("id").asLong(-1);
            int quantity = item.path("quantity").asInt(-1);

            if (productId <= 0 || quantity < 1 || quantity > 100) {
                sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                        "A product or quantity in your cart is invalid.");
                return;
            }

            int combinedQuantity = quantities.getOrDefault(productId, 0) + quantity;
            if (combinedQuantity > 100) {
                sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                        "The quantity for a product cannot exceed 100.");
                return;
            }

            quantities.put(productId, combinedQuantity);
        }

        long userId = userIdNumber.longValue();

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                List<OrderLine> orderLines = new ArrayList<>();
                BigDecimal total = BigDecimal.ZERO;

                String productSql = """
                        SELECT name, price, stock_quantity, active
                        FROM products
                        WHERE id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement productStatement =
                             connection.prepareStatement(productSql)) {
                    for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
                        long productId = entry.getKey();
                        int quantity = entry.getValue();

                        productStatement.setLong(1, productId);

                        try (ResultSet product = productStatement.executeQuery()) {
                            if (!product.next()) {
                                connection.rollback();
                                sendMessage(response, HttpServletResponse.SC_BAD_REQUEST,
                                        "A product in your cart could not be found.");
                                return;
                            }

                            if (!product.getBoolean("active")) {
                                connection.rollback();
                                sendMessage(response, HttpServletResponse.SC_CONFLICT,
                                        "A product in your cart is no longer available.");
                                return;
                            }

                            int stock = product.getInt("stock_quantity");
                            if (stock < quantity) {
                                connection.rollback();
                                sendMessage(response, HttpServletResponse.SC_CONFLICT,
                                        "There is not enough stock for "
                                                + product.getString("name") + ".");
                                return;
                            }

                            String productName = product.getString("name");
                            BigDecimal price = product.getBigDecimal("price");

                            orderLines.add(new OrderLine(
                                    productId, productName, quantity, price));
                            total = total.add(
                                    price.multiply(BigDecimal.valueOf(quantity)));
                        }
                    }
                }

                String orderSql = """
                        INSERT INTO orders
                            (user_id, shipping_address, phone_number, total,
                             payment_method, status)
                        VALUES (?, ?, ?, ?, 'CASH_ON_DELIVERY', 'PLACED')
                        """;

                long orderId;
                try (PreparedStatement orderStatement = connection.prepareStatement(
                        orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    orderStatement.setLong(1, userId);
                    orderStatement.setString(2, address);
                    orderStatement.setString(3, phone);
                    orderStatement.setBigDecimal(4, total);
                    orderStatement.executeUpdate();

                    try (ResultSet generatedKeys = orderStatement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("The order ID was not returned.");
                        }
                        orderId = generatedKeys.getLong(1);
                    }
                }

                String itemSql = """
                        INSERT INTO order_items
                            (order_id, product_id, product_name, quantity, unit_price)
                        VALUES (?, ?, ?, ?, ?)
                        """;

                String stockSql = """
                        UPDATE products
                        SET stock_quantity = stock_quantity - ?
                        WHERE id = ? AND stock_quantity >= ? AND active = TRUE
                        """;

                try (PreparedStatement itemStatement =
                             connection.prepareStatement(itemSql);
                     PreparedStatement stockStatement =
                             connection.prepareStatement(stockSql)) {
                    for (OrderLine line : orderLines) {
                        itemStatement.setLong(1, orderId);
                        itemStatement.setLong(2, line.productId());
                        itemStatement.setString(3, line.productName());
                        itemStatement.setInt(4, line.quantity());
                        itemStatement.setBigDecimal(5, line.unitPrice());
                        itemStatement.executeUpdate();

                        stockStatement.setInt(1, line.quantity());
                        stockStatement.setLong(2, line.productId());
                        stockStatement.setInt(3, line.quantity());

                        if (stockStatement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "The stock could not be updated for a product.");
                        }
                    }
                }

                connection.commit();

                response.setStatus(HttpServletResponse.SC_CREATED);
                JSON.writeValue(response.getWriter(), Map.of(
                        "message", "Order placed successfully.",
                        "orderId", orderId,
                        "total", total.toPlainString()
                ));

            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }

        } catch (SQLException exception) {
            log("Could not place the order.", exception);
            sendMessage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not place the order. Please try again.");
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

    private record OrderLine(
            long productId, String productName, int quantity, BigDecimal unitPrice) {
    }
}
