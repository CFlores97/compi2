package org.example.semantics;

public class MiniCType {
    private final String name;
    private final boolean pointer;
    private final int dimensions;

    public MiniCType(String name, boolean pointer, int dimensions) {
        this.name = name;
        this.pointer = pointer;
        this.dimensions = dimensions;
    }

    public String getName()       { return name; }
    public boolean isPointer()    { return pointer; }
    public int getDimensions()    { return dimensions; }

    @Override
    public String toString() {
        String result = name;
        if (pointer) result += "*";
        for (int i = 0; i < dimensions; i++) result += "[]";
        return result;
    }
}



