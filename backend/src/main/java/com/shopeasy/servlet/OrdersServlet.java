package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.config.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/orders")
public class OrdersServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Object sessionUserId = session == null ? null : session.getAttribute("userId");

        if (!(sessionUserId instanceof Number userIdNumber)) {
            sendMessage(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Please sign in to view your orders.");
            return;
        }

        String sql = """
                SELECT
                    o.id AS order_id,
                    o.status,
                    o.total,
                    o.payment_method,
                    o.created_at,
                    oi.product_name,
                    oi.quantity,
                    oi.unit_price
                FROM orders o
                LEFT JOIN order_items oi ON oi.order_id = o.id
                WHERE o.user_id = ?
                ORDER BY o.created_at DESC, o.id DESC, oi.id ASC
                """;

        List<Map<String, Object>> orders = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userIdNumber.longValue());

            try (ResultSet results = statement.executeQuery()) {
                long currentOrderId = -1;
                List<Map<String, Object>> currentItems = null;

                while (results.next()) {
                    long orderId = results.getLong("order_id");

                    if (orderId != currentOrderId) {
                        currentOrderId = orderId;
                        currentItems = new ArrayList<>();

                        Map<String, Object> order = new LinkedHashMap<>();
                        order.put("orderId", orderId);
                        order.put("status", results.getString("status"));
                        order.put("total", results.getBigDecimal("total"));
                        order.put("paymentMethod",
                                results.getString("payment_method"));

                        Timestamp createdAt = results.getTimestamp("created_at");
                        order.put("createdAt",
                                createdAt == null ? "" : createdAt.toString());
                        order.put("items", currentItems);

                        orders.add(order);
                    }

                    String productName = results.getString("product_name");
                    if (productName != null && currentItems != null) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("productName", productName);
                        item.put("quantity", results.getInt("quantity"));
                        item.put("unitPrice",
                                results.getBigDecimal("unit_price"));
                        currentItems.add(item);
                    }
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
            JSON.writeValue(response.getWriter(), Map.of("orders", orders));

        } catch (SQLException exception) {
            log("Could not load customer orders.", exception);
            sendMessage(response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not load your orders. Please try again.");
        }
    }

    private static void sendMessage(
            HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        JSON.writeValue(response.getWriter(), Map.of("message", message));
    }
}
