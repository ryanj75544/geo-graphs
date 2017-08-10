/*
 * GraphEquation.java
 * GraphEquation is an object with both an equation component and an answer component
 * used to keep track of past equation - answer combinations
 */
package com.ryan.graphcalc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// class to store original equation (as entered by user) and the evaluated result
class GraphEquation {
  private String originalEquation;
  private Double ans;


  public GraphEquation(String originalEquation, Double ans) {
    this.originalEquation = originalEquation;
    this.ans = ans;
  }

  public String getOriginalEquation() {
    return originalEquation;
  }

  public void setOriginalEquation(String originalEquation) {
    this.originalEquation = originalEquation;
  }

  public Double getAns() {
    return ans;
  }

  public void setAns(double ans) {
    this.ans = ans;
  }

  // get the closing parentheses in the equation
  public static int getClosingParentheses(String equationLine, int startSearchPos) {
    // find right parentheses
    int numParens = 1;
    int j = startSearchPos + 1;
    while(j < equationLine.length() && numParens >= 1) {
      j++;
      if(equationLine.charAt(j) == '(') {
        numParens++;
      } else if(equationLine.charAt(j) == ')') {
        numParens--;
      }
    }
    if(j < equationLine.length() && numParens == 0) {
      return j;
    }
    return -1;
  }

  // round a number to the nearest n places
  public static double roundDouble(double dVal, int nDecimalPlaces) {
    return Math.round(dVal * Math.pow(10, nDecimalPlaces)) / Math.pow(10, nDecimalPlaces);
  }

  // detect if an equation has variables
  public static boolean hasVariables(String equation) {
    Pattern hasVariablesP0 = Pattern.compile("[^a-z][a-z][^a-z]");
    Pattern hasVariablesP1 = Pattern.compile("^[a-z][^a-z]");
    Pattern hasVariablesP2 = Pattern.compile("[^a-z][a-z]$");
    Matcher hasVariablesM0 = hasVariablesP0.matcher(equation);
    Matcher hasVariablesM1 = hasVariablesP1.matcher(equation);
    Matcher hasVariablesM2 = hasVariablesP2.matcher(equation);
    if(hasVariablesM0.find() || hasVariablesM1.find() || hasVariablesM2.find()) {
      return true;
    } else {
      return false;
    }
  }

  // determine if its a trig function
  public static boolean isTrigFunction(String eqStr) {
    Pattern mFuncPattern = Pattern.compile("(sin|cos|tan)", Pattern.CASE_INSENSITIVE);
    Matcher mFuncMatcher = mFuncPattern.matcher(eqStr);

    // found a function in the equation line
    if(mFuncMatcher.find()) {
      return true;
    }
    return false;
  }
}
