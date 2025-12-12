Static Code Analyzer

CS-661 Fall Project

Overview

This project is a static code analyzer for Java developed incrementally throughout the CS-661 course.
It uses JavaParser to analyze Java source files and detect common variable-related issues such as scope errors, unused variables, and incorrect declarations.

The analyzer produces a clean command-line report and optionally exports results in JSON format.

⸻

Features

The analyzer currently detects:
	•	Declared but never used variables (Week 7)
	•	Variables used before declaration or initialization (Week 8)
	•	Variable shadowing across nested scopes (Week 9)
	•	Redeclaration of variables in the same scope (Week 9)
	•	Formatted CLI reporting system with severity levels (Week 10)
	•	Optional JSON export for machine-readable output (Week 10)
	•	JUnit 5 test suite validating analyzer behavior (Week 11)

⸻

Project Structure

java-analyzer/
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


⸻

Requirements
	•	Java 21
	•	Apache Maven
	•	macOS / Linux / Windows

⸻

Setup

Clone the repository and build the project:

git clone https://github.com/janvi18/FallProject.git
cd java-analyzer
mvn clean compile


⸻

Usage

Run Analyzer (CLI Output)

java -cp "target/classes:lib/*" com.cs661.StaticCodeAnalyzer examples/Sample2.java

Run Analyzer (JSON Output)

java -cp "target/classes:lib/*" com.cs661.StaticCodeAnalyzer examples/Week9Sample.java --json


⸻

Example Output (CLI)

=========================================================
 Static Code Analysis Report for Sample2.java
=========================================================
[INFO] (SHADOWING) Sample2.java:10 - Variable 'a2' shadows a variable from an outer scope.
[WARNING] (UNUSED_VARIABLE) Sample2.java:6 - Variable 'a' is declared but never used.


⸻

Example Output (JSON)

{
  "file": "Week9Sample.java",
  "issues": [
    {
      "type": "SHADOWING",
      "severity": "INFO",
      "line": 6,
      "variable": "x",
      "message": "Variable 'x' shadows a variable from an outer scope."
    }
  ]
}


⸻

Testing

Unit tests are implemented using JUnit 5.

Run tests with:

mvn test

All tests pass successfully.

⸻

Documentation & Code Quality (Week 12)
	•	Inline documentation added to core classes
	•	Code refactored for clarity and maintainability
	•	Clean separation of concerns (analysis, scope tracking, reporting)
	•	Consistent formatting and naming conventions
	•	Maven project structure cleaned and verified

⸻

Limitations & Future Work
	•	No full data-flow analysis
	•	No method-call or control-flow tracking
	•	Future improvements could include:
	•	Unused import detection
	•	Dead code analysis
	•	Method-level and inter-procedural analysis

⸻

Author

Janvi Shah
CS-661 – Fall Semester