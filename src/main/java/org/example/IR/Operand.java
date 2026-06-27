package org.example.IR;


// Esta clase es importante porque ayuda a determinar que tipo es cada valor del programa
public class Operand {

    // Enumera los tipos que pueden ser un valor
    // TEMP es una variable que crea el compilador para resultados intermedios
    // CONST con valores directos que vienen del AST
    // VAR son las variables creadas en el programa
    // LABEL son como etiquetas que ayudan a determinar el inicio y final de bloques como ifs o whiles
    public enum Kind {TEMP, CONST, VAR, LABEL, FUNC}

    public final Kind kind;
    public final String value;

    // Constructor
    private Operand(Kind kind, String value) {
        this.kind = kind;
        this.value = value;
    }

    // Getters para cada tipo de operando

    public static Operand temp(String n) {
        return new Operand(Kind.TEMP, n);
    }

    public static Operand constant(String n) {
        return new Operand(Kind.CONST, n);
    }

    public static Operand variable(String n) {
        return new Operand(Kind.VAR, n);
    }

    public static Operand label(String n) {
        return new Operand(Kind.LABEL, n);
    }

    public static Operand function(String n) { return new Operand(Kind.FUNC, n); }

    public boolean isConst(){
        return kind == Kind.CONST;
    }

    public int intValue() {
        return Integer.parseInt(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
