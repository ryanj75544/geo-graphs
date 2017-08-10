/*
 * TrigonometryCalculator.java
 * Calculates trigonometry functions such as sin, cos, tan
 */
package com.ryan.graphcalc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrigonometryCalculator extends Calculator {
    // the equation line to solve
    String mEquationLine;

    // name of trig function
    String mTrigFunction;

    // where first trig function starts
    int mTrigFunctPos = -1;

    // result of solving equation
    String mTrigResult;

    // where parameter to trig function starts
    int mStartParameter;

    // the parameter that is used to pass to trig function
    double mTrigParamVal;

    // solved value of function
    double mAns;

    // set the equation to work with
    public void setEquation(String equation) {
        mEquationLine = equation;
    }

    // given an input equation solve each trig function it has and return the equation with the trig
    // functions replaced with actual results
    public String processTrigFunctions(String equation) {
        // set the equation from the parameter
        setEquation(equation);

        // apply all formatting
        mEquationLine = formatEquation(mEquationLine);

        // if the equation has a trig function to solve for
        while(hasTrigFunction()) {
            // evaluate the first trig function found
            mEquationLine = evaluateFirstFunction();
        }
        return mEquationLine;
    }
    // use x as an input variable to all trig functions
    public String processTrigFunctions(String equation, double xVal) {

        // apply preliminary formatting to equation
        equation = formatEquation(equation);

        // replace all x's with xVal
        equation = equation.replaceAll("(?i)x", Double.toString(xVal));

        // continue processing
        return processTrigFunctions(equation);
    }

    // detect if a trig function exists in the equation line
    protected boolean hasTrigFunction() {
        // if found a position, the function must exist within the line
        return (getFirstTrigPos() > -1);
    }

    // get the position of the first trigonometry function in the equation line
    private int getFirstTrigPos() {
        mTrigFunctPos = -1;
        // use regular expression to match function
        Pattern mFuncPattern = Pattern.compile("(a?)(sin|cos|tan)\\(");
        Matcher mFuncMatcher = mFuncPattern.matcher(mEquationLine);

        // found a function in the equation line
        if(mFuncMatcher.find()) {
            // where pattern started
            mTrigFunctPos  = mFuncMatcher.start();

            // name of function
            mTrigFunction = ((mFuncMatcher.groupCount() > 1)? mFuncMatcher.group(1) + mFuncMatcher.group(2) : mFuncMatcher.group(1));

            // position after left parentheses start
            mStartParameter = mFuncMatcher.end();
        }
        return mTrigFunctPos;
    }

    // evaluate the first trig function that was found earlier
    public String evaluateFirstFunction() {
        mTrigResult = "";

        // get where function paremter ends (last parentheses)
        int lastParentheses = GraphEquation.getClosingParentheses(mEquationLine, mStartParameter);

        // solve for the parameter of the trig function
        String trigParam = mEquationLine.substring(mStartParameter, lastParentheses);
        try {
            double ans = Double.parseDouble(parseEquation(trigParam, 4));

            // use result as x value of trig param
            mTrigParamVal = ans;

            // solve for y value
            mAns = solveTrigFunction();

            // replace function with answer
            mEquationLine = // substring before where function starts
                            mEquationLine.substring(0, mTrigFunctPos) +
                                // use mAns to replace function in original equation
                                Double.toString(mAns) +
                                // the remainder after last parentheses
                                mEquationLine.substring(lastParentheses + 1);
        } catch(Exception exception) {
            System.err.println("Failed to solve for parameter of trig function: " + trigParam);
            return "";
        }
        return mEquationLine;
    }
    private double solveTrigFunction() {
        switch(mTrigFunction) {
            // calculate sine
            case "sin":
                return Math.sin(mTrigParamVal);
            // calculate cosine
            case "cos":
                return Math.cos(mTrigParamVal);
            // calculate tangent
            case "tan":
                return Math.tan(mTrigParamVal);
            // calculate cosecant
            /*
            // todo - not working yet
            case "asin":
                return Math.sin(mTrigParamVal);
            // calculate secant
            case "acos":
                return Math.cos(mTrigParamVal);
            // calculate cotangent
            case "cot":
                return Math.tan(mTrigParamVal);
                */
        }
        return 0;
    }
}