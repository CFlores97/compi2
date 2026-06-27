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
        registerRuntimeFunctions();
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

    private void registerRuntimeFunctions() {
        globalScope.define(new FunctionSymbol(
                "print_int",
                new MiniCType("void", false, 0),
                List.of(new MiniCType("int", false, 0)),
                0,
                0
        ));

        globalScope.define(new FunctionSymbol(
                "print_char",
                new MiniCType("void", false, 0),
                List.of(new MiniCType("char", false, 0)),
                0,
                0
        ));

        globalScope.define(new FunctionSymbol(
                "print_bool",
                new MiniCType("void", false, 0),
                List.of(new MiniCType("bool", false, 0)),
                0,
                0
        ));

        globalScope.define(new FunctionSymbol(
                "println",
                new MiniCType("void", false, 0),
                List.of(),
                0,
                0
        ));

        globalScope.define(new FunctionSymbol(
                "read_int",
                new MiniCType("int", false, 0),
                List.of(),
                0,
                0
        ));

        globalScope.define(new FunctionSymbol(
                "read_char",
                new MiniCType("char", false, 0),
                List.of(),
                0,
                0
        ));
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