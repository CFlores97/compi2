package org.example.IR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class IRProgram {
    public final List<IRFunction> functions = new ArrayList<>();
    public final List<IRGlobal> globals = new ArrayList<>();
    public final List<IRString> strings = new ArrayList<>();
    private final Map<String, Operand> stringPool = new LinkedHashMap<>();

    public void addFunction(IRFunction function) { functions.add(function); }
    public void addGlobal(IRGlobal global) { globals.add(global); }

    public Operand internString(String literalText) {
        Operand existing = stringPool.get(literalText);
        if (existing != null) return existing;
        Operand created = Operand.string("str_" + (strings.size() + 1));
        stringPool.put(literalText, created);
        strings.add(new IRString(created.value, literalText));
        return created;
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IRGlobal global : globals) sb.append("global ").append(global.label).append(" : ").append(global.type).append("\n");
        for (IRString string : strings) sb.append("string ").append(string.label).append(" = ").append(string.literalText).append("\n");
        for (IRFunction function : functions) sb.append(function).append("\n");
        return sb.toString();
    }
}
