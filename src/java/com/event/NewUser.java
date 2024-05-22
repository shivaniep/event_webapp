package com.event;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NewUser extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("uname");
        String username = request.getParameter("username");
        String password = request.getParameter("userpassword");
        String confirmPassword = request.getParameter("cpassword");

        boolean userExists = isUserExists(username);

        if (userExists) {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.println("<html><body><script>alert('User already exists!'); window.location.href='Plogin.html';</script></body></html>");
        } else {
            boolean success = registerUser(name, username, password);
            if (success) {
                response.sendRedirect("Plogin.html");
            } else {
                response.setContentType("text/html");
                PrintWriter out = response.getWriter();
                out.println("<html><body><h3>Failed to register user!</h3></body></html>");
            }
        }
    }

    private boolean isUserExists(String username) {
        boolean exists = false;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root")) {
            String sql = "SELECT COUNT(*) FROM user WHERE username=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        exists = (count > 0);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }

    private boolean registerUser(String name, String username, String password) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root")) {
            String sql = "INSERT INTO user (name, username, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, username);
                stmt.setString(3, password);
                int rowsAffected = stmt.executeUpdate();
                return (rowsAffected > 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
