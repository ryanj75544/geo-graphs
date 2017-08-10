/*
 * GraphCalcControl.java
 * The controller for the Graph Calc application. Checks for key, click events. Assigns equation to the
 * Calculator instance when its ready to evaluate
 *
 */
package com.ryan.graphcalc;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.Optional;
import java.util.Stack;

public class GraphCalcControl {
    // the text field for the equation
    @FXML
    private TextField equationTxt;
    // the equation waiting to be evaluated
    private String currentEquation = "";

    @FXML
    public StackPane answerHistory;

    // Calculator which handles recursive evaluation of the input string
    private Calculator calc = new Calculator();

    // calculator which handles all trigonometry functions
    private TrigonometryCalculator trigCalculator = new TrigonometryCalculator();

    // Panel for displaying past equations that were entered
    private ResultsHistoryDisplay resultsHistoryDisplay;

    @FXML
    AnchorPane anchorPane;

    Stack mainStack;

    // the GraphCalcController constructor
    public GraphCalcControl() {
        resultsHistoryDisplay = new ResultsHistoryDisplay(calc);
    }

    // add intro message to results history display
    public void openResultsHistory() {
        calc.addEnteredEquation("         Welcome to GeoGraphs 1.0\n                 by Ryan Johnson", null);
        calc.addEnteredEquation("  Press [up] to access past equations", null);
        resultsHistoryDisplay.updateHistoryDisplay(answerHistory);
        returnInputFocus();
    }

    // handle clicking button to plot graph
    @FXML
    public void openGraph(ActionEvent actionEvent) {
        // create new GraphDisplayer instance
        GraphDisplayer graphDisplayer = new GraphDisplayer();
        if(!GraphEquation.hasVariables(currentEquation) && actionEvent != null) {
            // create dialog to prompt user for function
            TextInputDialog askFunctionDialog = new TextInputDialog();
            askFunctionDialog.setTitle("Function for Graph");
            askFunctionDialog.setHeaderText("Enter using the form y = f(x)");
            askFunctionDialog.setContentText("y = ");

            Optional<String> result = askFunctionDialog.showAndWait();
            // open graph window using lambda expression
            result.ifPresent(inputEq -> graphDisplayer.addNewGraphAndOpen(inputEq));
        }
        // don't need to prompt user for equation since using the one from the text field
        else {
            graphDisplayer.addNewGraphAndOpen(currentEquation);
            // add the equation to the Calculator instance so it will display in previous results
            calc.addEnteredEquation(currentEquation, 0.0);
            // clear text field to get ready for next equation
            clearInputEquation(null);
            // update results to show the equation that was just entered
            resultsHistoryDisplay.updateHistoryDisplay(answerHistory);
            clearInputEquation(null);
            // return focus for next equation
            returnInputFocus();
        }
    }

    // close the window when Close is pressed
    @FXML
    private void handleClosePressed(ActionEvent actionEvent) {
        ((Stage) anchorPane.getScene().getWindow()).close();
    }

    @FXML
    // handler for button press in main application panel
    private void handleButtonPressed(ActionEvent actionEvent) {
        // get the text value of the Action Event's source
        String addBtnText = ((Button) actionEvent.getSource()).getText();
        if(!"ENTER".equals(addBtnText)) {
            // if its the minus button, just make it the minus sign
            if("(-)".equals(addBtnText)) {
                addBtnText = "-";
            }
            // to lowercase for trig functions, add opening parentheses
            if(GraphEquation.isTrigFunction(addBtnText)) {
                addBtnText = addBtnText.toLowerCase() + "(";
            }
            if("Y".equals(addBtnText)) { addBtnText = "y"; }
            if("X".equals(addBtnText)) { addBtnText = "x"; }
            // add character to end of input buffer
            currentEquation += addBtnText;

            // update the text field to match the input buffer
            equationTxt.setText(currentEquation);

            // return focus text field to accept key presses again
            returnInputFocus();
        }
        // equals sign entered
        else {
            // solve it since equal sign pressed
            solveTextFieldEquation();
        }
    }

    // solve the equation for whatever is in text field
    private void solveTextFieldEquation() {
        // handle differently if equation has variables
        if(GraphEquation.hasVariables(currentEquation)) {
            // open up graph with equation
            openGraph(null);
            return;
        }
        // record the original equation as entered by the user
        calc.setOriginalEquation(currentEquation);
        try {
            // put the equation through the trig parser first, evaluate it
            String processedEquation = trigCalculator.processTrigFunctions(currentEquation);

            // put the equation through the parser, evaluate it
            calc.processLine(processedEquation);
        } catch(Exception exception) {
            exception.printStackTrace();
            System.err.println(exception.getMessage());
        }
        // update display to show results
        resultsHistoryDisplay.updateHistoryDisplay(answerHistory);
        clearInputEquation(null);
    }

    // handler for when key pressed in text field
    public void handleKeyPressed(KeyEvent keyEvent) {
        // enter pressed
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            // same as clicking equals sign, solve equation
            solveTextFieldEquation();
        } else if(keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.DOWN)) {
            // try to get last equation or next equation, place it in text field
            String lastEquation = (keyEvent.getCode().equals(KeyCode.UP))?
                                        calc.getPreviousEquation()
                                        : calc.getNextEquation();
            if(lastEquation != null) {
                // update the text field
                equationTxt.setText(lastEquation);
                // update the input buffer
                currentEquation = lastEquation;
                // return cursor to last position
                equationTxt.positionCaret(currentEquation.length());
            }
        } {
            // To Do:
            // sound beep or play .wav since no equations available
        }
        // need to update the current read buffer so it will match the text field
        currentEquation = equationTxt.getText();
    }

    // clear the input buffer
    public void clearInputEquation(ActionEvent actionEvent) {
        equationTxt.setText("");
        currentEquation = "";
        returnInputFocus();
    }

    // return focus to the input text field
    private void returnInputFocus() {
        equationTxt.requestFocus();
        equationTxt.positionCaret(currentEquation.length());
    }
}