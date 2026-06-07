package org.example.semantics;

import java.util.ArrayList;
import java.util.List;

public class SemanticErrorReporter {
    private final List<String> errors = new ArrayList<>();

    public void addError(int line, int column, String message) {
        errors.add("Error semantico en linea " + line
                + ", columna " + column + ": " + message);
    }

    public boolean hasErrors()  { return !errors.isEmpty(); }

    public void printErrors() {
        for (String error : errors)
            System.err.println(error);
    }

    public List<String> getErrors() { return errors; }
}