package org.example.IR;

public final class IRInstruction {

    public final IROp op;
    public final Operand result;
    public final Operand arg1;
    public final Operand arg2;

    public IRInstruction(IROp op, Operand result, Operand arg1, Operand arg2) {
        this.op = op;
        this.result = result;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public static IRInstruction bin(IROp op, Operand result, Operand left, Operand right) {
        return new IRInstruction(op, result, left, right);
    }

    public static IRInstruction one(IROp op, Operand result, Operand arg1) {
        return new IRInstruction(op, result, arg1, null);
    }

    public static IRInstruction assign(Operand target, Operand value) {
        return new IRInstruction(IROp.ASSIGN, target, value, null);
    }

    public static IRInstruction unary(IROp op, Operand result, Operand value) {
        return new IRInstruction(op, result, value, null);
    }

    public static IRInstruction label(Operand label) {
        return new IRInstruction(IROp.LABEL, label, null, null);
    }

    public static IRInstruction goTo(Operand label) {
        return new IRInstruction(IROp.GOTO, label, null, null);
    }

    public static IRInstruction ifz(Operand condition, Operand label) {
        return new IRInstruction(IROp.IFZ, label, condition, null);
    }

    public static IRInstruction param(Operand value) {
        return new IRInstruction(IROp.PARAM, null, value, null);
    }

    public static IRInstruction call(Operand result, Operand function, int argumentCount) {
        return new IRInstruction(
                IROp.CALL,
                result,
                function,
                Operand.constant(String.valueOf(argumentCount))
        );
    }

    public static IRInstruction ret(Operand value) {
        return new IRInstruction(IROp.RETURN, null, value, null);
    }

    @Override
    public String toString() {
        return switch (op) {
            case ASSIGN -> result + " = " + arg1;

            case ADD, SUB, MUL, DIV, MOD,
                 LT, LE, GT, GE, EQ, NE,
                 AND, OR ->
                    result + " = " + arg1 + " " + operatorText(op) + " " + arg2;

            case NEG -> result + " = -" + arg1;
            case NOT -> result + " = !" + arg1;

            case LABEL -> result + ":";
            case GOTO -> "goto " + result;
            case IFZ -> "ifz " + arg1 + " goto " + result;
            case PARAM -> "param " + firstNonNull(arg1, result);

            case CALL -> {
                String destination = result == null ? "" : result + " = ";
                String argumentCount = arg2 == null ? "" : ", " + arg2;
                yield destination + "call " + arg1 + argumentCount;
            }

            case RETURN -> {
                Operand value = firstNonNull(arg1, result);
                yield value == null ? "return" : "return " + value;
            }
        };
    }

    private static Operand firstNonNull(Operand first, Operand second) {
        return first != null ? first : second;
    }

    private static String operatorText(IROp op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case MOD -> "%";
            case LT -> "<";
            case LE -> "<=";
            case GT -> ">";
            case GE -> ">=";
            case EQ -> "==";
            case NE -> "!=";
            case AND -> "&&";
            case OR -> "||";
            default -> throw new IllegalArgumentException(
                    "No es una operacion binaria: " + op
            );
        };
    }
}