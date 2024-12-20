import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class RegisterWindow extends JFrame {

    JTextField emailField, usernameField;
    JPasswordField passwordField, confirmPasswordField;

    public static final String DB_URL = "jdbc:mysql://localhost:3306/codequest";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root";

    public RegisterWindow() {
        setTitle("Register for CodeQuest");
        setSize(800, 600);  // Even larger size for the window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));  // Increased padding
        mainPanel.setBackground(new Color(245, 245, 255));

        JLabel titleLabel = new JLabel("Register for CodeQuest");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));  // Larger font size
        titleLabel.setForeground(new Color(75, 0, 130));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));  // Increased spacing

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));  // Larger font size
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(usernameLabel);

        usernameField = new JTextField(25);
        usernameField.setMaximumSize(new Dimension(500, 40));  // Larger field width
        mainPanel.add(usernameField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));  // Increased spacing

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));  // Larger font size
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(emailLabel);

        emailField = new JTextField(25);
        emailField.setMaximumSize(new Dimension(500, 40));  // Larger field width
        mainPanel.add(emailField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));  // Increased spacing

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));  // Larger font size
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(passwordLabel);

        passwordField = new JPasswordField(25);
        passwordField.setMaximumSize(new Dimension(500, 40));  // Larger field width
        mainPanel.add(passwordField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));  // Increased spacing

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));  // Larger font size
        confirmPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(confirmPasswordLabel);

        confirmPasswordField = new JPasswordField(25);
        confirmPasswordField.setMaximumSize(new Dimension(500, 40));  // Larger field width
        mainPanel.add(confirmPasswordField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));  // Increased spacing

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 0));  // Increased spacing between buttons
        buttonPanel.setBackground(new Color(245, 245, 255));

        JButton registerButton = createStyledButton("Register");
        registerButton.addActionListener(e -> registerUser());
        buttonPanel.add(registerButton);

        mainPanel.add(buttonPanel);

        // Center content using a container with GridBagLayout
        JPanel containerPanel = new JPanel(new GridBagLayout());
        containerPanel.add(mainPanel);

        add(containerPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));  // Larger font size
        button.setBackground(new Color(138, 43, 226));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 60));  // Larger button size
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Curved border
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 0, 130), 3, true),  // Thicker border
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        button.addMouseListener(new MouseAdapter() {
            Color originalColor = button.getBackground();

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(originalColor.darker());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(originalColor);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalColor);
            }
        });

        return button;
    }

    public void registerUser() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Check if passwords match
        if (password.equals(confirmPassword)) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // SQL query to insert a new user
                String query = "INSERT INTO users (username, email, pass) VALUES (?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, password);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Registration successful!");
                    dispose();
                    new LoginWindow().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RegisterWindow registerWindow = new RegisterWindow();
            registerWindow.setVisible(true);
        });
    }
}