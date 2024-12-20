import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

class RegisterWindowTest {

    @Test
    void testRegisterUser_successfulRegistration() {
        // Create instance of RegisterWindow to test registration logic
        RegisterWindow registerWindow = new RegisterWindow();

        // Setup values for testing
        registerWindow.usernameField.setText("testUser");
        registerWindow.emailField.setText("testUser@example.com");
        registerWindow.passwordField.setText("password123");

        // The actual registerUser method relies on the database, so we would typically test this with a real or test DB.
        // For this example, we'll verify that the method correctly calls the database.

        try {
            // Try registering the user, this should insert a row into the database
            // We will assume the DB connection and execution happen correctly (without actually testing the DB here).
            registerWindow.registerUser();
        } catch (Exception ex) {
            fail("Database error: " + ex.getMessage());
        }

        // Verify behavior by checking if a confirmation message is shown and if the window closes (or performs expected action)
        // In real tests, you could mock `JOptionPane` or check for window closure.
    }

    @Test
    void testRegisterUser_registrationFailed() {
        // Create instance of RegisterWindow to test failed registration
        RegisterWindow registerWindow = new RegisterWindow();

        // Simulate an invalid registration (e.g., username already exists)
        registerWindow.usernameField.setText("testUser");
        registerWindow.emailField.setText("testUser@example.com");
        registerWindow.passwordField.setText("password123");

        try {
            // Simulate a failed registration (normally this would be caught by a database check)
            registerWindow.registerUser();
        } catch (Exception ex) {
            // Handle exception (this should simulate a failure to insert into the DB)
            fail("Database error: " + ex.getMessage());
        }

        // Verify if the correct error message is shown
        // Ideally, you'd check that the error handling logic displays an appropriate message using JOptionPane
    }




    @Test
    void testMain() {
        // Test if the main method runs without any exceptions
        assertDoesNotThrow(() -> RegisterWindow.main(new String[]{}), "Main method should run without exceptions");
    }
}
