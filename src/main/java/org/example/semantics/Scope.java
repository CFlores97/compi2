package org.example.semantics;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Scope {
    private final String name;
    private final Scope parent;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public Scope(String name, Scope parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() { return name; }
    public Scope getParent() { return parent; }
    public Map<String, Symbol> getSymbols() { return symbols; }

    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) return false;
        symbols.put(symbol.getName(), symbol);
        return true;
    }

    public Symbol resolveLocal(String name) { return symbols.get(name); }

    public Symbol resolve(String name) {
        Symbol found = symbols.get(name);
        return found != null ? found : parent == null ? null : parent.resolve(name);
    }
}
