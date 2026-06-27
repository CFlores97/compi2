package org.example.IR;

public class LabelFactory {
    private int count = 0;

    public Operand newLabel(String prefix) {
        count++;
        return Operand.label(prefix + count);
    }

    public void reset(){
        this.count = 0;
    }
}
