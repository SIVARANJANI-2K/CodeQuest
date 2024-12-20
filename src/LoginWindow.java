import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class LoginWindow extends JFrame {

    JTextField emailField;
    JPasswordField passwordField;

    public static final String DB_URL = "jdbc:mysql://localhost:3306/codequest";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root";

    public LoginWindow() {
        setTitle("Login to CodeQuest");
        setSize(600, 400);  // Larger size for the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));  // Increased padding
        mainPanel.setBackground(new Color(245, 245, 255));

        JLabel titleLabel = new JLabel("Login to CodeQuest");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));  // Larger font size
        titleLabel.setForeground(new Color(75, 0, 130));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));  // Increased spacing

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));  // Larger font size
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(emailLabel);

        emailField = new JTextField(25);
        emailField.setMaximumSize(new Dimension(400, 40));  // Larger field width
        mainPanel.add(emailField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));  // Increased spacing

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));  // Larger font size
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(passwordLabel);

        passwordField = new JPasswordField(25);
        passwordField.setMaximumSize(new Dimension(400, 40));  // Larger field width
        mainPanel.add(passwordField);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));  // Increased spacing

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));  // Increased spacing between buttons
        buttonPanel.setBackground(new Color(245, 245, 255));

        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> loginUser());
        buttonPanel.add(loginButton);

        JButton registerButton = createStyledButton("Register");
        registerButton.addActionListener(e -> {
            RegisterWindow registerWindow = new RegisterWindow();
            registerWindow.setVisible(true);
            dispose();
        });
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
        button.setPreferredSize(new Dimension(180, 50));  // Larger button size
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

    public void loginUser() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM users WHERE email = ? AND pass = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                HomeWindow homeWindow = new HomeWindow(rs.getInt("id"));
                homeWindow.setVisible(true);
                dispose();
            } else {
                int choice = JOptionPane.showConfirmDialog(this,
                        "User not found. Do you want to register?", "User Not Found",
                        JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    RegisterWindow registerWindow = new RegisterWindow();
                    registerWindow.setVisible(true);
                    dispose();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        });
    }
}