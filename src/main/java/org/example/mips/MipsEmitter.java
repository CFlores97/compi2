package org.example.mips;

import org.example.IR.IRFunction;
import org.example.IR.IRInstruction;
import org.example.IR.IRProgram;
import org.example.IR.Operand;

import java.util.ArrayList;
import java.util.List;

public class MipsEmitter {

    private final List<Operand> pendingArgs = new ArrayList<>();

    private StackFrame currentFrame;
    private IRFunction currentFunction;
    private String currentEndLabel;

    public String emit(IRProgram program) {
        if (program == null) {
            throw new IllegalArgumentException(
                    "IRProgram no puede ser null."
            );
        }

        StringBuilder sb = new StringBuilder();

        sb.append(".data\n\n");
        sb.append(".text\n");
        sb.append(".globl main\n\n");

        boolean hasMain = false;

        for (IRFunction function : program.functions) {
            if ("main".equals(function.name)) {
                hasMain = true;
            }

            emitFunction(function, sb);
            sb.append("\n");
        }

        if (!hasMain) {
            throw new IllegalStateException(
                    "No se encontro la funcion main en el IR."
            );
        }

        return sb.toString();
    }

    private void emitFunction(IRFunction function, StringBuilder sb) {
        currentFunction = function;
        currentFrame = new StackFrame(function);
        currentEndLabel = "__end_" + function.name;

        pendingArgs.clear();

        sb.append(function.name).append(":\n");

        emitPrologue(sb);
        emitParameterMoves(sb);

        for (IRInstruction instruction : function.code) {
            emitInstruction(instruction, sb);
        }

        // Return por defecto si la función termina sin RETURN.
        sb.append(" li $v0, 0\n");
        sb.append(" j ").append(currentEndLabel).append("\n");
        sb.append(" nop\n\n");

        sb.append(currentEndLabel).append(":\n");

        emitEpilogue(sb);

        if ("main".equals(function.name)) {
            sb.append(" li $v0, 10\n");
            sb.append(" syscall\n");
        } else {
            sb.append(" jr $ra\n");
            sb.append(" nop\n");
        }

        if (!pendingArgs.isEmpty()) {
            throw new IllegalStateException(
                    "Quedaron PARAM sin CALL en la funcion "
                            + function.name
            );
        }
    }

    private void emitPrologue(StringBuilder sb) {
        sb.append(" # Prologo de ")
                .append(currentFunction.name)
                .append("\n");

        sb.append(" addiu $sp, $sp, -")
                .append(currentFrame.frameSize())
                .append("\n");

        sb.append(" sw $ra, ")
                .append(currentFrame.savedRaOffset())
                .append("($sp)\n");

        sb.append(" sw $fp, ")
                .append(currentFrame.savedFpOffset())
                .append("($sp)\n");

        sb.append(" move $fp, $sp\n\n");
    }

    private void emitEpilogue(StringBuilder sb) {
        sb.append(" # Epilogo de ")
                .append(currentFunction.name)
                .append("\n");

        sb.append(" move $sp, $fp\n");

        sb.append(" lw $ra, ")
                .append(currentFrame.savedRaOffset())
                .append("($sp)\n");

        sb.append(" lw $fp, ")
                .append(currentFrame.savedFpOffset())
                .append("($sp)\n");

        sb.append(" addiu $sp, $sp, ")
                .append(currentFrame.frameSize())
                .append("\n");
    }

    private void emitParameterMoves(StringBuilder sb) {
        for (int index = 0; index < currentFunction.params.size(); index++) {
            String paramName = currentFunction.params.get(index);

            int destinationOffset = currentFrame.offsetOf(paramName);

            if (index < 4) {
                sb.append(" sw $a")
                        .append(index)
                        .append(", ")
                        .append(destinationOffset)
                        .append("($fp)\n");
            } else {
                int sourceOffset = currentFrame.frameSize()
                        + ((index - 4) * 4);

                sb.append(" lw $t0, ")
                        .append(sourceOffset)
                        .append("($fp)\n");

                sb.append(" sw $t0, ")
                        .append(destinationOffset)
                        .append("($fp)\n");
            }
        }

        if (!currentFunction.params.isEmpty()) {
            sb.append("\n");
        }
    }

