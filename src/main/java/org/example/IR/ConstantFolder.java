package org.example.IR;

import java.util.ArrayList;
import java.util.List;

public class ConstantFolder {

    public IRProgram optimize(IRProgram program) {
        for (IRFunction function : program.functions) {
            List<IRInstruction> optimized = new ArrayList<>();

            for (IRInstruction instruction : function.code) {
                if (isFoldable(instruction)) {
                    int left = instruction.arg1.intValue();
                    int right = instruction.arg2.intValue();

                    int result = evaluate(instruction.op, left, right);

                    optimized.add(
                            IRInstruction.assign(
                                    instruction.result,
                                    Operand.constant(String.valueOf(result))
                            )
                    );
                } else {
                    optimized.add(instruction);
                }
            }

            function.code.clear();
            function.code.addAll(optimized);
        }

        return program;
    }

    private boolean isFoldable(IRInstruction instruction) {
        boolean validOperation =
                instruction.op == IROp.ADD ||
                        instruction.op == IROp.SUB ||
                        instruction.op == IROp.MUL ||
                        instruction.op == IROp.DIV ||
                        instruction.op == IROp.MOD;

        if (!validOperation ||
                instruction.arg1 == null ||
                instruction.arg2 == null ||
                !instruction.arg1.isConst() ||
                !instruction.arg2.isConst()) {
            return false;
        }

        int divisor = instruction.arg2.intValue();

        if ((instruction.op == IROp.DIV || instruction.op == IROp.MOD)
                && divisor == 0) {
            return false;
        }

        return true;
    }

    private int evaluate(IROp op, int left, int right) {
        return switch (op) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> left / right;
            case MOD -> left % right;
            default -> throw new IllegalArgumentException(
                    "Operacion no optimizable: " + op
            );
        };
    }
}