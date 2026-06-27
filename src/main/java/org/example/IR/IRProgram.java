package org.example.IR;

import java.util.ArrayList;
import java.util.List;

public class IRProgram {

    public final List<IRFunction> functions = new ArrayList<>();

    public void addFunction(IRFunction function) {
        if (function == null) {
            throw new IllegalArgumentException(
                    "No se puede agregar una funcion IR nula."
            );
        }

        functions.add(function);
    }

    public List<IRFunction> getFunctions() {
        return functions;
    }

    public IRFunction getFunction(String name) {
        for (IRFunction function : functions) {
            if (function.getName().equals(name)) {
                return function;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (IRFunction function : functions) {
            sb.append(function).append("\n");
        }

        return sb.toString();
    }
}