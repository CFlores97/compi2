package org.example.semantics;

import java.util.List;

// Extiende Symbol agregando la firma de la función:
// tipo de retorno (heredado) + lista de tipos de parámetros
public class FunctionSymbol extends Symbol {

    private final List<MiniCType> paramTypes;

    public FunctionSymbol(String name, MiniCType returnType,
                          List<MiniCType> paramTypes,
                          int line, int column) {
        super(name, SymbolKind.FUNCTION, returnType, line, column);
        this.paramTypes = paramTypes;
    }

    public List<MiniCType> getParamTypes() { return paramTypes; }

    @Override
    public String toString() {
        return super.toString() + " params=" + paramTypes;
    }
}