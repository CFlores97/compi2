package org.example.IR;

import org.example.semantics.MiniCType;

import java.util.ArrayList;
import java.util.List;

public final class IRFunction {
    public final String name;
    public final List<IRVariable> params = new ArrayList<>();
    public final List<IRVariable> locals = new ArrayList<>();
    public final List<IRInstruction> code = new ArrayList<>();

    public IRFunction(String name) { this.name = name; }

    public void addParam(String name, MiniCType type) { params.add(new IRVariable(name, type, true)); }
    public void addLocal(String name, MiniCType type) { locals.add(new IRVariable(name, type, false)); }
    public void emit(IRInstruction instruction) { if (instruction == null) throw new IllegalArgumentException("Instruccion IR nula"); code.add(instruction); }
    public String getName() { return name; }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder("function ").append(name).append("\n");
        if (!params.isEmpty()) {
            sb.append("  params: ");
            for (int i=0;i<params.size();i++) { if(i>0) sb.append(", "); sb.append(params.get(i).name).append(":").append(params.get(i).type); }
            sb.append("\n");
        }
        if (!locals.isEmpty()) {
            sb.append("  locals: ");
            for (int i=0;i<locals.size();i++) { if(i>0) sb.append(", "); sb.append(locals.get(i).name).append(":").append(locals.get(i).type); }
            sb.append("\n");
        }
        for (IRInstruction instruction : code) sb.append("  ").append(instruction).append("\n");
        return sb.append("end\n").toString();
    }
}
