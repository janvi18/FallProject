package com.cs661;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.printer.YamlPrinter;

import java.nio.file.Files;
import java.nio.file.Path;

public class AstDemo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar java-analyzer.jar <path/to/File.java>");
            System.exit(1);
        }

        // Load source code
        String code = Files.readString(Path.of(args[0]));
        CompilationUnit cu = StaticJavaParser.parse(code);

        // 1) Print AST tree in YAML
        System.out.println("=== AST (YAML) ===");
        System.out.println(new YamlPrinter(true).output(cu));

        // 2) Print variable declarations
        System.out.println("\n=== Variable Declarations ===");
        cu.findAll(VariableDeclarator.class).forEach(v -> {
            String name = v.getNameAsString();
            int line = v.getName().getBegin().map(p -> p.line).orElse(-1);
            System.out.printf("declare: %s (line %d)%n", name, line);
        });

        // 3) Print variable usages (NameExpr nodes)
        System.out.println("\n=== Variable Usages (NameExpr) ===");
        cu.findAll(NameExpr.class).forEach(ne -> {
            String name = ne.getNameAsString();
            int line = ne.getBegin().map(p -> p.line).orElse(-1);
            System.out.printf("use: %s (line %d)%n", name, line);
        });
    }
}
