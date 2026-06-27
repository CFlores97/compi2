package org.example.IR;

/** A value used by a TAC instruction. */
public final class Operand {
    public enum Kind { TEMP, CONST, VAR, GLOBAL, LABEL, FUNC, STRING }

    public final Kind kind;
    public final String value;

    private Operand(Kind kind, String value) {
        this.kind = kind;
        this.value = value;
    }

    public static Operand temp(String name) { return new Operand(Kind.TEMP, name); }
    public static Operand constant(String value) { return new Operand(Kind.CONST, value); }
    public static Operand variable(String name) { return new Operand(Kind.VAR, name); }
    public static Operand global(String label) { return new Operand(Kind.GLOBAL, label); }
    public static Operand label(String name) { return new Operand(Kind.LABEL, name); }
    public static Operand function(String name) { return new Operand(Kind.FUNC, name); }
    public static Operand string(String label) { return new Operand(Kind.STRING, label); }

    public boolean isConst() { return kind == Kind.CONST; }
    public int intValue() { return Integer.parseInt(value); }

    @Override public String toString() { return value; }
}
