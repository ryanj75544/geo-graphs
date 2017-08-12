/*
 * Calculator.java
 * The class that parses the equation so that it can be solved using the correct order of precedence
 * Also keeps track of past equations so that they can be accessed in the future
 */
package com.ryan.graphcalc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Calculator {

    // number if results to keep in history
    public final int NUM_HISTORY_RESULTS = 10;

    // queue to store previous answers
    private Queue<GraphEquation> answers = new LinkedList<>();

    // value on left side of operator
    private String operand0;

    // value on right side of operator
    private String operand1;

    // where start of left operand is
    private int startOperand0;

    // where start of right operand is
    private int startOperand1;

    // keep track of original equation before it was processed
    private String mOriginalEquation;

    // represents symbol in between two operands
    Operator operator = null;

    // index to keep track of which equation the up arrow should pull up
    int mHistoryBrowserIndex = -1;

    // calculator constructor
    public Calculator() {
    }

    // set original equation before any processing is done
    public void setOriginalEquation(String originalEquation) {
        mOriginalEquation = originalEquation;
    }

    // starting point for processing the main equation
    public double processLine(String eqLine) throws Exception {
        // apply initial formatting to the equation they entered
        eqLine = formatEquation(eqLine);

        // make first call to evaluate with precedence order 4 (look for parentheses)
        double ans = Double.parseDouble(parseEquation(eqLine, 4));

        // round  answer to 6 decimal places
        ans = GraphEquation.roundDouble(ans, 6);

        // add answer to end of queue
        addEnteredEquation(mOriginalEquation, ans);

        // reset original equation
        setOriginalEquation("");

        return ans;
    }

    // in case the equation has the form y=f(x), read second paramter for x
    public double processLine(String eqLine, double xVal) throws Exception {
        // replace all x's with second parameter
        eqLine = eqLine.replaceAll("(?i)x", Double.toString(xVal));

        // apply preliminary formatting to equation
        eqLine = formatEquation(eqLine);

        // solve for x
        return processLine(eqLine);
    }

    // apply all formatting to the equation so that it can be put through parser
    public String formatEquation(String eqLine) {
        // replace all spaces between operators / variables
        eqLine = eqLine.replaceAll("\\s", "");

        // replace occurrences of 3x -> change to 3 * x
        eqLine = eqLine.replaceAll("(\\d)(x)", "$1" + "*" + "$2");

        // replace "y = " at start of equation
        eqLine = eqLine.replaceAll("^(?i)y\\s?=\\s?", "");

        return eqLine;
    }

    // process all operators that match the precedence order
    protected String parseEquation(String equation, int order) throws Exception {
        // initialize operands with empty strings
        resetOperands();
        int i = 0;
        // iterate each character
        while(i < equation.length()) {
            // check for numbers, if found add number to end of operand
            if((int) equation.charAt(i) >= (int) '0' && (int) equation.charAt(i) <= (int) '9'
                    || equation.charAt(i) == '.'
                    ) {
                if(operator == null) {
                    operand0 += equation.substring(i, i + 1);
                    if(startOperand0 == -1) { startOperand0 = i; }
                } else {
                    operand1 += equation.substring(i, i + 1);
                    if(startOperand1 == -1) { startOperand1 = i; }
                }
            } else {
                // check for negative numbers if its a hyphen
                if (operator == null && operand0 == "" && equation.charAt(i) == '-') {
                    operand0 = "-";
                    startOperand0 = i;
                } else if (operator != null && operand0 != "" && operand1 == "" && equation.charAt(i) == '-') {
                    operand1 = "-";
                    startOperand1 = i;
                }
                // found an operator in between to numbers
                else if(operator == null) {
                    boolean foundOperator = false;
                    // check for operator symbols
                    // e.g. (+, -) if order == 1
                    for (Operator op : Operator.values()) {
                        if (op.getOpName().equals(equation.substring(i, i + 1)) && op.getOrder() == order) {
                            foundOperator = true;
                            operator = op;
                            // parentheses is handle differently since it has only one operand
                            if(op == Operator.LEFTPAREN) {
                                // find right parentheses
                                int numParens = 1;
                                int j = i + 1;
                                while(j < equation.length() && numParens >= 1) {
                                    j++;
                                    if(equation.charAt(j) == '(') {
                                        numParens++;
                                    } else if(equation.charAt(j) == ')') {
                                        numParens--;
                                    }
                                }
                                if(j < equation.length() && numParens == 0) {
                                    equation = // everything before opening parentheses
                                                equation.substring(0, i) +
                                                // call parseEquation on everything inside parentheses
                                                parseEquation(equation.substring(i + 1, j), 4) +
                                                // add everything after closing parentheses
                                                equation.substring(j + 1);
                                    resetOperands();
                                } else {
                                    throw new Exception("Failed to find closing parentheses after \"" + equation.substring(0, i) + "\"");
                                }
                            }
                            break;
                        }
                    } // end for of operators
                    if(!foundOperator) {
                        // operands reset since symbol doesn't apply to this order level
                        resetOperands();
                    }
                }
                // found an operator to end the second operand, ready to evaluate part of equation
                else {
                    // finish reading second operand
                    operand1 = equation.substring(startOperand1, i);
                    // apply binary operation
                    String partialResult = Double.toString(evaluateOp(operand0, operand1, operator));
                    // rearrange string with result in the middle
                    equation = equation.substring(0, startOperand0) +
                                        partialResult +
                                        equation.substring(i);
                    // reset operands to look for new equation piece
                    resetOperands();

                    // start over again at beginning of equation
                    i = -1;
                }
            }
            i++;
        }
        // handle remaining operands
        if(operand0 != "" && i > startOperand1 && operator != null) {
            operand1 = equation.substring(startOperand1, i);
            // solve equation piecee
            String partialResult = Double.toString(evaluateOp(operand0, operand1, operator));
            // rearrange equation with result at the end
            equation = equation.substring(0, startOperand0) +
                    partialResult;
        }
        // if still more order levels to take care of
        if(order > 1) {
            // call parseEquation again for new order level
            return parseEquation(equation, order - 1);
        }
        // done with last order level, return final result
        return equation;
    }

    // reset operands back to starting position
    private void resetOperands() {
        operand0 = "";
        operand1 = "";
        startOperand0 = -1;
        startOperand1 = -1;
        operator = null;
    }

    // given only two operands and an operator, return the result
    private double evaluateOp(String operand0, String operand1, Operator operator) {
        switch(operator) {
            case ADD:
                return Double.parseDouble(operand0) + Double.parseDouble(operand1);
            case SUBTRACT:
                return Double.parseDouble(operand0) - Double.parseDouble(operand1);
            case MULTIPLY:
                return Double.parseDouble(operand0) * Double.parseDouble(operand1);
            case DIVIDE:
                return Double.parseDouble(operand0) / Double.parseDouble(operand1);
            case EXPONENT:
                return Math.pow(Double.parseDouble(operand0), Double.parseDouble(operand1));
        }
        return 0;
    }

    // return an array of the last 10 equations the user entered
    public String[] getEquationsHistory() {
        String[] equationsHistory = new String[NUM_HISTORY_RESULTS];
        int equationsLen = answers.size();
        int equationIndex = 0;
        for(GraphEquation graphEquation : answers) {
            equationsHistory[NUM_HISTORY_RESULTS - equationsLen + equationIndex++] = graphEquation.getOriginalEquation();
        }
        return equationsHistory;
    }
    // return an array of the last 10 answers the user entered
    public Double[] getAnswersHistory() {
        Double[] answerHistory = new Double[NUM_HISTORY_RESULTS];
        int answersLen = answers.size();
        int answerIndex = 0;
        for(GraphEquation graphEquation : answers) {
            answerHistory[NUM_HISTORY_RESULTS - answersLen + answerIndex++] = graphEquation.getAns();
        }
        return answerHistory;
    }

    // add an equation / answer to the answer history list
    public void addEnteredEquation(String origEquation, Double ans) {
        GraphEquation ge = new GraphEquation(origEquation, ans);
        // add to end of queue
        answers.add(ge);
        // remove first from front of queue
        if(answers.size() > NUM_HISTORY_RESULTS) { answers.remove(); }

        // equation browser index resets when they entered an equation
        mHistoryBrowserIndex = answers.size();
    }

    // get a past result, how far back depends on parameter
    public String getPastResult(int pastResultsOffset) {
        // iterator for queue collection
        Iterator iterator = answers.iterator();
        String lastEquation;

        // counter to detect how far back its gone
        int pastResultsCounter = 0;

        // iterator the history list from past to current
        while(iterator.hasNext()) {
            lastEquation = ((GraphEquation) iterator.next()).getOriginalEquation();
            // found the past equation referred to by pastResultsOffset
            if(pastResultsCounter == pastResultsOffset) {
                if(lastEquation.indexOf("Ryan Johnson") > -1
                        || lastEquation.indexOf("Press [up] to") > -1) { return null; }
                return lastEquation;
            }
            pastResultsCounter++;
        }
        return null;
    }

    // decrement index to point to more recent one, return the equation it refers to
    public String getPreviousEquation() {
        // decrement to go back
        mHistoryBrowserIndex = Math.max(0, mHistoryBrowserIndex - 1);
        // use the decremented mHistoryBrowserIndex as the parameter for past results, return it
        return getPastResult(mHistoryBrowserIndex);
    }

    // increment index to point to more recent one, return the equation it refers to
    public String getNextEquation() {
        // increment to get forward
        mHistoryBrowserIndex = Math.min(answers.size() - 1, mHistoryBrowserIndex + 1);
        // use the incremented mHistoryBrowserIndex as the parameter for past results, return it
        return getPastResult(mHistoryBrowserIndex);
    }
}