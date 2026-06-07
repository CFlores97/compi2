package org.example.semantics;

import java.util.ArrayList;
import java.util.List;

public class SymbolTable {

    // Ámbito global, raíz de todos los demás
    private final Scope globalScope = new Scope("global", null);

    // Lista de todos los ámbitos creados durante el análisis
    private final List<Scope> allScopes = new ArrayList<>();

    // Ámbito en el que nos encontramos actualmente
    private Scope currentScope = globalScope;

    public SymbolTable() {
        // El ámbito global siempre existe desde el inicio
        allScopes.add(globalScope);
    }

    public Scope getCurrentScope() { return currentScope; }

    // Abre un nuevo ámbito hijo del actual (para funciones o bloques)
    public void enterScope(String name) {
        Scope newScope = new Scope(name, currentScope);
        allScopes.add(newScope);
        currentScope = newScope;
    }

    // Cierra el ámbito actual y regresa al padre
    public void exitScope() {
        if (currentScope.getParent() != null)
            currentScope = currentScope.getParent();
    }

    // Define un símbolo en el ámbito actual
    public boolean define(Symbol symbol) {
        return currentScope.define(symbol);
    }

    // Busca un símbolo desde el ámbito actual hacia afuera
    public Symbol resolve(String name) {
        return currentScope.resolve(name);
    }

    // Imprime todos los ámbitos y sus símbolos
    @Override
    public String toString() {
        String result = "\n=== TABLA DE SIMBOLOS ===\n";

        for (Scope scope : allScopes) {
            result += "\nAmbito: " + scope.getName() + "\n";

            if (scope.getSymbols().isEmpty()) {
                result += "  (sin simbolos)\n";
            } else {
                for (Symbol s : scope.getSymbols().values()) {
                    result += "  " + s + "\n";
                }
            }
        }

        return result;
    }
}