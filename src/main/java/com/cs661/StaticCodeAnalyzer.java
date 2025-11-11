package com.cs661;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Week 8: Detect variables used before declaration/initialization.
 */
public class StaticCodeAnalyzer {

    static class VariableInfo {
        final String name;
        final int line;
        boolean used = false;
        boolean initialized = false;
        final String scopePath;

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

    private static final Deque<Scope> scopeStack = new ArrayDeque<>();
    private static final List<VariableInfo> allVariables = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java com.cs661.StaticCodeAnalyzer <JavaSourceFile>");
            return;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File not found: " + file.getAbsolutePath());
            return;
        }

        CompilationUnit cu = StaticJavaParser.parse(file);
        scopeStack.push(new Scope("GLOBAL", null));
        new ScopeVisitor(file.getName()).visit(cu, null);
        scopeStack.pop();

        // --- Report unused ---
        System.out.println("\n--- Analysis Report: Declared but Never Used (Week 7) ---");
        boolean any = false;
        for (VariableInfo v : allVariables) {
            if (!v.used) {
                any = true;
                System.out.printf("⚠️  %s:%d - Variable '%s' in scope [%s] is declared but never used.%n",
                        file.getName(), v.line, v.name, v.scopePath);
            }
        }
        if (!any) System.out.println("✅ No unused variables found.");

        System.out.println("\n--- Analysis Report: Used Before Declaration/Initialization (Week 8) ---");
        if (!Week8Checker.hadWarnings)
            System.out.println("✅ No use-before-declaration or uninitialized uses found.");
        System.out.println("---------------------------------------------------------");
    }

    private static class Week8Checker {
        static boolean hadWarnings = false;

        static void warn(String file, String name, int line, String reason, String scope) {
            hadWarnings = true;
            System.out.printf("⚠️  %s:%d - Variable '%s' used %s [%s]%n", file, line, name, reason, scope);
        }
    }

    private static Scope currentScope() { return scopeStack.peek(); }

    private static Scope findInScopes(String name, Scope from) {
        Scope cur = from;
        while (cur != null) {
            if (cur.variables.containsKey(name)) return cur;
            cur = cur.parent;
        }
        return null;
    }

    private static void enterScope(String label, Node n) {
        String withLine = n.getBegin().map(p -> label + " (line " + p.line + ")").orElse(label);
        scopeStack.push(new Scope(withLine, currentScope()));
    }

    private static void exitScope() { scopeStack.pop(); }

    private static class ScopeVisitor extends VoidVisitorAdapter<Void> {
        final String filename;
        ScopeVisitor(String filename) { this.filename = filename; }

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
                System.out.printf("WARNING: Redeclaration of '%s' in same scope at line %d%n", name, line);
                return;
            }

            Scope outer = findInScopes(name, scope.parent);
            if (outer != null)
                System.out.printf("INFO: '%s' at line %d shadows variable from scope [%s]%n",
                        name, line, outer.variables.get(name).scopePath);

            VariableInfo info = new VariableInfo(name, line, scope.path());
            if (var.getInitializer().isPresent()) info.initialized = true; // has = value
            scope.variables.put(name, info);
            allVariables.add(info);
        }

        @Override
        public void visit(NameExpr expr, Void arg) {
            super.visit(expr, arg);
            Scope scope = currentScope();
            if (scope == null) return;

            String name = expr.getNameAsString();
            int useLine = expr.getBegin().map(p -> p.line).orElse(-1);

            Scope found = findInScopes(name, scope);
            if (found != null) {
                VariableInfo info = found.variables.get(name);
                if (info != null) {
                    // Week 8 check: use before declaration
                    if (useLine < info.line) {
                        Week8Checker.warn(filename, name, useLine, "before declaration", info.scopePath);
                    }
                    // Week 8 check: use before initialization
                    else if (!info.initialized) {
                        Week8Checker.warn(filename, name, useLine, "before initialization", info.scopePath);
                    }
                    info.used = true;
                }
            }
        }

        @Override
        public void visit(AssignExpr assign, Void arg) {
            super.visit(assign, arg);
            String name = assign.getTarget().toString();
            Scope found = findInScopes(name, currentScope());
            if (found != null) {
                VariableInfo info = found.variables.get(name);
                if (info != null) info.initialized = true;
            }
        }
    }
}