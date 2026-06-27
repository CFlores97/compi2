package org.example.IR;

import java.util.ArrayList;
import java.util.List;

/** Required optimization: evaluates TAC arithmetic when both operands are constants. */
public final class ConstantFolder {
    public IRProgram optimize(IRProgram program) {
        for (IRFunction function : program.functions) {
            List<IRInstruction> output = new ArrayList<>();
            for (IRInstruction instruction : function.code) {
                if (foldable(instruction)) {
                    int left = instruction.arg1.intValue();
                    int right = instruction.arg2.intValue();
                    output.add(IRInstruction.assign(instruction.result, Operand.constant(String.valueOf(evaluate(instruction.op, left, right)))));
                } else output.add(instruction);
            }
            function.code.clear();
            function.code.addAll(output);
        }
        return program;
    }

    private boolean foldable(IRInstruction i) {
        if (i.arg1 == null || i.arg2 == null || !i.arg1.isConst() || !i.arg2.isConst()) return false;
        if ((i.op == IROp.DIV || i.op == IROp.MOD) && i.arg2.intValue() == 0) return false;
        return switch (i.op) { case ADD, SUB, MUL, DIV, MOD, LT, LE, GT, GE, EQ, NE, AND, OR -> true; default -> false; };
    }

    private int evaluate(IROp op, int a, int b) {
        return switch (op) {
            case ADD -> a+b; case SUB -> a-b; case MUL -> a*b; case DIV -> a/b; case MOD -> a%b;
            case LT -> a < b ? 1 : 0; case LE -> a <= b ? 1 : 0; case GT -> a > b ? 1 : 0; case GE -> a >= b ? 1 : 0;
            case EQ -> a == b ? 1 : 0; case NE -> a != b ? 1 : 0;
            case AND -> (a != 0 && b != 0) ? 1 : 0; case OR -> (a != 0 || b != 0) ? 1 : 0;
            default -> throw new IllegalArgumentException("No plegable: " + op);
        };
    }
}
