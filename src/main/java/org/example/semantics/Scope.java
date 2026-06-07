package org.example.semantics;

import java.util.LinkedHashMap;
import java.util.Map;

public class Scope {
    private final String name;
    private final Scope parent;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public Scope(String name, Scope parent) {
        this.name   = name;
        this.parent = parent;
    }

    public String getName()              { return name; }
    public Scope getParent()             { return parent; }
    public Map<String, Symbol> getSymbols() { return symbols; }

    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) return false;
        symbols.put(symbol.getName(), symbol);
        return true;
    }

    public Symbol resolve(String name) {
        Symbol found = symbols.get(name);
        if (found != null) return found;
        if (parent != null) return parent.resolve(name);
        return null;
    }
}