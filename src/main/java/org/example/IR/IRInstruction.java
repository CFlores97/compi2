package org.example.IR;

/** One TAC instruction. The meaning of result/arg1/arg2 depends on the opcode. */
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

    public static IRInstruction assign(Operand target, Operand value) { return new IRInstruction(IROp.ASSIGN, target, value, null); }
    public static IRInstruction bin(IROp op, Operand result, Operand left, Operand right) { return new IRInstruction(op, result, left, right); }
    public static IRInstruction unary(IROp op, Operand result, Operand value) { return new IRInstruction(op, result, value, null); }
    public static IRInstruction addressOf(Operand result, Operand storage) { return new IRInstruction(IROp.ADDR_OF, result, storage, null); }
    public static IRInstruction loadIndirect(Operand result, Operand address) { return new IRInstruction(IROp.LOAD_IND, result, address, null); }
    public static IRInstruction storeIndirect(Operand address, Operand value) { return new IRInstruction(IROp.STORE_IND, address, value, null); }
    public static IRInstruction label(Operand label) { return new IRInstruction(IROp.LABEL, label, null, null); }
    public static IRInstruction goTo(Operand label) { return new IRInstruction(IROp.GOTO, label, null, null); }
    public static IRInstruction ifz(Operand condition, Operand label) { return new IRInstruction(IROp.IFZ, label, condition, null); }
    public static IRInstruction param(Operand value) { return new IRInstruction(IROp.PARAM, null, value, null); }
    public static IRInstruction call(Operand result, Operand function, int count) { return new IRInstruction(IROp.CALL, result, function, Operand.constant(String.valueOf(count))); }
    public static IRInstruction ret(Operand value) { return new IRInstruction(IROp.RETURN, null, value, null); }

    @Override
    public String toString() {
        return switch (op) {
            case ASSIGN -> result + " = " + arg1;
            case ADD, SUB, MUL, DIV, MOD, LT, LE, GT, GE, EQ, NE, AND, OR -> result + " = " + arg1 + " " + operatorText(op) + " " + arg2;
            case NEG -> result + " = -" + arg1;
            case NOT -> result + " = !" + arg1;
            case ADDR_OF -> result + " = &" + arg1;
            case LOAD_IND -> result + " = *" + arg1;
            case STORE_IND -> "*" + result + " = " + arg1;
            case LABEL -> result + ":";
            case GOTO -> "goto " + result;
            case IFZ -> "ifz " + arg1 + " goto " + result;
            case PARAM -> "param " + arg1;
            case CALL -> (result == null ? "" : result + " = ") + "call " + arg1 + ", " + arg2;
            case RETURN -> arg1 == null ? "return" : "return " + arg1;
        };
    }

    private static String operatorText(IROp op) {
        return switch (op) {
            case ADD -> "+"; case SUB -> "-"; case MUL -> "*"; case DIV -> "/"; case MOD -> "%";
            case LT -> "<"; case LE -> "<="; case GT -> ">"; case GE -> ">=";
            case EQ -> "=="; case NE -> "!="; case AND -> "&&"; case OR -> "||";
            default -> throw new IllegalArgumentException("No es operacion binaria: " + op);
        };
    }
}
