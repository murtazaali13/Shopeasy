package com.shopeasy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL driver is not available.", exception);
        }

        String url = System.getenv().getOrDefault(
            "SHOPEASY_DB_URL",
            "jdbc:mysql://localhost:3306/shopeasy"
        );

        return DriverManager.getConnection(
            url,
            required("SHOPEASY_DB_USER"),
            required("SHOPEASY_DB_PASSWORD")
        );
    }

    private static String required(String name) throws SQLException {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new SQLException("Missing environment variable: " + name);
        }

        return value;
    }
}