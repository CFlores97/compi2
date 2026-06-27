package org.example.IR;

public final class TempFactory {
    private int count;

    public Operand newTemp() {
        return Operand.temp("t" + (++count));
    }

    public void reset() {
        count = 0;
    }
}
