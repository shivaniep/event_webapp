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


public class GetEventDetails extends HttpServlet {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response content type
        response.setContentType("application/json");

        // Get event name from request parameter
        String eventName = request.getParameter("eventName");

        // Initialize PrintWriter
        PrintWriter out = response.getWriter();

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection to MySQL database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root");

            // Prepare the SQL statement
            preparedStatement = connection.prepareStatement("SELECT event_name, event_date, event_time, venue_name, venue_address, city FROM event_booking WHERE event_name = ?");
            preparedStatement.setString(1, eventName);

            // Execute the query
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Retrieve event details from the result set
                String event_name = resultSet.getString("event_name");
                String event_date = resultSet.getString("event_date");
                String event_time = resultSet.getString("event_time");
                String venue_name = resultSet.getString("venue_name");
                String venue_address = resultSet.getString("venue_address");
                String city = resultSet.getString("city");

                // Create a JSON object containing event details
                String eventDetailsJson = String.format("{\"name\":\"%s\", \"date\":\"%s\", \"time\":\"%s\", \"venueName\":\"%s\", \"venueAddress\":\"%s\", \"city\":\"%s\"}",
                        event_name, event_date, event_time, venue_name, venue_address, city);

                // Send JSON response to the client
                out.print(eventDetailsJson);
            } else {
                out.print("{\"error\":\"Event not found\"}");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Send an error message in case of exception
            out.print("{\"error\":\"An error occurred\"}");
        } finally {
            // Close all JDBC objects
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
