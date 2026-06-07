package org.example.semantics;

public class Symbol {
    private final String name;
    private final SymbolKind kind;
    private final MiniCType type;
    private final int line;
    private final int column;

    public Symbol(String name, SymbolKind kind, MiniCType type, int line, int column) {
        this.name   = name;
        this.kind   = kind;
        this.type   = type;
        this.line   = line;
        this.column = column;
    }

    public String getName()    { return name; }
    public SymbolKind getKind(){ return kind; }
    public MiniCType getType() { return type; }
    public int getLine()       { return line; }
    public int getColumn()     { return column; }

    @Override
    public String toString() {
        return kind + " " + name + " : " + type
                + " (linea " + line + ", columna " + column + ")";
    }
}