    private void emitInstruction(IRInstruction i, StringBuilder sb) {
        switch (i.op) {

            case ASSIGN -> emitAssign(i, sb);

            case ADD -> emitBinary("add", i, sb);
            case SUB -> emitBinary("sub", i, sb);
            case MUL -> emitBinary("mul", i, sb);

            case DIV -> emitDivision(i, false, sb);
            case MOD -> emitDivision(i, true, sb);

            case LT -> emitLessThan(i, sb);
            case LE -> emitLessOrEqual(i, sb);
            case GT -> emitGreaterThan(i, sb);
            case GE -> emitGreaterOrEqual(i, sb);
            case EQ -> emitEqual(i, sb);
            case NE -> emitNotEqual(i, sb);

            case AND -> emitLogicalAnd(i, sb);
            case OR -> emitLogicalOr(i, sb);

            case NEG -> emitNeg(i, sb);
            case NOT -> emitNot(i, sb);

            case LABEL -> sb.append(i.result).append(":\n");

            case GOTO -> {
                sb.append(" j ").append(i.result).append("\n");
                sb.append(" nop\n");
            }

            case IFZ -> {
                load(i.arg1, "$t0", sb);

                sb.append(" beq $t0, $zero, ")
                        .append(i.result)
                        .append("\n");

                sb.append(" nop\n");
            }

            case PARAM -> {
                Operand value = firstNonNull(i.arg1, i.result);

                if (value == null) {
                    throw new IllegalStateException(
                            "PARAM sin valor."
                    );
                }

                pendingArgs.add(value);
            }

            case CALL -> emitCall(i, sb);

            case RETURN -> emitReturn(i, sb);
        }
    }

    private void emitAssign(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        store(i.result, "$t0", sb);
    }

