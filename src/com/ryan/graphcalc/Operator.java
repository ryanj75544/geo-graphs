/*
 * Operator.java
 * Declaring an Enum type for operators
 */
package com.ryan.graphcalc;

// each operator has an associated symbol and the precedence order level
// (what order it should be solved)
public enum Operator {
    ADD("+", 1),
    SUBTRACT("-", 1),
    MULTIPLY("*", 2),
    DIVIDE("/", 2),
    EXPONENT("^", 3),
    LEFTPAREN("(", 4);

    // character representing operator
    private final String opName;

    // evaluation order of operator
    private final int precedence;

    // enum constructor
    Operator(String opName, int precedence) {
        this.opName = opName;
        this.precedence = precedence;
    }

    // return the symbol
    public String getOpName() {
        return opName;
    }

    // return the precedence
    public int getOrder() {
        return precedence;
    }
}
