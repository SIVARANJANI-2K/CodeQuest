import javax.tools.*;
import java.io.*;
import java.util.*;

public class CodeCompiler {

    // Inner class to represent a test case
    static class TestCase {
        String input;
        String expectedOutput;
        boolean isPublic;

        TestCase(String input, String expectedOutput,boolean isPublic) {
            this.input = input;
            this.expectedOutput = expectedOutput;
            this.isPublic=isPublic;
        }
        public String getInput() {
            return input;
        }
    }

    public static String compileAndRun(String className, String code, List<TestCase> testCases) {
        StringBuilder result = new StringBuilder();

        try {
            File sourceFile = new File(className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(code);
            }

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                return "Compiler not available. Make sure to run with a JDK installed.";
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
            boolean success = task.call();

            if (success) {
                result.append("Compilation successful!\n");
                for (int i = 0; i < testCases.size(); i++) {
                    TestCase testCase = testCases.get(i);
                    result.append("Test Case ").append(i + 1).append(":\n");

                    if (testCase.isPublic) {
                        result.append("Input: ").append(testCase.input).append("\n");
                        result.append("Expected Output: ").append(testCase.expectedOutput).append("\n");
                    }

                    result.append(executeCompiledClass(className, testCase.input, testCase.expectedOutput)).append("\n");
                }
            } else {
                result.append("Compilation failed:\n");
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    result.append(diagnostic.getMessage(null)).append("\n");
                }
            }

            sourceFile.delete();
            fileManager.close();

        } catch (IOException e) {
            result.append("Error during compilation: ").append(e.getMessage());
        }

        return result.toString();
    }


    private static String executeCompiledClass(String className, String input, String expectedOutput) {
        try {
            // Start the compiled class in a new process
            ProcessBuilder processBuilder = new ProcessBuilder("java", className);
            Process process = processBuilder.start();

            // Write the test case input to the process's output stream only if it's not empty
            if (input!=null && !input.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    writer.write(input);
                    writer.newLine();
                    writer.flush();
                }
            }

            // Capture the output of the class execution
            String actualOutput = new String(process.getInputStream().readAllBytes()).trim();
            String errorOutput = new String(process.getErrorStream().readAllBytes()).trim();
            process.waitFor();

            if (!errorOutput.isEmpty()) {
                return "Execution Error:\n" + errorOutput;
            }

            // Compare actual output to expected output
            if (actualOutput.equals(expectedOutput)) {
                return "Success! Output matched expected output.";
            } else {
                return "Failure. Expected: " + expectedOutput + ", but got: " + actualOutput;
            }

        } catch (IOException | InterruptedException e) {
            return "Error executing compiled class: " + e.getMessage();
        }
    }
}
