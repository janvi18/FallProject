package com.cs661;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class StaticCodeAnalyzerTest {

    private String runAnalyzer(String path, String... extraArgs) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            String[] args;
            if (extraArgs != null && extraArgs.length > 0) {
                args = new String[1 + extraArgs.length];
                args[0] = path;
                System.arraycopy(extraArgs, 0, args, 1, extraArgs.length);
            } else {
                args = new String[]{ path };
            }
            StaticCodeAnalyzer.main(args);
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString();
    }

    @Test
    void sample2_reportsUnusedVariables() throws Exception {
        String out = runAnalyzer("examples/Sample2.java");
        assertTrue(out.contains("Variable 'a2' is declared but never used."),
                "Expected unused variable warning for 'a2'");
        assertTrue(out.contains("Variable 'a' is declared but never used."),
                "Expected unused variable warning for 'a'");
    }

    @Test
    void week9Sample_reportsShadowingAndRedeclaration() throws Exception {
        String out = runAnalyzer("examples/Week9Sample.java");
        assertTrue(out.contains("SHADOWING") || out.contains("shadows a variable from an outer scope"),
                "Expected shadowing info for 'x'");
        assertTrue(out.contains("REDECLARATION") || out.contains("is redeclared in the same scope"),
                "Expected redeclaration warning for 'y'");
    }

    @Test
    void jsonFlag_producesJsonOutput() throws Exception {
        String out = runAnalyzer("examples/Week9Sample.java", "--json");
        assertTrue(out.contains("\"file\": \"Week9Sample.java\""),
                "JSON output should contain file name");
        assertTrue(out.contains("\"issues\": ["),
                "JSON output should contain issues array");
    }
}