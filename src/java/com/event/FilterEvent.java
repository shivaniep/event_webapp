package com.event;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

public class FilterEvent extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String category = request.getParameter("category");
        String location = request.getParameter("location");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String[] selectedStatus = request.getParameterValues("status");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root");

            String query = "SELECT * FROM event_booking WHERE event_type = ? AND city = ?";
            if (startDate != null && !startDate.isEmpty()) {
                query += " AND event_date >= ?";
            }
            if (endDate != null && !endDate.isEmpty()) {
                query += " AND event_date <= ?";
            }
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, category);
            statement.setString(2, location);
            int parameterIndex = 3;
            if (startDate != null && !startDate.isEmpty()) {
                statement.setString(parameterIndex++, startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                statement.setString(parameterIndex++, endDate);
            }

            ResultSet resultSet = statement.executeQuery();

            List<Event> filteredEvents = new ArrayList<>();

            Calendar currentDate = Calendar.getInstance();

            while (resultSet.next()) {
                String eventName = resultSet.getString("event_name");
                String eventType = resultSet.getString("event_type");
                String eventLocation = resultSet.getString("city");
                String eventDateStr = resultSet.getString("event_date");
                String eventStatus = calculateEventStatus(eventDateStr, currentDate);
                

                if (selectedStatus == null || selectedStatus.length == 0 || isSelectedStatus(eventStatus, selectedStatus)) {
                    Event event = new Event(eventName, eventType, eventLocation, eventDateStr);
                    filteredEvents.add(event);
                }
            }

            String jsonEvents = convertEventsToJSON(filteredEvents);

            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(jsonEvents);
            out.flush();
            
            // Log the JSON response
            System.out.println("JSON Response: " + jsonEvents);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String calculateEventStatus(String eventDateStr, Calendar currentDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date eventDate = sdf.parse(eventDateStr);
            Calendar eventCalendar = Calendar.getInstance();
            eventCalendar.setTime(eventDate);

            if (eventCalendar.before(currentDate)) {
                return "Completed";
            } else if (eventCalendar.equals(currentDate)) {
                return "Ongoing";
            } else {
                return "Upcoming";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private boolean isSelectedStatus(String eventStatus, String[] selectedStatus) {
        for (String status : selectedStatus) {
            if (eventStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private String convertEventsToJSON(List<Event> events) {
        Gson gson = new Gson();
        String jsonEvents = gson.toJson(events);
        System.out.println("JSON String: " + jsonEvents); 
        return jsonEvents;
    }
}

class Event {
    private String name;
    private String type;
    private String location;
    private String date;

    public Event(String name, String type, String location, String date) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.date = date;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}