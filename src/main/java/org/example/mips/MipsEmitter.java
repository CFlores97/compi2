package org.example.mips;

import org.example.IR.IRFunction;
import org.example.IR.IRGlobal;
import org.example.IR.IRInstruction;
import org.example.IR.IRProgram;
import org.example.IR.IRString;
import org.example.IR.IRVariable;
import org.example.IR.Operand;

import java.util.ArrayList;
import java.util.List;

/** Emits MIPS32 from the project's TAC. Runtime is appended externally by Main. */
public final class MipsEmitter {
    private final List<Operand> pendingArgs = new ArrayList<>();
    private StackFrame frame;
    private IRFunction function;
    private String endLabel;

    public String emit(IRProgram program) {
        StringBuilder sb = new StringBuilder();
        emitData(program, sb);
        sb.append("\n.text\n");
        for (IRFunction current : program.functions) {
            sb.append(".globl ").append(current.name).append("\n");
        }
        sb.append("\n");
        sb.append("  j main\n  nop\n\n");
        boolean mainFound = false;
        for (IRFunction current : program.functions) {
            if ("main".equals(current.name)) mainFound = true;
            emitFunction(current, sb);
            sb.append("\n");
        }
        if (!mainFound) throw new IllegalStateException("El programa IR no contiene main");
        return sb.toString();
    }

    private void emitData(IRProgram program, StringBuilder sb) {
        sb.append(".data\n");
        for (IRString string : program.strings) {
            sb.append(".align 2\n").append(string.label).append(": .asciiz ").append(string.literalText).append("\n");
        }
        for (IRGlobal global : program.globals) {
            sb.append(".align 2\n").append(global.label).append(": ");
            if (global.isArray()) {
                sb.append(".space ").append(global.storageBytes());
            } else if (global.initializer == null) {
                sb.append(".word 0");
            } else if (global.initializer.kind == Operand.Kind.CONST || global.initializer.kind == Operand.Kind.STRING) {
                sb.append(".word ").append(global.initializer.value);
            } else {
                throw new IllegalStateException("Inicializador global no soportado: " + global.initializer);
            }
            sb.append("\n");
        }
    }

    private void emitFunction(IRFunction current, StringBuilder sb) {
        function = current;
        frame = new StackFrame(current);
        endLabel = "__end_" + current.name;
        pendingArgs.clear();
        sb.append(current.name).append(":\n");
        emitPrologue(sb);
        moveIncomingParameters(sb);
        for (IRInstruction instruction : current.code) emitInstruction(instruction, sb);
        // Fall-through return defaults to zero.
        sb.append("  li $v0, 0\n  j ").append(endLabel).append("\n  nop\n\n");
        sb.append(endLabel).append(":\n");
        emitEpilogue(sb);
        if ("main".equals(current.name)) sb.append("  li $v0, 10\n  syscall\n");
        else sb.append("  jr $ra\n  nop\n");
        if (!pendingArgs.isEmpty()) throw new IllegalStateException("PARAM sin CALL en " + current.name);
    }

    private void emitPrologue(StringBuilder sb) {
        sb.append("  # Prologo de ").append(function.name).append("\n");
        sb.append("  addiu $sp, $sp, -").append(frame.frameSize()).append("\n");
        sb.append("  sw $ra, ").append(frame.savedRaOffset()).append("($sp)\n");
        sb.append("  sw $fp, ").append(frame.savedFpOffset()).append("($sp)\n");
        sb.append("  move $fp, $sp\n");
    }

    private void emitEpilogue(StringBuilder sb) {
        sb.append("  # Epilogo de ").append(function.name).append("\n");
        sb.append("  move $sp, $fp\n");
        sb.append("  lw $ra, ").append(frame.savedRaOffset()).append("($sp)\n");
        sb.append("  lw $fp, ").append(frame.savedFpOffset()).append("($sp)\n");
        sb.append("  addiu $sp, $sp, ").append(frame.frameSize()).append("\n");
    }

    private void moveIncomingParameters(StringBuilder sb) {
        for (int index = 0; index < function.params.size(); index++) {
            IRVariable param = function.params.get(index);
            int destination = frame.offsetOf(param.name);
            if (index < 4) sb.append("  sw $a").append(index).append(", ").append(destination).append("($fp)\n");
            else {
                int incoming = frame.frameSize() + ((index - 4) * 4);
                sb.append("  lw $t0, ").append(incoming).append("($fp)\n");
                sb.append("  sw $t0, ").append(destination).append("($fp)\n");
            }
        }
    }

    private void emitInstruction(IRInstruction i, StringBuilder sb) {
        switch (i.op) {
            case ASSIGN -> { load(i.arg1, "$t0", sb); store(i.result, "$t0", sb); }
            case ADD -> binary("add", i, sb); case SUB -> binary("sub", i, sb); case MUL -> binary("mul", i, sb);
            case DIV -> division(i, false, sb); case MOD -> division(i, true, sb);
            case LT -> compare("slt", i, sb); case GT -> compareReverse("slt", i, sb);
            case LE -> lessEqual(i, sb); case GE -> greaterEqual(i, sb); case EQ -> equal(i, sb); case NE -> notEqual(i, sb);
            case AND -> logical("and", i, sb); case OR -> logical("or", i, sb);
            case NEG -> { load(i.arg1,"$t0",sb); sb.append("  subu $t1, $zero, $t0\n"); store(i.result,"$t1",sb); }
            case NOT -> { load(i.arg1,"$t0",sb); sb.append("  sltiu $t1, $t0, 1\n"); store(i.result,"$t1",sb); }
            case ADDR_OF -> addressOf(i, sb);
            case LOAD_IND -> { load(i.arg1,"$t0",sb); sb.append("  lw $t1, 0($t0)\n"); store(i.result,"$t1",sb); }
            case STORE_IND -> { load(i.result,"$t0",sb); load(i.arg1,"$t1",sb); sb.append("  sw $t1, 0($t0)\n"); }
            case LABEL -> sb.append(i.result.value).append(":\n");
            case GOTO -> sb.append("  j ").append(i.result.value).append("\n  nop\n");
            case IFZ -> { load(i.arg1,"$t0",sb); sb.append("  beq $t0, $zero, ").append(i.result.value).append("\n  nop\n"); }
            case PARAM -> pendingArgs.add(i.arg1);
            case CALL -> call(i, sb);
            case RETURN -> { if(i.arg1 == null) sb.append("  li $v0, 0\n"); else load(i.arg1,"$v0",sb); sb.append("  j ").append(endLabel).append("\n  nop\n"); }
        }
    }

