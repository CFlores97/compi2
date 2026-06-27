package org.example.mips;

import org.example.IR.IRFunction;
import org.example.IR.IRInstruction;
import org.example.IR.IROp;
import org.example.IR.IRVariable;
import org.example.IR.Operand;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns stable stack offsets for one IR function before MIPS emission.
 * Scalars and temporaries occupy one word; local arrays occupy their full byte size.
 */
public final class StackFrame {
    private final Map<String, Integer> offsets = new LinkedHashMap<>();
    private int nextOffset;
    private final int frameSize;
    private final int savedFpOffset;
    private final int savedRaOffset;

    public StackFrame(IRFunction function) {
        for (IRVariable param : function.params) reserve(param.name, 4);
        for (IRVariable local : function.locals) reserve(local.name, local.storageBytes());
        for (IRInstruction instruction : function.code) register(instruction);
        int raw = nextOffset + 8; // saved $fp + saved $ra
        frameSize = align8(raw);
        savedFpOffset = frameSize - 8;
        savedRaOffset = frameSize - 4;
    }

    private void register(IRInstruction i) {
        switch (i.op) {
            case LABEL, GOTO -> { }
            case IFZ, RETURN, PARAM, LOAD_IND -> reserveOperand(i.arg1);
            case STORE_IND -> { reserveOperand(i.result); reserveOperand(i.arg1); }
            case CALL -> reserveOperand(i.result);
            case ADDR_OF -> reserveOperand(i.result); // arg1 is a storage reference, already allocated if local
            default -> { reserveOperand(i.result); reserveOperand(i.arg1); reserveOperand(i.arg2); }
        }
    }

    private void reserveOperand(Operand operand) {
        if (operand == null) return;
        if (operand.kind == Operand.Kind.TEMP || operand.kind == Operand.Kind.VAR) reserve(operand.value, 4);
    }

    private void reserve(String name, int bytes) {
        if (offsets.containsKey(name)) return;
        int aligned = align4(Math.max(4, bytes));
        offsets.put(name, nextOffset);
        nextOffset += aligned;
    }

    public int offsetOf(String name) {
        Integer offset = offsets.get(name);
        if (offset == null) throw new IllegalStateException("No existe offset para '" + name + "'");
        return offset;
    }
    public int frameSize() { return frameSize; }
    public int savedFpOffset() { return savedFpOffset; }
    public int savedRaOffset() { return savedRaOffset; }
    private static int align4(int bytes) { return ((bytes + 3) / 4) * 4; }
    private static int align8(int bytes) { return ((bytes + 7) / 8) * 8; }
}
