/*
 * ResultsHistoryDisplay
 * Display the equations + results for previous equations entered
 */
package com.ryan.graphcalc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class ResultsHistoryDisplay {

    // Calculator instance that has past results
    private Calculator calc;

    // constructor to setup variables
    public ResultsHistoryDisplay(Calculator calc) {
        this.calc = calc;
    }

    // add all the equations - answers to the results history display
    public void updateHistoryDisplay(StackPane answerHistory) {
        // clear the Stack Pane of all previous answers
        answerHistory.getChildren().clear();
        answerHistory.setPadding(new Insets(5.0, 5.0, 5.0, 5.0));

        // read the last 10 equations of the answer history queue
        String[] lastEquations = calc.getEquationsHistory();

        // build equation string
        String equationsOutput = "";
        int startEquation = Math.max(lastEquations.length - 5, 0);
        for(int i = startEquation; i < lastEquations.length; i++) {
            // add equations to every other line
            equationsOutput += ((lastEquations[i] == null)? "  " : lastEquations[i]) + "\n";
            equationsOutput += " \n";
        }
        // use equation string as text of equation label
        Label equationLabel = new Label(equationsOutput);
        equationLabel.setFont(Font.font("Verdana", 13));
        equationLabel.setTextFill(Color.rgb(78,185,54));
        equationLabel.setTextAlignment(TextAlignment.LEFT);
        equationLabel.setAlignment(Pos.BOTTOM_LEFT);

        equationLabel.setLineSpacing(13);
        equationLabel.setPrefWidth(265);
        answerHistory.getChildren().add(equationLabel);

        // read the last 5 answers from the answer history queue
        Double[] lastAnswers = calc.getAnswersHistory();
        // build answer string
        String answersOutput = "";
        int startAnswer = Math.max(lastAnswers.length - 5, 0);
        for(int i = startAnswer; i < lastAnswers.length; i++) {
            // add equations to every line
            answersOutput += " \n";
            answersOutput += ((lastAnswers[i] == null)? "  " : lastAnswers[i]) + "\n";
        }
        // use equation string as text of equation label
        Label answersLabel = new Label(answersOutput);
        answersLabel.setFont(Font.font("Verdana", 13));
        answersLabel.setTextFill(Color.rgb(78,185,54));
        answersLabel.setPrefWidth(265);
        answersLabel.setTextAlignment(TextAlignment.RIGHT);
        answersLabel.setAlignment(Pos.BOTTOM_RIGHT);
        answersLabel.setLineSpacing(13);
        answerHistory.getChildren().add(answersLabel);
    }
}