    private void binary(String op, IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  ").append(op).append(" $t2, $t0, $t1\n"); store(i.result,"$t2",sb); }
    private void division(IRInstruction i, boolean mod, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  div $t0, $t1\n"); sb.append(mod?"  mfhi $t2\n":"  mflo $t2\n"); store(i.result,"$t2",sb); }
    private void compare(String op, IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  ").append(op).append(" $t2, $t0, $t1\n"); store(i.result,"$t2",sb); }
    private void compareReverse(String op, IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  ").append(op).append(" $t2, $t1, $t0\n"); store(i.result,"$t2",sb); }
    private void lessEqual(IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  slt $t2, $t1, $t0\n  xori $t2, $t2, 1\n"); store(i.result,"$t2",sb); }
    private void greaterEqual(IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  slt $t2, $t0, $t1\n  xori $t2, $t2, 1\n"); store(i.result,"$t2",sb); }
    private void equal(IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  xor $t2, $t0, $t1\n  sltiu $t2, $t2, 1\n"); store(i.result,"$t2",sb); }
    private void notEqual(IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  xor $t2, $t0, $t1\n  sltu $t2, $zero, $t2\n"); store(i.result,"$t2",sb); }
    private void logical(String op, IRInstruction i, StringBuilder sb) { load(i.arg1,"$t0",sb); load(i.arg2,"$t1",sb); sb.append("  sltu $t0, $zero, $t0\n  sltu $t1, $zero, $t1\n  ").append(op).append(" $t2, $t0, $t1\n"); store(i.result,"$t2",sb); }

    private void addressOf(IRInstruction i, StringBuilder sb) {
        if (i.arg1.kind == Operand.Kind.VAR) sb.append("  addiu $t0, $fp, ").append(frame.offsetOf(i.arg1.value)).append("\n");
        else if (i.arg1.kind == Operand.Kind.GLOBAL) sb.append("  la $t0, ").append(i.arg1.value).append("\n");
        else throw new IllegalStateException("ADDR_OF requiere VAR o GLOBAL, no " + i.arg1);
        store(i.result, "$t0", sb);
    }

    private void call(IRInstruction i, StringBuilder sb) {
        if (i.arg1 == null || i.arg1.kind != Operand.Kind.FUNC) throw new IllegalStateException("CALL sin funcion");
        int expected = i.arg2 == null ? pendingArgs.size() : i.arg2.intValue();
        if (expected != pendingArgs.size()) throw new IllegalStateException("CALL a " + i.arg1 + " no coincide con PARAM");
        int registerArgs = Math.min(4, pendingArgs.size());
        for (int index=0; index<registerArgs; index++) load(pendingArgs.get(index), "$a" + index, sb);
        int extras = pendingArgs.size() - registerArgs;
        int bytes = align8(extras * 4);
        if (bytes > 0) {
            sb.append("  addiu $sp, $sp, -").append(bytes).append("\n");
            for (int index=4; index<pendingArgs.size(); index++) { load(pendingArgs.get(index),"$t0",sb); sb.append("  sw $t0, ").append((index-4)*4).append("($sp)\n"); }
        }
        sb.append("  jal ").append(i.arg1.value).append("\n  nop\n");
        if (bytes > 0) sb.append("  addiu $sp, $sp, ").append(bytes).append("\n");
        if (i.result != null) store(i.result,"$v0",sb);
        pendingArgs.clear();
    }

    private void load(Operand operand, String register, StringBuilder sb) {
        if (operand == null) { sb.append("  li ").append(register).append(", 0\n"); return; }
        switch (operand.kind) {
            case CONST -> sb.append("  li ").append(register).append(", ").append(operand.value).append("\n");
            case STRING -> sb.append("  la ").append(register).append(", ").append(operand.value).append("\n");
            case GLOBAL -> {
                sb.append("  la $t8, ").append(operand.value).append("\n");
                sb.append("  lw ").append(register).append(", 0($t8)\n");
            }
            case TEMP, VAR -> sb.append("  lw ").append(register).append(", ").append(frame.offsetOf(operand.value)).append("($fp)\n");
            default -> throw new IllegalStateException("No se puede cargar " + operand);
        }
    }

    private void store(Operand operand, String register, StringBuilder sb) {
        if (operand == null) return;
        switch (operand.kind) {
            case GLOBAL -> {
                sb.append("  la $t8, ").append(operand.value).append("\n");
                sb.append("  sw ").append(register).append(", 0($t8)\n");
            }
            case TEMP, VAR -> sb.append("  sw ").append(register).append(", ").append(frame.offsetOf(operand.value)).append("($fp)\n");
            default -> throw new IllegalStateException("No se puede guardar en " + operand);
        }
    }

    private static int align8(int bytes) { return ((bytes + 7) / 8) * 8; }
}
