# Static Code Analyzer for Variable Usage in Java

**Author:** Janvi Shah  
**Course:** CS-661 – Advanced Project  
**Student ID:** S1377200  
**GitHub Repository:** [https://github.com/janvi18/FallProject](https://github.com/janvi18/FallProject)

---

## 📘 Project Overview

This project is a **Static Code Analyzer** that examines Java source files to detect common **variable usage issues** such as:

- Variables declared but never used  
- Variables used before declaration or initialization  
- Redeclarations within the same scope  
- Shadowing of variables in nested scopes  

The tool helps developers identify code-quality issues early and maintain cleaner, safer Java programs.

---

## ⚙️ Features Implemented (Weeks 1–11)

| Week | Task | Status |
|------|------|---------|
| 1 | Project setup (JDK, Maven, GitHub) | ✅ |
| 2 | Parser research (JavaParser chosen) | ✅ |
| 3 | Basic CLI project structure | ✅ |
| 4 | AST exploration using JavaParser | ✅ |
| 5 | Symbol table design | ✅ |
| 6 | Implemented scope management | ✅ |
| 7 | Analysis Rule 1 – Declared but never used | ✅ |
| 8 | Analysis Rule 2 – Used before declaration/initialization | ✅ |
| 9 | Analysis Rule 3 – Redeclaration & shadowing detection | ✅ |
| 10 | Reporting system (CLI + JSON output) | ✅ |
| 11 | JUnit 5 testing and validation | ✅ |

---

## 🧩 Technical Details

### **Language & Frameworks**
- Java 21 (OpenJDK / Temurin)
- Maven (build tool)
- [JavaParser 3.26.2](https://javaparser.org/)
- JUnit 5 (testing)

### **Project Structure**
### Project Structure

```bash
java-analyzer/
│
├── examples/
│   ├── Sample1.java
│   ├── Sample2.java
│   ├── Sample3.java
│   └── Week9Sample.java
│
├── src/
│   ├── main/java/com/cs661/
│   │   ├── App.java
│   │   ├── AstDemo.java
│   │   ├── StaticCodeAnalyzer.java
│   │   ├── SymbolDemo.java
│   │   └── SymbolTable.java
│   │
│   └── test/java/com/cs661/
│       └── StaticCodeAnalyzerTest.java
│
├── lib/
│   └── javaparser-core-3.26.2.jar
│
├── pom.xml
├── README.md
└── target/
    ├── classes/
    ├── test-classes/
    └── ...