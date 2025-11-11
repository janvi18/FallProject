package com.cs661;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.util.*;

/**
 * Static Code Analyzer (Weeks 6–10)
 */
public class StaticCodeAnalyzer {

    static class VariableInfo {
        final String name;
        final int line;
        final String scopePath;
        boolean used = false;
        boolean initialized = false;

        VariableInfo(String name, int line, String scopePath) {
            this.name = name;
            this.line = line;
            this.scopePath = scopePath;
        }
    }

    static class Scope {
        final String name;
        final Scope parent;
        final Map<String, VariableInfo> variables = new HashMap<>();

        Scope(String name, Scope parent) {
            this.name = name;
            this.parent = parent;
        }

        String path() {
            Deque<String> parts = new ArrayDeque<>();
            Scope cur = this;
            while (cur != null) {
                parts.push(cur.name);
                cur = cur.parent;
            }
            return String.join(" > ", parts);
        }
    }

    static class Issue {
        final String type;
        final String severity;
        final String file;
        final int line;
        final String varName;
        final String message;
        final String suggestion;
        final String scopePath;

        Issue(String type, String severity, String file, int line,
              String varName, String message, String suggestion, String scopePath) {
            this.type = type;
            this.severity = severity;
            this.file = file;
            this.line = line;
            this.varName = varName;
            this.message = message;
            this.suggestion = suggestion;
            this.scopePath = scopePath;
        }
    }

