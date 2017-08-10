/*
 * GraphCalcApp.java
 * The GraphCalc application allows for plotting of equations on a graph. Recursion is used to evaluate
 * the equations by the correct order of precedence. So y = 2 + 3 * 7 will evaluate to 23 instead of 35.
 *
 */
package com.ryan.graphcalc;


import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GraphCalcApp extends Application  {

    // set up the primary stage of the application
    public void start(Stage primaryStage) throws Exception {
        // load the FXML file which has the object hierarchy
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(GraphCalcControl.class.getResourceAsStream("GraphCalcDisplay.fxml"));
        final GraphCalcControl controller = loader.getController();

        // open results history as soon as window is shown
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>()
        {
            @Override
            public void handle(WindowEvent window)
            {
                controller.openResultsHistory();
            }
        });
        Scene mainScene = new Scene(root);
        String css = this.getClass().getResource("style.css").toExternalForm();
        mainScene.getStylesheets().add(css);
        primaryStage.setScene(mainScene);
        primaryStage.show();
        primaryStage.setTitle("GeoGraphs 1.0");
    }
    // launch the application
    public static void main(String[] args) {
        launch(args);
    }
}
