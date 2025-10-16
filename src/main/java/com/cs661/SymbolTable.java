package com.cs661;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {
    /** A declared variable. */
    public static class Symbol {
        public final String name;
        public final String scopePath;
        public final int line;

        public Symbol(String name, String scopePath, int line) {
            this.name = name; this.scopePath = scopePath; this.line = line;
        }
    }

    private static class Scope {
        final String kind;         // "class", "method", "block"
        final String label;        // e.g., class name, method name, or "#1"
        final Scope parent;
        final Map<String, Symbol> symbols = new LinkedHashMap<>();

        Scope(String kind, String label, Scope parent) {
            this.kind = kind; this.label = label; this.parent = parent;
        }

        String path() {
            Deque<String> parts = new ArrayDeque<>();
            for (Scope s = this; s != null; s = s.parent) {
                parts.addFirst(s.kind + ":" + s.label);
            }
            return String.join(">", parts);
        }
    }

    private final Deque<Scope> stack = new ArrayDeque<>();
    private final List<Symbol> all = new ArrayList<>();
    private int blockCounter = 0;

    public void enterClass(String name)   { stack.push(new Scope("class",  name, stack.peek())); }
    public void enterMethod(String name)  { stack.push(new Scope("method", name, stack.peek())); }
    public void enterBlock()              { stack.push(new Scope("block",  "#" + (++blockCounter), stack.peek())); }
    public void exitScope()               { if (!stack.isEmpty()) stack.pop(); }

    public void declare(String name, int line) {
        Scope cur = stack.peek();
        if (cur == null) { // file-level fallback
            cur = new Scope("file", "<top>", null);
            stack.push(cur);
        }
        Symbol sym = new Symbol(name, cur.path(), line);
        cur.symbols.put(name, sym);
        all.add(sym);
    }

    public List<Symbol> allSymbols() { return Collections.unmodifiableList(all); }
}
