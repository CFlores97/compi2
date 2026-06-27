package org.example.IR;

public enum IROp {
    ASSIGN,
    ADD, SUB, MUL, DIV, MOD,
    LT, LE, GT, GE, EQ, NE,
    AND, OR,
    NEG, NOT,
    ADDR_OF,
    LOAD_IND,
    STORE_IND,
    LABEL,
    GOTO,
    IFZ,
    PARAM,
    CALL,
    RETURN
}
