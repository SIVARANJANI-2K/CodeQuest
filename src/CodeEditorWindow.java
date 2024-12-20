import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


class CodeEditorWindow extends JFrame {
    public JTextArea codeArea;
    public JTextArea lineNumberArea;
    public int problemId;
    public int userId;// ID of the logged-in user
    public HomeWindow h;
    public Callable<List<CodeCompiler.TestCase>> getTestCases;
    public  int problemsSolved;
    public int badgeCount=0;
    public JTextPane resultPane;

   /* public CodeEditorWindow(int userId, int problemId, JTextPane resultPane) {
        this.userId = userId;
        this.problemId = problemId;
        this.resultPane = resultPane;
    }*/

    // Simulate the display of result messages


    public CodeEditorWindow(int problemId, int userId,HomeWindow homeWindow) {
        this.problemId = problemId;
        this.userId = userId;
        this.problemsSolved = 0; // Initial count of solved problems
        this.badgeCount = 0;
        setTitle("Code Editor");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        h=homeWindow;
        // Main code area
        codeArea = new JTextArea();
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        codeArea.setBackground(new Color(40, 40, 40));
        codeArea.setForeground(Color.WHITE);
        codeArea.setCaretColor(Color.WHITE);

        // Predefined code template
        codeArea.setText("public class Solution {\n    public static void main(String[] args) {\n        // Write your code here\n    }\n}");

        // Line number area
        lineNumberArea = new JTextArea("1");
        lineNumberArea.setBackground(new Color(30, 30, 30));
        lineNumberArea.setForeground(Color.GRAY);
        lineNumberArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(codeArea);
        scrollPane.setRowHeaderView(lineNumberArea);

        // Synchronize line numbers with code area
        codeArea.getDocument().addDocumentListener(new DocumentListener() {
            public String getText() {
                int lines = codeArea.getLineCount();
                StringBuilder text = new StringBuilder("1\n");
                for (int i = 2; i <= lines; i++) {
                    text.append(i).append("\n");
                }
                return text.toString();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                lineNumberArea.setText(getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lineNumberArea.setText(getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lineNumberArea.setText(getText());
            }
        });

        JButton saveButton = new JButton("Run Code");
        saveButton.addActionListener(e -> compileAndRunCode());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(saveButton);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void compileAndRunCode() {
        String code = codeArea.getText();
        List<CodeCompiler.TestCase> testCases = getTestCases();

        if (testCases.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No test cases found for this problem.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String result = CodeCompiler.compileAndRun("Solution", code, testCases);

        // Check if all test cases passed
        if (!(result.contains("Failure.")) && !(result.contains("failed") && !(result.contains("error") && !(result.contains("Error"))))){
            updateProblemsSolved();
            result += "\nAll test cases passed!";
        }

        displayResult(result);
    }

    public List<CodeCompiler.TestCase> getTestCases() {
        List<CodeCompiler.TestCase> testCases = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(HomeWindow.DB_URL, HomeWindow.DB_USER, HomeWindow.DB_PASSWORD)) {
            String query = "SELECT input, expected_output, is_public FROM testcases WHERE problem_id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, problemId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String input = rs.getString("input");
                String expectedOutput = rs.getString("expected_output");
                boolean isPublic = rs.getBoolean("is_public");
                testCases.add(new CodeCompiler.TestCase(input, expectedOutput, isPublic));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching test cases: " + e.getMessage());
        }

        return testCases;
    }


    public void updateProblemsSolved() {
        try (Connection conn = DriverManager.getConnection(HomeWindow.DB_URL, HomeWindow.DB_USER, HomeWindow.DB_PASSWORD)) {
            String selectQuery="SELECT problem_id from problems_solved where user_id=?";
            boolean isNeeded=true;
            PreparedStatement st=conn.prepareStatement(selectQuery);
            st.setInt(1,userId);
            ResultSet rs=st.executeQuery();
            while(rs.next()){
                if(rs.getInt("problem_id")==problemId) {
                    isNeeded = false;
                    break;
                }
            }
            if(isNeeded) {
                String updateQuery = "UPDATE users SET problems_count = problems_count + 1 WHERE id = ?";
                PreparedStatement pst = conn.prepareStatement(updateQuery);
                pst.setInt(1, userId);
                pst.executeUpdate();

                String insertQuery = "INSERT IGNORE INTO problems_solved (user_id, problem_id) VALUES (?, ?)";
                PreparedStatement pst2 = conn.prepareStatement(insertQuery);
                pst2.setInt(1, userId);
                pst2.setInt(2, problemId);
                pst2.executeUpdate();

                String badgeQuery="Select username,problems_count,badges_count from users where id=?";
                PreparedStatement bt=conn.prepareStatement(badgeQuery);
                bt.setInt(1,userId);
                ResultSet r=bt.executeQuery();

                while(r.next()){
                    int problemsSolved=r.getInt("problems_count");
                    int existingBadges=r.getInt("badges_count");
                    int newBadgeCount=0;
                    if(problemsSolved==5 && existingBadges==0){
                        newBadgeCount=1;
                        String updateBadgeQuery = "UPDATE users SET badges_count = ? WHERE id = ?";
                        PreparedStatement updateBadgeStmt = conn.prepareStatement(updateBadgeQuery);
                        updateBadgeStmt.setInt(1, newBadgeCount);
                        updateBadgeStmt.setInt(2, userId);
                        updateBadgeStmt.executeUpdate();

                        // Display the congratulatory message
                        showBadgeCongratsWindow(r.getString("username"), newBadgeCount);

                    }
                    else if(problemsSolved==10 && existingBadges==1){
                        newBadgeCount=2;
                        String updateBadgeQuery = "UPDATE users SET badges_count = ? WHERE id = ?";
                        PreparedStatement updateBadgeStmt = conn.prepareStatement(updateBadgeQuery);
                        updateBadgeStmt.setInt(1, newBadgeCount);
                        updateBadgeStmt.setInt(2, userId);
                        updateBadgeStmt.executeUpdate();

                        // Display the congratulatory message
                        showBadgeCongratsWindow(r.getString("username"), newBadgeCount);
                    }
                    else if(problemsSolved==15 && existingBadges==2){
                        newBadgeCount=3;
                        String updateBadgeQuery = "UPDATE users SET badges_count = ? WHERE id = ?";
                        PreparedStatement updateBadgeStmt = conn.prepareStatement(updateBadgeQuery);
                        updateBadgeStmt.setInt(1, newBadgeCount);
                        updateBadgeStmt.setInt(2, userId);
                        updateBadgeStmt.executeUpdate();

                        // Display the congratulatory message
                        showBadgeCongratsWindow(r.getString("username"), newBadgeCount);
                    }

            }

            // Update the HomeWindow if you have a reference to it
                h.reloadHomeWindow();

            }

        } catch (SQLException e) {
            System.out.println("Error updating problems solved count: " + e.getMessage());
        }
        problemsSolved+=5;

        // Calculate badge count based on the number of problems solved
        int newBadgeCount = problemsSolved/5;
        if (newBadgeCount > badgeCount) {
            badgeCount = newBadgeCount; // Update badge count when it crosses the milestone
        }
    }
    public int getProblemsSolved() {
        return problemsSolved;
    }

    // Get the current badge count
    public int getBadgeCount() {
        return badgeCount;
    }

    public void displayResult(String result) {
        /*if (result.contains("Success")) {
            resultPane.setText(result);
            resultPane.setForeground(Color.GREEN); // Set the color to green for success
        } else {
            resultPane.setText(result);
            resultPane.setForeground(Color.RED); // Set the color to red for failure
        }*/
        JTextPane resultPane = new JTextPane();
        resultPane.setEditable(false);
        resultPane.setBackground(new Color(30, 30, 30));

        // Apply different colors for success and failure messages
        StyledDocument doc = resultPane.getStyledDocument();
        Style style = resultPane.addStyle("Style", null);

        try {
            // Split the result by lines to color each line based on its content
            String[] lines = result.split("\n");
            for (String line : lines) {
                if (line.contains("Success! Output matched expected output.")) {
                    StyleConstants.setForeground(style, Color.GREEN); // Green for success
                } else if (line.contains("Failure") || line.contains("Error")) {
                    StyleConstants.setForeground(style, Color.RED); // Red for failure or error
                } else {
                    StyleConstants.setForeground(style, Color.WHITE); // Default color for other lines
                }
                doc.insertString(doc.getLength(), line + "\n", style);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(resultPane);
        scrollPane.setPreferredSize(new Dimension(700, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Compilation and Execution Results", JOptionPane.INFORMATION_MESSAGE);
    }
    public void showBadgeCongratsWindow(String username, int badgeLevel) {
        JFrame congratsFrame = new JFrame("Congratulations!");
        congratsFrame.setSize(400, 300);
        congratsFrame.setLayout(new BorderLayout());

        // Message Label
        JLabel messageLabel = new JLabel("Congratulations " + username + "! You earned a new badge!", JLabel.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setForeground(Color.BLUE);
        congratsFrame.add(messageLabel, BorderLayout.NORTH);

        // Badge Image
        String badgeImagePath = "./resources/" + badgeLevel + ".jpeg"; // Update with actual image paths for each badge level
        ImageIcon badgeIcon = new ImageIcon(badgeImagePath);
        Image img = badgeIcon.getImage();
        Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH); //
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImg));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        congratsFrame.add(iconLabel);

        // Close Button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> congratsFrame.dispose());
        congratsFrame.add(closeButton, BorderLayout.SOUTH);

        congratsFrame.setLocationRelativeTo(null); // Center the window
        congratsFrame.setVisible(true);
    }


}
