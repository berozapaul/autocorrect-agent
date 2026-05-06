package org.ai.autocorrect.services;

import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * This class contains deliberate issues for testing a Code Review Agent.
 */
public class DataProcessor {

    // 1. HARDCODED SECRET / SECURITY RISK
    private static final String DB_PASSWORD = "super_secret_password_123!";

    // 2. PUBLIC STATIC MEMBER (Encapsulation violation)
    public static List<String> cache = new ArrayList<>();

    // 3. UNUSED FIELD
    private int unusedCounter = 0;

    public void processData(String inputId, String userInput) {
        try {
            // 4. POTENTIAL SQL INJECTION
            // Using string concatenation instead of PreparedStatement parameters
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/db", "admin", DB_PASSWORD);
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM users WHERE id = '" + inputId + "'";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                System.out.println(rs.getString("name"));
            }

            // 5. RESOURCE LEAK
            // Connection, Statement, and ResultSet are never closed.

        } catch (Exception e) {
            // 6. SWALLOWING EXCEPTION / GENERIC CATCH
            // No logging, no rethrowing; the error disappears.
            e.printStackTrace();
        }
    }

    // 7. COGNITIVE COMPLEXITY / NESTED LOGIC
    public String validateAndFormat(String val) {
        if (val != null) {
            if (val.length() > 0) {
                if (val.startsWith("A")) {
                    for (int i = 0; i < 1; i++) {
                        return val.toUpperCase();
                    }
                }
            }
        }
        return null; // 8. RETURNING NULL (Should use Optional or empty string)
    }

    // 9. PERFORMANCE: STRING CONCATENATION IN LOOP
    public String buildReport(String[] items) {
        String report = "";
        for (String item : items) {
            report += item + ", "; // Should use StringBuilder
        }
        return report;
    }

    // 10. EQUALS WITHOUT HASHCODE
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DataProcessor)) return false;
        return true;
    }
}