    private void emitBinary(
            String instruction,
            IRInstruction i,
            StringBuilder sb
    ) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" ")
                .append(instruction)
                .append(" $t2, $t0, $t1\n");

        store(i.result, "$t2", sb);
    }

    private void emitDivision(
            IRInstruction i,
            boolean remainder,
            StringBuilder sb
    ) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" div $t0, $t1\n");

        sb.append(remainder
                ? " mfhi $t2\n"
                : " mflo $t2\n");

        store(i.result, "$t2", sb);
    }

    private void emitLessThan(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" slt $t2, $t0, $t1\n");

        store(i.result, "$t2", sb);
    }

    private void emitLessOrEqual(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" slt $t2, $t1, $t0\n");
        sb.append(" xori $t2, $t2, 1\n");

        store(i.result, "$t2", sb);
    }

    private void emitGreaterThan(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" slt $t2, $t1, $t0\n");

        store(i.result, "$t2", sb);
    }

    private void emitGreaterOrEqual(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" slt $t2, $t0, $t1\n");
        sb.append(" xori $t2, $t2, 1\n");

        store(i.result, "$t2", sb);
    }

    private void emitEqual(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" xor $t2, $t0, $t1\n");
        sb.append(" sltiu $t2, $t2, 1\n");

        store(i.result, "$t2", sb);
    }

    private void emitNotEqual(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" xor $t2, $t0, $t1\n");
        sb.append(" sltu $t2, $zero, $t2\n");

        store(i.result, "$t2", sb);
    }

    private void emitLogicalAnd(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" sltu $t0, $zero, $t0\n");
        sb.append(" sltu $t1, $zero, $t1\n");
        sb.append(" and $t2, $t0, $t1\n");

        store(i.result, "$t2", sb);
    }

    private void emitLogicalOr(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);
        load(i.arg2, "$t1", sb);

        sb.append(" sltu $t0, $zero, $t0\n");
        sb.append(" sltu $t1, $zero, $t1\n");
        sb.append(" or $t2, $t0, $t1\n");

        store(i.result, "$t2", sb);
    }

    private void emitNeg(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);

        sb.append(" subu $t1, $zero, $t0\n");

        store(i.result, "$t1", sb);
    }

    private void emitNot(IRInstruction i, StringBuilder sb) {
        load(i.arg1, "$t0", sb);

        sb.append(" sltiu $t1, $t0, 1\n");

        store(i.result, "$t1", sb);
    }

    private void emitCall(IRInstruction i, StringBuilder sb) {
        if (i.arg1 == null || i.arg1.kind != Operand.Kind.FUNC) {
            throw new IllegalStateException(
                    "CALL sin operando de funcion."
            );
        }

        int expectedArgs = i.arg2 == null
                ? pendingArgs.size()
                : i.arg2.intValue();

        if (expectedArgs != pendingArgs.size()) {
            throw new IllegalStateException(
                    "CALL a " + i.arg1
                            + " esperaba " + expectedArgs
                            + " PARAM, pero encontro "
                            + pendingArgs.size()
            );
        }

        int registerArgs = Math.min(4, pendingArgs.size());

        for (int index = 0; index < registerArgs; index++) {
            load(pendingArgs.get(index), "$a" + index, sb);
        }

        int extraArgs = Math.max(0, pendingArgs.size() - 4);
        int extraBytes = alignToEight(extraArgs * 4);

        if (extraBytes > 0) {
            sb.append(" addiu $sp, $sp, -")
                    .append(extraBytes)
                    .append("\n");

            for (int index = 4; index < pendingArgs.size(); index++) {
                load(pendingArgs.get(index), "$t0", sb);

                sb.append(" sw $t0, ")
                        .append((index - 4) * 4)
                        .append("($sp)\n");
            }
        }

        sb.append(" jal ")
                .append(i.arg1.value)
                .append("\n");

        sb.append(" nop\n");

        if (extraBytes > 0) {
            sb.append(" addiu $sp, $sp, ")
                    .append(extraBytes)
                    .append("\n");
        }

        if (i.result != null) {
            store(i.result, "$v0", sb);
        }

        pendingArgs.clear();
    }

    private void emitReturn(IRInstruction i, StringBuilder sb) {
        Operand value = firstNonNull(i.arg1, i.result);

        if (value == null) {
            sb.append(" li $v0, 0\n");
        } else {
            load(value, "$v0", sb);
        }

        sb.append(" j ")
                .append(currentEndLabel)
                .append("\n");

        sb.append(" nop\n");
    }

    private void load(
            Operand operand,
            String register,
            StringBuilder sb
    ) {
        if (operand == null) {
            sb.append(" li ")
                    .append(register)
                    .append(", 0\n");
            return;
        }

        if (operand.kind == Operand.Kind.CONST) {
            sb.append(" li ")
                    .append(register)
                    .append(", ")
                    .append(operand.value)
                    .append("\n");
            return;
        }

        if (operand.kind != Operand.Kind.TEMP &&
                operand.kind != Operand.Kind.VAR) {
            throw new IllegalStateException(
                    "No se puede cargar como valor: " + operand
            );
        }

        sb.append(" lw ")
                .append(register)
                .append(", ")
                .append(currentFrame.offsetOf(operand.value))
                .append("($fp)\n");
    }

    private void store(
            Operand operand,
            String register,
            StringBuilder sb
    ) {
        if (operand == null) {
            return;
        }

        if (operand.kind != Operand.Kind.TEMP &&
                operand.kind != Operand.Kind.VAR) {
            throw new IllegalStateException(
                    "No se puede guardar dentro de: " + operand
            );
        }

        sb.append(" sw ")
                .append(register)
                .append(", ")
                .append(currentFrame.offsetOf(operand.value))
                .append("($fp)\n");
    }

    private static Operand firstNonNull(
            Operand first,
            Operand second
    ) {
        return first != null ? first : second;
    }

    private static int alignToEight(int bytes) {
        return (bytes + 7) / 8 * 8;
    }
}