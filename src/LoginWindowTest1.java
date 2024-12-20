import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoginWindowTest1 {

    @Test
    void loginUser_SuccessfulLogin() {
        // Create a new instance of LoginWindow
        LoginWindow loginWindow = new LoginWindow();

        // Set the email and password directly
        loginWindow.emailField.setText("user@example.com");
        loginWindow.passwordField.setText("password");

        // Create a new connection for testing
        try (Connection conn = DriverManager.getConnection(loginWindow.DB_URL, loginWindow.DB_USER, loginWindow.DB_PASSWORD)) {
            // Create a prepared statement to test the query
            String query = "SELECT * FROM users WHERE email = ? AND pass = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "user@example.com");
            stmt.setString(2, "password");
            ResultSet rs = stmt.executeQuery();

            // Test if the user exists
            if (rs.next()) {
                assertEquals("user@example.com", rs.getString("email"));
                assertEquals("password", rs.getString("pass"));
            }
        } catch (SQLException ex) {
            fail("Database connection failed: " + ex.getMessage());
        }
    }

    @Test
    void loginUser_UnsuccessfulLogin() {
        // Create a new instance of LoginWindow
        LoginWindow loginWindow = new LoginWindow();

        // Set the email and password directly
        loginWindow.emailField.setText("nonexistent@example.com");
        loginWindow.passwordField.setText("wrongpassword");

        // Create a new connection for testing
        try (Connection conn = DriverManager.getConnection(loginWindow.DB_URL, loginWindow.DB_USER, loginWindow.DB_PASSWORD)) {
            // Create a prepared statement to test the query
            String query = "SELECT * FROM users WHERE email = ? AND pass = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "nonexistent@example.com");
            stmt.setString(2, "wrongpassword");
            ResultSet rs = stmt.executeQuery();

            // Test if no user is found
            assertFalse(rs.next(), "User should not be found in the database");
        } catch (SQLException ex) {
            fail("Database connection failed: " + ex.getMessage());
        }
    }

}
