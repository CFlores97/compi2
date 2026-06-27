package org.example.IR;

import java.util.ArrayList;
import java.util.List;

public class IRFunction {

    public final String name;

    public final List<String> params = new ArrayList<>();
    public final List<String> locals = new ArrayList<>();
    public final List<IRInstruction> code = new ArrayList<>();

    public IRFunction(String name) {
        this.name = name;
    }

    public void addParam(String paramName) {
        if (!params.contains(paramName)) {
            params.add(paramName);
        }
    }

    public void addLocal(String localName) {
        if (!locals.contains(localName) && !params.contains(localName)) {
            locals.add(localName);
        }
    }

    public void emit(IRInstruction instruction) {
        if (instruction == null) {
            throw new IllegalArgumentException(
                    "No se puede emitir una instruccion IR nula."
            );
        }

        code.add(instruction);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("function ").append(name).append("\n");

        if (!params.isEmpty()) {
            sb.append("  params: ")
                    .append(String.join(", ", params))
                    .append("\n");
        }

        if (!locals.isEmpty()) {
            sb.append("  locals: ")
                    .append(String.join(", ", locals))
                    .append("\n");
        }

        for (IRInstruction instruction : code) {
            sb.append("  ").append(instruction).append("\n");
        }

        sb.append("end\n");

        return sb.toString();
    }
}