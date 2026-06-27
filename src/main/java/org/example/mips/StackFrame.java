package org.example.mips;

import org.example.IR.IRFunction;
import org.example.IR.IRInstruction;
import org.example.IR.Operand;

import java.util.LinkedHashMap;
import java.util.Map;

public class StackFrame {

    private final Map<String, Integer> offsets = new LinkedHashMap<>();

    private int nextLocalOffset = 0;

    private final int frameSize;
    private final int savedFpOffset;
    private final int savedRaOffset;

    public StackFrame(IRFunction function) {
        prepare(function);

        int rawSize = nextLocalOffset + 8;

        frameSize = alignToEight(rawSize);
        savedFpOffset = frameSize - 8;
        savedRaOffset = frameSize - 4;
    }

    private void prepare(IRFunction function) {
        for (String param : function.params) {
            reserve(param);
        }

        for (String local : function.locals) {
            reserve(local);
        }

        for (IRInstruction instruction : function.code) {
            registerInstruction(instruction);
        }
    }

    private void registerInstruction(IRInstruction instruction) {
        switch (instruction.op) {

            case LABEL, GOTO -> {
                // Etiquetas no usan espacio en stack.
            }

            case IFZ -> reserveOperand(instruction.arg1);

            case PARAM -> reserveOperand(
                    firstNonNull(instruction.arg1, instruction.result)
            );

            case CALL -> reserveOperand(instruction.result);

            case RETURN -> reserveOperand(
                    firstNonNull(instruction.arg1, instruction.result)
            );

            default -> {
                reserveOperand(instruction.result);
                reserveOperand(instruction.arg1);
                reserveOperand(instruction.arg2);
            }
        }
    }

    private void reserveOperand(Operand operand) {
        if (operand == null) {
            return;
        }

        if (operand.kind == Operand.Kind.TEMP ||
                operand.kind == Operand.Kind.VAR) {
            reserve(operand.value);
        }
    }

    private void reserve(String name) {
        if (!offsets.containsKey(name)) {
            offsets.put(name, nextLocalOffset);
            nextLocalOffset += 4;
        }
    }

    public int offsetOf(String name) {
        Integer offset = offsets.get(name);

        if (offset == null) {
            throw new IllegalStateException(
                    "No existe offset de stack para: " + name
            );
        }

        return offset;
    }

    public int frameSize() {
        return frameSize;
    }

    public int savedFpOffset() {
        return savedFpOffset;
    }

    public int savedRaOffset() {
        return savedRaOffset;
    }

    private static Operand firstNonNull(Operand first, Operand second) {
        return first != null ? first : second;
    }

    private static int alignToEight(int bytes) {
        return (bytes + 7) / 8 * 8;
    }
}