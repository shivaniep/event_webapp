package com.event;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.google.gson.*;
import java.sql.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class updateDate extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set response content type
        response.setContentType("application/json");

        // Get JSON request body
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        UpdateRequest updateRequest = gson.fromJson(reader, UpdateRequest.class);

        // Extract data from updateRequest object
        String eventName = updateRequest.getEventName();
        String newDate = updateRequest.getNewDate();

        // JDBC code to update the event date in the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root");
            String sql = "UPDATE event_booking SET event_date = ? WHERE event_name = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, newDate);
            statement.setString(2, eventName);
            int rowsUpdated = statement.executeUpdate();
            conn.close();

            // Send success response
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", rowsUpdated > 0);
            PrintWriter out = response.getWriter();
            out.println(jsonResponse.toString());
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Send error response
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", e.getMessage());
            PrintWriter out = response.getWriter();
            out.println(errorResponse.toString());
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle GET requests if needed
    }

    // Define a POJO class for update request data
    private static class UpdateRequest {
        private String eventName;
        private String newDate;

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }

        public String getNewDate() {
            return newDate;
        }

        public void setNewDate(String newDate) {
            this.newDate = newDate;
        }
    }
}
