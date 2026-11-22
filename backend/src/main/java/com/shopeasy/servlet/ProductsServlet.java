package com.shopeasy.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.config.DatabaseConnection;
import com.shopeasy.model.Product;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/products")
public class ProductsServlet extends HttpServlet {
    private static final ObjectMapper JSON = new ObjectMapper();

    @Override
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String sql = """
            SELECT id, name, category, description, price, stock_quantity
            FROM products
            WHERE active = TRUE
            ORDER BY id
            """;

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet results = statement.executeQuery()
        ) {
            List<Product> products = new ArrayList<>();

            while (results.next()) {
                products.add(new Product(
                    results.getLong("id"),
                    results.getString("name"),
                    results.getString("category"),
                    results.getString("description"),
                    results.getBigDecimal("price"),
                    results.getInt("stock_quantity")
                ));
            }

            JSON.writeValue(response.getOutputStream(), products);
        } catch (SQLException exception) {
            getServletContext().log("Could not load products", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSON.writeValue(
                response.getOutputStream(),
                Map.of("error", "Could not load products")
            );
        }
    }
}
