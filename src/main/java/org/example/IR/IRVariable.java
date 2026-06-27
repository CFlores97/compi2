package org.example.IR;

import org.example.semantics.MiniCType;

public final class IRVariable {
    public final String name;
    public final MiniCType type;
    public final boolean parameter;

    public IRVariable(String name, MiniCType type, boolean parameter) {
        this.name = name;
        this.type = type;
        this.parameter = parameter;
    }

    public int storageBytes() {
        // An array parameter is passed as an address.
        return parameter ? 4 : type.storageBytes();
    }
}
