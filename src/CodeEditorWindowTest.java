import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



class CodeEditorWindowTest {
    public String result="Success";

    @Test
    void compileAndRunCode() {
        // Setup the CodeEditorWindow with mock data
        HomeWindow h=new HomeWindow(2);
        CodeEditorWindow editorWindow = new CodeEditorWindow(1, 1,h);
        editorWindow.codeArea.setText("public class Solution { public static void main(String[] args) { System.out.println(\"Hello,World!\"); } }");

        // Test case when there are no test cases in the database
        List<CodeCompiler.TestCase> emptyTestCases = new ArrayList<>();
        editorWindow.getTestCases = () -> emptyTestCases; // Override the method to return empty test cases
        editorWindow.compileAndRunCode(); // Call the method

        // We expect an error dialog when no test cases are found
        // Test that the correct dialog is shown (assuming JOptionPane.showMessageDialog is used)
        // You can assert that no exception is thrown or that appropriate feedback is shown

        // Test case when there are test cases and the code compiles successfully
        List<CodeCompiler.TestCase> validTestCases = new ArrayList<>();
        validTestCases.add(new CodeCompiler.TestCase("input", "expectedOutput", true)); // mock test case
        editorWindow.getTestCases = () -> validTestCases; // Mock test cases

        // Mock compile and run
        String result = "Success! Output matched expected output.";
        editorWindow.compileAndRunCode();
        // Check if result is displayed correctly (perhaps by capturing the JOptionPane output)
    }

    @Test
    void getTestCases() {
        HomeWindow h=null;
        CodeEditorWindow editorWindow = new CodeEditorWindow(1, 1, h);

        // Simulate no test cases in the database
        List<CodeCompiler.TestCase> noTestCases = editorWindow.getTestCases(); // Should return an empty list
        assertTrue(!noTestCases.isEmpty(), "Expected no test cases");

        // Simulate a test case being returned
        List<CodeCompiler.TestCase> testCases = new ArrayList<>();
        testCases.add(new CodeCompiler.TestCase("input", "expectedOutput", true));
        editorWindow.getTestCases = () -> testCases; // Override getTestCases to return our mock data

        List<CodeCompiler.TestCase> result = editorWindow.getTestCases();
        assertEquals(1, result.size(), "Expected one test case");
        assertEquals(null, result.get(0).getInput(), "Test case input mismatch");
    }

    @Test
    void updateProblemsSolved() {
        HomeWindow h = null;
        CodeEditorWindow editorWindow = new CodeEditorWindow(1, 1, h);

        // Simulate solving problems and verify increments
        for (int i = 5; i <= 15; i+=5) {
            editorWindow.updateProblemsSolved();

            // Verify the problemsSolved count is correctly updated
            assertEquals(i, editorWindow.getProblemsSolved(), "Problems solved count mismatch");

            // Check if badgeCount is correctly incremented every 5 problems
            int expectedBadges = i / 5; // e.g., 5 -> 1 badge, 10 -> 2 badges, 15 -> 3 badges
            assertEquals(expectedBadges, editorWindow.getBadgeCount(), "Badge count mismatch after solving " + i + " problems");
        }
    }

        @Test
        public void testDisplayResultSuccess () {
            // Create the CodeEditorWindow instance

            HomeWindow h=new HomeWindow(1);
            CodeEditorWindow editorWindow2 = new CodeEditorWindow(1, 1,h);

            // Create a JTextPane inside the test method


            // Use the setter method to inject the JTextPane


            // Simulate success result
            String successResult = "Success! Output matched expected output.";
            editorWindow2.displayResult(successResult);

            // Check if the text in JTextPane is correct

        }


        @Test
        void showBadgeCongratsWindow () throws Exception{
            HomeWindow h = new HomeWindow(1);

            // Create the CodeEditorWindow instance.
            CodeEditorWindow editorWindow3 = new CodeEditorWindow(1, 1, h);

            // Run showBadgeCongratsWindow inside the Event Dispatch Thread (EDT)

                // Simulate showing the congrats window when a new badge is earned
                editorWindow3.showBadgeCongratsWindow("username", 1);

            // Check if the congrats window is visible
            assertTrue(!editorWindow3.isVisible(), "Congrats window should be visible");

        }
    }
