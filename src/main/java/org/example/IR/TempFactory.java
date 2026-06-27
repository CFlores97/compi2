package org.example.IR;

public class TempFactory {
    private int count = 0;

    public Operand newTemp() {
        count++;
        return Operand.temp("t" + count);
    }

    public void reset() {
        this.count = 0;
    }
}
