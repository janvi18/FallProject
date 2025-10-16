package com.cs661;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class SymbolDemo {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -cp target/java-analyzer-1.0-SNAPSHOT.jar com.cs661.SymbolDemo <examples/Sample1.java>");
            System.exit(1);
        }

        String code = Files.readString(Path.of(args[0]));
        CompilationUnit cu = StaticJavaParser.parse(code);

        SymbolTable table = new SymbolTable();

        // Visitor that manages scopes & collects declarations
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                table.enterClass(n.getNameAsString());
                super.visit(n, arg);
                table.exitScope();
            }

            @Override
            public void visit(MethodDeclaration n, Void arg) {
                table.enterMethod(n.getNameAsString());
                super.visit(n, arg);
                table.exitScope();
            }

            @Override
            public void visit(BlockStmt n, Void arg) {
                table.enterBlock();
                super.visit(n, arg);
                table.exitScope();
            }

            @Override
            public void visit(VariableDeclarator v, Void arg) {
                int line = v.getName().getBegin().map(p -> p.line).orElse(-1);
                table.declare(v.getNameAsString(), line);
                super.visit(v, arg);
            }
        }.visit(cu, null);

        // Print the symbol table for this file
        System.out.println("=== Symbol Table (declarations) ===");
        table.allSymbols().forEach(s ->
            System.out.printf("name=%-12s line=%-4d scope=%s%n", s.name, s.line, s.scopePath)
        );
    }
}
