import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

class HomeWindow extends JFrame {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/codequest"; // Replace with your database URL
    public static final String DB_USER = "root"; // Replace with your MySQL username
    public static final String DB_PASSWORD = "root";
    private String username = "";
    private int problems_solved;
    private int badge_count;
    public int userId;

    public HomeWindow(int id) {
        setTitle("Home");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Fetch user details from the database
        userId=id;
        getUserDetails(id);

        // Main panel with BoxLayout for vertical stacking
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // User icon setup
        ImageIcon userIcon = new ImageIcon("./resources/User.png"); // Update with your icon path
        Image img = userIcon.getImage();
        Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Scale icon to fit
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImg));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Profile name and user details
        JLabel profileName = new JLabel("Hey, " + username);
        profileName.setFont(new Font("Arial", Font.BOLD, 20));
        profileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel problemsSolvedLabel = new JLabel("Problems Solved: " + problems_solved);
        JLabel badgesEarnedLabel = new JLabel("Badges Earned: " + badge_count);
        problemsSolvedLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        badgesEarnedLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        problemsSolvedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgesEarnedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton editProfileButton = new JButton("Edit Profile");
        editProfileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editProfileButton.addActionListener(e -> openEditProfileWindow());

        // Details panel for better alignment and spacing of details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
        detailsPanel.add(problemsSolvedLabel);
        detailsPanel.add(badgesEarnedLabel);

        // Add components to the main panel
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(iconLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(profileName);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(detailsPanel);
        mainPanel.add(editProfileButton);
        mainPanel.add(Box.createVerticalStrut(20));

        // Add problem list section

        JPanel problemListPanel = createProblemListPanel();
        mainPanel.add(problemListPanel);

        // Adjust layout
        add(new JScrollPane(mainPanel));
    }
    private void openEditProfileWindow() {
        JFrame editProfileFrame = new JFrame("Edit Profile");
        editProfileFrame.setSize(400, 300);
        editProfileFrame.setLocationRelativeTo(this);
        editProfileFrame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(username, 15);

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(15);

        // Update Button
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(e -> {
            String newUsername = usernameField.getText();
            String newPassword = new String(passwordField.getPassword());
            updateUserDetails(newUsername, newPassword);
            editProfileFrame.dispose();
            reloadHomeWindow();
        });

        // Adding components to the frame with GridBag layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        editProfileFrame.add(usernameLabel, gbc);
        gbc.gridy++;
        editProfileFrame.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        editProfileFrame.add(usernameField, gbc);
        gbc.gridy++;
        editProfileFrame.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        editProfileFrame.add(updateButton, gbc);

        editProfileFrame.setVisible(true);
    }
    private void updateUserDetails(String newUsername, String newPassword) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String updateQuery = "UPDATE users SET username = ?, pass = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(updateQuery);
            pst.setString(1, newUsername);
            pst.setString(2, newPassword);
            pst.setInt(3, userId);
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Profile updated successfully.");
                this.username = newUsername;
            } else {
                System.out.println("Profile update failed.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating profile: " + e.getMessage());
        }
    }
    private JPanel createProblemListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        Font sectionFont = new Font("Arial", Font.BOLD, 20);
        Font contentFont = new Font("Arial", Font.PLAIN, 14);

        // Query for problems solved by this user
        Set<Integer> solvedProblems = new HashSet<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String solvedQuery = "SELECT problem_id FROM problems_solved WHERE user_id = ?";
            PreparedStatement solvedPst = conn.prepareStatement(solvedQuery);
            solvedPst.setInt(1, userId);
            ResultSet solvedRs = solvedPst.executeQuery();
            while (solvedRs.next()) {
                solvedProblems.add(solvedRs.getInt("problem_id"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching solved problems: " + e.getMessage());
        }

        // Retrieve and categorize problems
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT problem_id, title, difficulty FROM problems ORDER BY difficulty, problem_id";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            int currentDifficulty = -1;
            int row = 1;

            while (rs.next()) {
                int problemId = rs.getInt("problem_id");
                String title = rs.getString("title");
                int difficulty = rs.getInt("difficulty");

                // Check if a new difficulty section label is needed
                if (difficulty != currentDifficulty) {
                    currentDifficulty = difficulty;
                    String difficultyLabel = switch (difficulty) {
                        case 1 -> "Easy Problems";
                        case 2 -> "Medium Problems";
                        case 3 -> "Hard Problems";
                        default -> "Unknown Difficulty";
                    };
                    JLabel sectionLabel = new JLabel(difficultyLabel, JLabel.CENTER);
                    sectionLabel.setFont(sectionFont);
                    gbc.gridy = row++;
                    gbc.gridx = 0;
                    gbc.gridwidth = 4;
                    panel.add(sectionLabel, gbc);
                }

                // Reset grid width for problem list row
                gbc.gridwidth = 1;

                // S.No.
                gbc.gridy = row;
                gbc.gridx = 0;
                JLabel snoLabel = new JLabel(String.valueOf(row - 1), JLabel.CENTER);
                snoLabel.setFont(contentFont);
                panel.add(snoLabel, gbc);

                // Title with green circle if solved
                gbc.gridx = 1;
                JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                if (solvedProblems.contains(problemId)) {
                    JLabel greenCircle = createGreenCircleLabel();
                    titlePanel.add(greenCircle);
                }
                JLabel titleLabel = new JLabel(title, JLabel.LEFT);
                titleLabel.setFont(contentFont);
                titlePanel.add(titleLabel);
                panel.add(titlePanel, gbc);

                // View Details button
                gbc.gridx = 2;
                JButton viewDetailsButton = new JButton("View Details");
                viewDetailsButton.setFont(contentFont);
                viewDetailsButton.addActionListener(e -> openDetailsWindow(problemId));
                panel.add(viewDetailsButton, gbc);

                // Solve button
                gbc.gridx = 3;
                JButton solveButton = new JButton("Solve");
                solveButton.setFont(contentFont);
                solveButton.addActionListener(e -> {
                    openCodeEditor(problemId);
                });
                panel.add(solveButton, gbc);

                row++;
            }
        } catch (SQLException e) {
            System.out.println("Exception caught: " + e.getMessage());
        }

        return panel;
    }

    // Method to open the details window for a problem
    private void openDetailsWindow(int problemId) {
        JFrame detailsFrame = new JFrame("Problem Details");
        detailsFrame.setSize(400, 300);
        detailsFrame.setLocationRelativeTo(this);

        // Fetch and format problem details
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Arial", Font.PLAIN, 14));

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT details FROM problems WHERE problem_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, problemId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String details = rs.getString("details");
                detailsArea.setText(formatDetailsText(details));
            } else {
                detailsArea.setText("Problem details not found.");
            }
        } catch (SQLException e) {
            detailsArea.setText("Error fetching details: " + e.getMessage());
        }

        detailsFrame.add(new JScrollPane(detailsArea));
        detailsFrame.setVisible(true);
    }

    private String formatDetailsText(String text) {
        StringBuilder formattedText = new StringBuilder();
        String[] words = text.split(" ");
        int lineLength = 0;
        for (String word : words) {
            if (lineLength + word.length() > 40) {
                formattedText.append("\n");
                lineLength = 0;
            }
            formattedText.append(word).append(" ");
            lineLength += word.length() + 1;
        }
        return formattedText.toString();
    }


    // Method to open the code editor
    private void openCodeEditor(int problemId) {
        CodeEditorWindow editorWindow = new CodeEditorWindow(problemId,userId,this);
        editorWindow.setVisible(true);
    }

    public void getUserDetails(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT username, problems_count, badges_count FROM users WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                username = rs.getString("username");
                problems_solved = rs.getInt("problems_count");
                badge_count = rs.getInt("badges_count");
            } else {
                System.out.println("No data found for the given ID.");
            }
        } catch (SQLException e) {
            System.out.println("Exception caught: " + e.getMessage());
        }
    }
    public  void reloadHomeWindow() {
        // Dispose the current window
        this.dispose();

        // Create and show a new instance of HomeWindow with the same userId
        HomeWindow newHomeWindow = new HomeWindow(this.userId);
        newHomeWindow.setVisible(true);
    }
    private JLabel createGreenCircleLabel() {
        JLabel greenCircle = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.GREEN);
                g.fillOval(0, 0, 10, 10);  // Adjust size as needed
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, 10);  // Size of the circle
            }
        };
        greenCircle.setPreferredSize(new Dimension(10, 10));  // Ensure label size matches circle size
        return greenCircle;
    }

}
