package com.event;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ConfirmBooking extends HttpServlet {
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String ename = request.getParameter("ename");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        
        System.out.println("Event Name: " + ename);
        
        

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root");
            preparedStatement = connection.prepareStatement("SELECT event_name, event_date, event_time, venue_name, venue_address, city FROM event_booking WHERE event_name = ?");
            preparedStatement.setString(1, ename);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String event_name = resultSet.getString("event_name");
                String event_date = resultSet.getString("event_date");
                String event_time = resultSet.getString("event_time");
                String venue_name = resultSet.getString("venue_name");
                String venue_address = resultSet.getString("venue_address");
                String city = resultSet.getString("city");

                sendConfirmationEmail(name, email, event_name, event_date, event_time, venue_name, venue_address, city);

                out.print("{\"message\":\"Booking confirmed! A confirmation email has been sent.\"}");
            } else {
                out.print("{\"error\":\"Event not found\"}");
            }
        } catch (SQLException | ClassNotFoundException | MessagingException e) {
            e.printStackTrace();
            out.print("{\"error\":\"An error occurred\"}");
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // This method is called for GET requests
        // You can handle GET requests here if needed
    }

    private void sendConfirmationEmail(String name, String email, String event_name, String event_date, String event_time, String venue_name, String venue_address, String city) throws MessagingException {
        String from = "epuri.shivani@gmail.com";
        String password = "nbhs knqi qurt emxd";
        String to = email;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        };

        Session session = Session.getInstance(properties, auth);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("Booking Confirmation for Event: " + event_name);

        String mapsLink = "https://www.google.com/maps/search/?api=1&query=" + venue_address + ", " + city;

        String content = "Dear " + name + ",<br><br>";
        content += "Your booking for the event \"" + event_name + "\" has been confirmed.<br><br>";
        content += "<b>Event Details:</b><br>";
        content += "<b>Name:</b> " + event_name + "<br>";
        content += "<b>Date:</b> " + event_date + "<br>";
        content += "<b>Time:</b> " + event_time + "<br>";
        content += "<b>Venue:</b> " + venue_name + "<br>";
        content += "<b>Address:</b> " + venue_address + ", " + city + "<br>";
        content += "<b>Location:</b> <a href=\"" + mapsLink + "\">View on Google Maps</a><br><br>";
        content += "Thank you for booking with us!<br>";
        content += "Best regards,<br>";
        content += "EventEase Team";

        message.setContent(content, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
