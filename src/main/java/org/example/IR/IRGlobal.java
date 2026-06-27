package org.example.IR;

import org.example.semantics.MiniCType;

public final class IRGlobal {
    public final String label;
    public final MiniCType type;
    public final Operand initializer;

    public IRGlobal(String label, MiniCType type, Operand initializer) {
        this.label = label;
        this.type = type;
        this.initializer = initializer;
    }

    public boolean isArray() { return type.isArray(); }
    public int storageBytes() { return type.storageBytes(); }
}
