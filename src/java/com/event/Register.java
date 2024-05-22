package com.event;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Register extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String eventName = request.getParameter("event_name");
        String eventType = request.getParameter("event_type");
        String eventDate = request.getParameter("event_date");
        String eventTime = request.getParameter("event_time");
        String eventDuration = request.getParameter("event_duration");
        String venueName = request.getParameter("venue_name");
        String venueAddress = request.getParameter("venue_address");
        String city = request.getParameter("city");
        String attendeesCount = request.getParameter("attendees_count");
        String[] services = request.getParameterValues("services[]");
        String budget = request.getParameter("budget");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String contactPreference = request.getParameter("contact_preference");

        if (!isValidEmailAddress(email)) {
            response.setContentType("text/html");
            response.getWriter().println("<script>alert('Invalid email address');</script>");
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event", "root", "root");

            String sql = "INSERT INTO event_booking (event_name, event_type, event_date, event_time, event_duration, venue_name, venue_address, attendees_count, services, budget, name, email, phone, contact_preference, city) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, eventName);
            stmt.setString(2, eventType);
            stmt.setString(3, eventDate);
            stmt.setString(4, eventTime);
            stmt.setString(5, eventDuration);
            stmt.setString(6, venueName);
            stmt.setString(7, venueAddress);
            stmt.setString(8, attendeesCount);
            stmt.setString(9, services != null ? String.join(",", services) : null);
            stmt.setString(10, budget);
            stmt.setString(11, name);
            stmt.setString(12, email);
            stmt.setString(13, phone);
            stmt.setString(14, contactPreference);
            stmt.setString(15, city);

            stmt.executeUpdate();

            boolean emailSent = sendConfirmationEmail(email, name, eventName, eventDate, eventTime);
            if (!emailSent) {
                response.setContentType("text/html");
                response.getWriter().println("<script>alert('Failed to send confirmation email');</script>");
                return;
            }

            response.sendRedirect("booksuccess.html?email=" + email);

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isValidEmailAddress(String email) {
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(regex);
    }

    private boolean sendConfirmationEmail(String recipientEmail, String name, String eventName, String eventDate, String eventTime) {
        final String username = "epuri.shivani@gmail.com"; 
        final String password = "nbhs knqi qurt emxd"; 

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Event Booking Confirmation");

            String htmlContent = "<html><body>"
                + "<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "<h2>Event Booking Confirmation</h2>"
                + "<p>Dear " + name + ",</p>"
                + "<p>Thank you for booking the event '" + eventName + "'.</p>"
                + "<p>Event Date: " + eventDate + "</p>"
                + "<p>Event Time: " + eventTime + "</p>"
                + "<p>We look forward to working with you!</p>"
                + "<p>Best regards,<br>EventEase Team</p>"
                + "</div>"
                + "</body></html>";

            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            return true; 

        } catch (MessagingException e) {
            e.printStackTrace();
            return false; 
        }
    }
}
