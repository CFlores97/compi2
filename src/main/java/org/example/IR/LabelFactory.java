package org.example.IR;

public final class LabelFactory {
    private int count;

    public Operand newLabel(String prefix) {
        return Operand.label(prefix + (++count));
    }

    public void reset() {
        count = 0;
    }
}