    private static final Deque<Scope> scopeStack = new ArrayDeque<>();
    private static final List<VariableInfo> allVariables = new ArrayList<>();
    private static final List<Issue> issues = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: java com.cs661.StaticCodeAnalyzer <JavaSourceFile> [--json]");
            return;
        }

        String filePath = args[0];
        boolean jsonRequested = (args.length == 2 && "--json".equalsIgnoreCase(args[1]));

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        String fileName = file.getName();
        CompilationUnit cu = StaticJavaParser.parse(file);

        scopeStack.clear();
        allVariables.clear();
        issues.clear();

        scopeStack.push(new Scope("GLOBAL", null));
        new ScopeVisitor(fileName).visit(cu, null);
        scopeStack.pop();

        // Week 7: declared but never used
        for (VariableInfo v : allVariables) {
            if (!v.used) {
                issues.add(new Issue(
                        "UNUSED_VARIABLE",
                        "WARNING",
                        fileName,
                        v.line,
                        v.name,
                        String.format("Variable '%s' is declared but never used.", v.name),
                        "Remove it if unnecessary, or use it if this is a bug.",
                        v.scopePath
                ));
            }
        }

        printTextReport(fileName);

        if (jsonRequested) {
            System.out.println();
            printJsonReport(fileName);
        }
    }

    private static class ScopeVisitor extends VoidVisitorAdapter<Void> {
        final String fileName;

        ScopeVisitor(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            enterScope("class " + n.getNameAsString(), n);
            super.visit(n, arg);
            exitScope();
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            enterScope("method " + n.getNameAsString() + "()", n);
            super.visit(n, arg);
            exitScope();
        }

        @Override
        public void visit(BlockStmt n, Void arg) {
            enterScope("block", n);
            super.visit(n, arg);
            exitScope();
        }

        @Override
        public void visit(VariableDeclarator var, Void arg) {
            super.visit(var, arg);

            Scope scope = currentScope();
            if (scope == null) return;

            String name = var.getNameAsString();
            int line = var.getBegin().map(p -> p.line).orElse(-1);

            if (scope.variables.containsKey(name)) {
                issues.add(new Issue(
                        "REDECLARATION",
                        "WARNING",
                        fileName,
                        line,
                        name,
                        String.format("Variable '%s' is redeclared in the same scope.", name),
                        "Rename or remove the duplicate declaration.",
                        scope.path()
                ));
                return;
            }

            Scope outer = findInScopes(name, scope.parent);
            if (outer != null) {
                VariableInfo outerInfo = outer.variables.get(name);
                if (outerInfo != null) {
                    issues.add(new Issue(
                            "SHADOWING",
                            "INFO",
                            fileName,
                            line,
                            name,
                            String.format("Variable '%s' shadows a variable from an outer scope.", name),
                            "Consider renaming to avoid confusion.",
                            scope.path()
                    ));
                }
            }

            VariableInfo info = new VariableInfo(name, line, scope.path());
            if (var.getInitializer().isPresent()) {
                info.initialized = true;
            }

            scope.variables.put(name, info);
            allVariables.add(info);
        }

        @Override
        public void visit(NameExpr expr, Void arg) {
            super.visit(expr, arg);

            Scope scope = currentScope();
            if (scope == null) return;

            String name = expr.getNameAsString();
            int line = expr.getBegin().map(p -> p.line).orElse(-1);

            Scope found = findInScopes(name, scope);
            if (found == null) {
                issues.add(new Issue(
                        "UNDECLARED_VARIABLE",
                        "WARNING",
                        fileName,
                        line,
                        name,
                        String.format("Variable '%s' is used but not declared in any visible scope.", name),
                        "Declare the variable before using it.",
                        scope.path()
                ));
                return;
            }

            VariableInfo info = found.variables.get(name);
            if (info == null) return;

            if (line < info.line) {
                issues.add(new Issue(
                        "USE_BEFORE_DECLARATION",
                        "WARNING",
                        fileName,
                        line,
                        name,
                        String.format("Variable '%s' is used before its declaration.", name),
                        "Move the declaration above this usage.",
                        info.scopePath
                ));
            } else if (!info.initialized) {
                issues.add(new Issue(
                        "USE_BEFORE_INITIALIZATION",
                        "WARNING",
                        fileName,
                        line,
                        name,
                        String.format("Variable '%s' might be used before being initialized.", name),
                        "Ensure it is assigned a value before this line.",
                        info.scopePath
                ));
            }

            info.used = true;
        }

        @Override
        public void visit(AssignExpr assign, Void arg) {
            super.visit(assign, arg);

            String target = assign.getTarget().toString();
            Scope scope = currentScope();
            if (scope == null) return;

            Scope found = findInScopes(target, scope);
            if (found != null) {
                VariableInfo info = found.variables.get(target);
                if (info != null) {
                    info.initialized = true;
                }
            }
        }
    }

    private static Scope currentScope() {
        return scopeStack.peek();
    }

    private static Scope findInScopes(String name, Scope from) {
        Scope cur = from;
        while (cur != null) {
            if (cur.variables.containsKey(name)) {
                return cur;
            }
            cur = cur.parent;
        }
        return null;
    }

    private static void enterScope(String label, Node n) {
        String withLine = n.getBegin()
                .map(p -> label + " (line " + p.line + ")")
                .orElse(label);
        scopeStack.push(new Scope(withLine, currentScope()));
    }

    private static void exitScope() {
        scopeStack.pop();
    }

    private static void printTextReport(String fileName) {
        System.out.println("=========================================================");
        System.out.println(" Static Code Analysis Report for " + fileName);
        System.out.println("=========================================================");

        if (issues.isEmpty()) {
            System.out.println("No issues found. ✅");
            return;
        }

        for (Issue i : issues) {
            System.out.printf("[%s] (%s) %s:%d - %s%n",
                    i.severity, i.type, i.file, i.line, i.message);
            if (i.scopePath != null && !i.scopePath.isEmpty()) {
                System.out.println("      Scope: " + i.scopePath);
            }
            if (i.suggestion != null && !i.suggestion.isEmpty()) {
                System.out.println("      Suggestion: " + i.suggestion);
            }
        }
    }

    private static void printJsonReport(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"file\": \"").append(escapeJson(fileName)).append("\",\n");
        sb.append("  \"issues\": [\n");

        for (int idx = 0; idx < issues.size(); idx++) {
            Issue i = issues.get(idx);
            sb.append("    {\n");
            sb.append("      \"type\": \"").append(escapeJson(i.type)).append("\",\n");
            sb.append("      \"severity\": \"").append(escapeJson(i.severity)).append("\",\n");
            sb.append("      \"file\": \"").append(escapeJson(i.file)).append("\",\n");
            sb.append("      \"line\": ").append(i.line).append(",\n");
            sb.append("      \"variable\": \"").append(escapeJson(i.varName)).append("\",\n");
            sb.append("      \"message\": \"").append(escapeJson(i.message)).append("\",\n");
            sb.append("      \"suggestion\": \"").append(escapeJson(i.suggestion)).append("\",\n");
            sb.append("      \"scope\": \"").append(escapeJson(i.scopePath)).append("\"\n");
            sb.append("    }");
            if (idx < issues.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("  ]\n");
        sb.append("}\n");

        System.out.println(sb.toString());
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}