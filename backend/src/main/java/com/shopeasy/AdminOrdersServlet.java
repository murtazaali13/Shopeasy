package com.shopeasy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

@WebServlet("/api/admin/orders")
public class AdminOrdersServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ArrayNode orderList = mapper.createArrayNode();

        String sql = """
            SELECT o.id, o.user_id, u.full_name, u.email,
                   o.shipping_address, o.phone_number, o.total,
                   o.payment_method, o.status
            FROM orders o
            JOIN users u ON o.user_id = u.id
            ORDER BY o.id DESC
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()
        ) {
            while (results.next()) {
                ObjectNode order = orderList.addObject();

                order.put("id", results.getLong("id"));
                order.put("userId", results.getLong("user_id"));
                order.put("customerName", results.getString("full_name"));
                order.put("email", results.getString("email"));
                order.put("shippingAddress", results.getString("shipping_address"));
                order.put("phoneNumber", results.getString("phone_number"));
                order.put("total", results.getBigDecimal("total"));
                order.put("paymentMethod", results.getString("payment_method"));
                order.put("status", results.getString("status"));
            }

            mapper.writeValue(response.getWriter(), orderList);

        } catch (SQLException error) {
            log("Could not load admin orders", error);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            ObjectNode message = mapper.createObjectNode();
            message.put("error", "Could not load orders");
            mapper.writeValue(response.getWriter(), message);
        }
    }
}