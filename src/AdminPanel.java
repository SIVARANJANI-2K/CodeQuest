import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminPanel extends JFrame {
    public static  final String DB_URL = "jdbc:mysql://localhost:3306/codequest"; // replace with your database URL
    public static final String DB_USER = "root"; // replace with your MySQL username
    public static final String DB_PASSWORD = "root"; // replace with your MySQL password

    private JPanel panel;
    private JButton manageUsersButton;
    private JButton manageProblemsButton;
    private JTextField passwordField;
    private JButton loginButton;
    private final String adminPassword = "admin@CQ";

    public AdminPanel() {
        // Frame settings
        setTitle("Admin Panel");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Background color
        getContentPane().setBackground(new Color(0, 102, 204));

        // Panel settings
        panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Password field
        JLabel passwordLabel = new JLabel("Enter Admin Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(30, 144, 255));
        loginButton.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(loginButton, gbc);

        // Action for login button
        loginButton.addActionListener(e -> {
            if (passwordField.getText().equals(adminPassword)) {
                showAdminOptions();
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel, BorderLayout.CENTER);
    }

    public  void showAdminOptions() {
        panel.removeAll();
        panel.revalidate();
        panel.repaint();

        // Manage Users Button
        manageUsersButton = new JButton("Manage Users");
        manageUsersButton.setFont(new Font("Arial", Font.BOLD, 16));
        manageUsersButton.setBackground(new Color(100, 149, 237));
        manageUsersButton.setForeground(Color.WHITE);
        manageUsersButton.addActionListener(e -> showUsers());
        panel.add(manageUsersButton);

        // Manage Problems Button
        manageProblemsButton = new JButton("Manage Problems");
        manageProblemsButton.setFont(new Font("Arial", Font.BOLD, 16));
        manageProblemsButton.setBackground(new Color(100, 149, 237));
        manageProblemsButton.setForeground(Color.WHITE);
        manageProblemsButton.addActionListener(e -> showProblems());
        panel.add(manageProblemsButton);

        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        add(panel);
    }

    public void showUsers() {
        JFrame userFrame = new JFrame("Manage Users");
        userFrame.setSize(400, 300);
        userFrame.getContentPane().setBackground(Color.WHITE);

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));

        // Fetch user data
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id,username, email FROM users");

            while (rs.next()) {
                JPanel userRow = new JPanel();
                userRow.setBackground(Color.WHITE);
                userRow.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
                String username = rs.getString("username");
                String email = rs.getString("email");
                int userId = rs.getInt("id");
                JLabel userLabel = new JLabel(rs.getString("username") + " - " + rs.getString("email"));
                userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                userRow.add(userLabel);

                JButton removeButton = new JButton("Remove");
                removeButton.setBackground(Color.RED);
                removeButton.setForeground(Color.WHITE);
                removeButton.addActionListener(e -> {
                    try {
                        removeUser(username,userId);
                        userFrame.dispose();
                        showUsers();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                userRow.add(removeButton);

                userPanel.add(userRow);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        userFrame.add(new JScrollPane(userPanel));
        userFrame.setVisible(true);
    }

    public void removeUser(String username,int userId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);  // Start transaction

            // First, delete related entries from problems_solved
            PreparedStatement deleteProblemsSolvedStmt = conn.prepareStatement("DELETE FROM problems_solved WHERE user_id = ?");
            deleteProblemsSolvedStmt.setInt(1, userId);
            deleteProblemsSolvedStmt.executeUpdate();

            // Then, delete the user from the users table
            PreparedStatement deleteUserStmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
            deleteUserStmt.setInt(1, userId);
            deleteUserStmt.executeUpdate();

            conn.commit();  // Commit transaction
            JOptionPane.showMessageDialog(null, "User and related solved records have been removed.");
            JOptionPane.showMessageDialog(null, username + " has been removed.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void showProblems() {
        JFrame problemFrame = new JFrame("Manage Problems");
        problemFrame.setSize(500, 500);
        problemFrame.getContentPane().setBackground(Color.WHITE);

        JPanel problemPanel = new JPanel();
        problemPanel.setLayout(new BoxLayout(problemPanel, BoxLayout.Y_AXIS));

        // Display existing problems
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT problem_id, title FROM problems");

            while (rs.next()) {
                JPanel problemRow = new JPanel();
                problemRow.setBackground(Color.WHITE);
                problemRow.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

                JLabel problemLabel = new JLabel("ID: " + rs.getInt("problem_id") + " - Title: " + rs.getString("title"));
                problemLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                problemRow.add(problemLabel);

                JButton removeButton = new JButton("Remove");
                removeButton.setBackground(Color.RED);
                removeButton.setForeground(Color.WHITE);
                int problemId = rs.getInt("problem_id");
                removeButton.addActionListener(e -> {
                    removeProblem(problemId);
                    problemFrame.dispose();
                    showProblems();

                });
                problemRow.add(removeButton);

                problemPanel.add(problemRow);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Add form for new problem
        JPanel addProblemPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        addProblemPanel.add(new JLabel("Problem ID:"));
        JTextField idField = new JTextField();
        addProblemPanel.add(idField);

        addProblemPanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField();
        addProblemPanel.add(titleField);

        addProblemPanel.add(new JLabel("Description:"));
        JTextField descriptionField = new JTextField();
        addProblemPanel.add(descriptionField);

        addProblemPanel.add(new JLabel("Difficulty:"));
        JTextField difficultyField = new JTextField();
        addProblemPanel.add(difficultyField);

        JButton addButton = new JButton("Add Problem");
        addButton.setBackground(new Color(30, 144, 255));
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> {
            addProblem(Integer.parseInt(idField.getText()), titleField.getText(), descriptionField.getText(), Integer.parseInt(difficultyField.getText()));
            problemFrame.dispose();
            showProblems();
        });
        addProblemPanel.add(addButton);

        problemPanel.add(addProblemPanel);
        problemFrame.add(new JScrollPane(problemPanel));
        problemFrame.setVisible(true);
    }

    public void removeProblem(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);  // Start transaction

            // First, delete related entries from problems_solved
            PreparedStatement deleteProblemsSolvedStmt = conn.prepareStatement("DELETE FROM problems_solved WHERE problem_id = ?");
            deleteProblemsSolvedStmt.setInt(1, id);
            deleteProblemsSolvedStmt.executeUpdate();

            // Then, delete related entries from testcases
            PreparedStatement deleteTestcasesStmt = conn.prepareStatement("DELETE FROM testcases WHERE problem_id = ?");
            deleteTestcasesStmt.setInt(1, id);
            deleteTestcasesStmt.executeUpdate();

            // Finally, delete the problem from the problems table
            PreparedStatement deleteProblemStmt = conn.prepareStatement("DELETE FROM problems WHERE problem_id = ?");
            deleteProblemStmt.setInt(1, id);
            deleteProblemStmt.executeUpdate();

            conn.commit();  // Commit transaction
            JOptionPane.showMessageDialog(null, "Problem with ID " + id + " has been removed.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void addProblem(int id, String title, String description, int difficulty) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO problems (problem_id, title, details, difficulty) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, difficulty);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Problem added successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminPanel adminPanel = new AdminPanel();
            adminPanel.setVisible(true);
        });
    }



}
