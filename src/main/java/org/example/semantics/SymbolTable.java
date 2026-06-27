package org.example.semantics;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    private final Scope globalScope = new Scope("global", null);
    private final List<Scope> allScopes = new ArrayList<>();
    private Scope currentScope = globalScope;

    public SymbolTable() {
        allScopes.add(globalScope);
        registerRuntimeFunctions();
    }

    public Scope getGlobalScope() { return globalScope; }
    public Scope getCurrentScope() { return currentScope; }

    public void enterScope(String name) {
        currentScope = new Scope(name, currentScope);
        allScopes.add(currentScope);
    }

    public void exitScope() {
        if (currentScope.getParent() != null) currentScope = currentScope.getParent();
    }

    public boolean define(Symbol symbol) { return currentScope.define(symbol); }
    public boolean defineGlobal(Symbol symbol) { return globalScope.define(symbol); }
    public Symbol resolve(String name) { return currentScope.resolve(name); }
    public Symbol resolveGlobal(String name) { return globalScope.resolveLocal(name); }

    private void registerRuntimeFunctions() {
        defineRuntime("print_int", MiniCType.scalar("void"), List.of(MiniCType.scalar("int")));
        defineRuntime("print_char", MiniCType.scalar("void"), List.of(MiniCType.scalar("char")));
        defineRuntime("print_bool", MiniCType.scalar("void"), List.of(MiniCType.scalar("bool")));
        defineRuntime("print_str", MiniCType.scalar("void"), List.of(MiniCType.scalar("string")));
        defineRuntime("println", MiniCType.scalar("void"), List.of());
        defineRuntime("read_int", MiniCType.scalar("int"), List.of());
        defineRuntime("read_char", MiniCType.scalar("char"), List.of());
        defineRuntime("read_str", MiniCType.scalar("void"), List.of(new MiniCType("char", 1, List.of()), MiniCType.scalar("int")));
    }

    private void defineRuntime(String name, MiniCType result, List<MiniCType> params) {
        globalScope.define(new FunctionSymbol(name, result, params, 0, 0));
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("\n=== TABLA DE SIMBOLOS ===\n");
        for (Scope scope : allScopes) {
            result.append("\nAmbito: ").append(scope.getName()).append("\n");
            if (scope.getSymbols().isEmpty()) result.append("  (sin simbolos)\n");
            for (Symbol symbol : scope.getSymbols().values()) result.append("  ").append(symbol).append("\n");
        }
        return result.toString();
    }
}
