/*
 * GraphDisplayer.java
 * draws rectangles to represent points on a graph. Solves the equation for each x value to determine
 * the x and y coordinates for each point. Draws axis as a reference for the graph
 */
package com.ryan.graphcalc;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.geometry.*;
import java.util.Optional;

/**
 * Created by ryanj on 8/6/2017.
 * plot a graph for an equation that follows the format y = f(x)
 */
public class GraphDisplayer {
  // the maximum number of points that can be plotted
  private static int MAX_GRAPH_STEPS = (int) Math.pow(10, 4) * 40;

  // the Stage to use as the top level container
  private Stage mWindow;

  // the Scene container for all content
  private Scene mScene;

  // screen coordinates of graph
  private static final double graphWidth = 600.0;

  // Calculator used to solve for x
  private Calculator calc = new Calculator();

  // Calculator used to solve trig functions
  private TrigonometryCalculator trigCalculator = new TrigonometryCalculator();

  // factor to multiply actual values to get their pixel values
  private double mScaleFactor;

  // minimum value for x
  private double mStartRangeX;

  // maximum value for x
  private double mEndRangeX;

  // value to increment x for each point
  private double mStepSize;

  // color to paint each point
  private Paint plotColor;

  // root node for window
  private StackPane mStackPane;

  // group node for graph lines
  private Group plotArea;

  // equation to solve for
  private String mSolveEquation;

  public static Double settingsReturnVal;

  // screen dimensions for width
  private double mScreenWidth;

  // screen dimensions for height
  private double mScreenHeight;

  // left margin for beginning of x-axis
  private double mLeftMargin;

  // top margin for beginning of y-axis
  private double mTopMargin;

  // graph line width
  private double mGraphLineWidth;

  // constructor for GraphDisplayer
  public GraphDisplayer() {
    // defaults for parameters of graph

    // left boundary of x
    mStartRangeX = -15;

    // right boundary of x
    mEndRangeX = 15;

    // change in for each point on graph
    mStepSize = 0.025;

    // adjust padding for window
    mLeftMargin = 22;
    mTopMargin = 22;

    // determine scale factor by ratio of minimum / maximum values for x
    mScaleFactor = (((mEndRangeX - mStartRangeX) != 0.0) ? 600 / (mEndRangeX - mStartRangeX) : 1);

    // how much width for the graph line (dimensions of each point)
    mGraphLineWidth = 2;
    // color to paint graph line with
    plotColor = Color.DARKCYAN;
  }

  // add a new graph to be plotted
  public void addNewGraphAndOpen(String inputEquation) {
    // equation used to solve for x
    mSolveEquation = inputEquation;
    // create a new Stage object for the window
    mWindow = new Stage();

    plotGraph();
  }

  public void plotGraph() {
    // uncomment for modal dialog (to make it so can't return focus to main window)
    //mWindow.initModality(Modality.APPLICATION_MODAL);

    // use stackPane as root node
    mStackPane = new StackPane();

    plotArea = new Group();

    // use the StackPane as the parent of the group
    mStackPane.getChildren().add(plotArea);

    // add static text to display what the equation is
    displayEquationLabel();

    // menu bar with options for graph
    MenuBar mMainMenuBar = new MenuBar();

    // add range menus to alter graph settings, window menu to close window
    mMainMenuBar.getMenus().addAll(buildSettingsMenu(), buildWindowMenu());

    BorderPane layout = new BorderPane();
    // menu bar goes at top
    layout.setTop(mMainMenuBar);
    // graph goes in center of window
    layout.setCenter(mStackPane);

    // add root node to the new scene
    mScene = new Scene(layout, 740, 740);

    // set the new scene for the stage
    mWindow.setScene(mScene);

    // calculate screen width, height
    mScreenWidth = mScene.getWidth() - mLeftMargin * 2;
    mScreenHeight = mScene.getHeight() - mTopMargin * 2;

    // determine scale factor to map actual values to screen values
    mScaleFactor = (((mEndRangeX - mStartRangeX) != 0.0) ? mScreenWidth / (mEndRangeX - mStartRangeX) : 1);

    // draw the graph points
    drawAllPoints();

    // draw x-axis and labels
    drawXAxis();

    // draw Y-axis and labels
    drawYAxis();

    // close button to close the window
    addCloseButton();

    // show the stage if this is the first time calling plotGraph
    if(!mWindow.isShowing()) {
      mWindow.showAndWait();
    }
  }

  // build a close button at bottom of screen
  public void addCloseButton() {
    // create the button control
    Button closeButton = new Button("Close Window");

    // lambda function to handle button click
    closeButton.setOnAction(e -> mWindow.close());

    // HBox to display it at bottom of screen
    HBox hbButtons = new HBox();
    hbButtons.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
    hbButtons.setAlignment(Pos.BOTTOM_RIGHT);
    hbButtons.getChildren().add(closeButton);
    mStackPane.getChildren().add(hbButtons);
  }

  // open dialog to ask user what range should be
  private Double askRangePrompt(String promptMsg, String headerMsg) {
    TextInputDialog askRangeDialog = new TextInputDialog();
    askRangeDialog.setTitle("Graph Settings");
    // the prompt message will direct user what to enter
    askRangeDialog.setContentText(promptMsg);
    askRangeDialog.setHeaderText(headerMsg);
    Optional<String> minXAnswer = askRangeDialog.showAndWait();
    minXAnswer.ifPresent(minXAnswerStr -> settingsReturnVal = Double.parseDouble(minXAnswerStr));
    return settingsReturnVal;
  }

  // draw a point on the graph as a rectangle
  private void drawGraphPoint(double xVal, double yVal) {
    // account for coordinate system (starts at top left for origin)
    double xScreenVal = mScreenWidth / 2 + mLeftMargin;
    double yScreenVal = mScreenHeight / 2 + 1;

    // multiply actual values by scaling factor
    xScreenVal += mScaleFactor * xVal;
    yScreenVal -= mScaleFactor * yVal;

    // if out of range
    if(xScreenVal < 0 || xScreenVal >= mScreenWidth
       || yScreenVal < 0 || yScreenVal >= mScreenHeight) {
      return;
    }
    //create a point at the coordinates
    Rectangle point = new Rectangle(xScreenVal, yScreenVal, mGraphLineWidth, mGraphLineWidth);
    point.setFill(plotColor);
    point.setStroke(plotColor);
    point.setStrokeWidth(1);

    // add to the group node
    plotArea.getChildren().add(point);
  }

  // draw all graph points to create a line / curve
  public void drawAllPoints() {
    for (double xVal = mStartRangeX; xVal < mEndRangeX; xVal += mStepSize) {
      // use try - catch to catch problems evaluating equation for a certain x value
      try {
        // call the function that includes a second parameter so that each x can be replaced with xVal
        String processedEquation = trigCalculator.processTrigFunctions(mSolveEquation, xVal);
        double yVal = calc.processLine(processedEquation, xVal);
        // draw each point to mimic a line or curve
        drawGraphPoint(xVal, yVal);
      } catch (Exception exception) {
        System.err.println("Error printing point");
      }
    }
  }

  // display equation at top left of screen
  public void displayEquationLabel() {
    // make sure equation name includes left hand side
    String equationName = ((mSolveEquation.indexOf('=') == -1) ? "y = " + mSolveEquation : mSolveEquation);

    // initialize text node with equation string
    Text equationFunctionTxt = new Text(equationName);

    // use bold text to make it clearly visible
    equationFunctionTxt.setFont(Font.font("verdana", FontWeight.BOLD, 15));

    // color is same as the graph points
    equationFunctionTxt.setFill(plotColor);

    // HBox to make it appear at top left
    HBox hEquationInfoBox = new HBox();
    hEquationInfoBox.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
    hEquationInfoBox.setAlignment(Pos.TOP_LEFT);
    hEquationInfoBox.getChildren().add(equationFunctionTxt);
    mStackPane.getChildren().add(hEquationInfoBox);
  }

  // draw x-axis by using a Line shape
  private void drawXAxis() {
    // margin from top of screen
    double topMargin = 5;

    // maximum of start, end range determines boundaries
    double maxRange = Math.max(Math.abs(mStartRangeX), Math.abs(mEndRangeX));

    // draw x-axis
    Line xAxis = new Line(mLeftMargin, mScreenHeight / 2 + topMargin,
                        mScreenWidth + mLeftMargin,mScreenHeight / 2 + topMargin);
    //Line xAxis = new Line(-245, 255, 305, 255);
    xAxis.setStrokeWidth(1.0f);
    xAxis.getStrokeDashArray().addAll(4d);
    xAxis.setStroke(Color.BLACK);
    plotArea.getChildren().add(xAxis);

    //NumberFormat numberFormat = new
    // add labels to the axis
    Text boundaryTxt0 = new Text(mLeftMargin - 7, mScreenHeight / 2 + topMargin + 15, String.format("%.2f", -maxRange));
    plotArea.getChildren().add(boundaryTxt0);
    Text boundaryTxt1 = new Text(mLeftMargin - 7 + mScreenWidth * 1 / 4, mScreenHeight / 2 + topMargin + 15, String.format("%.2f", -maxRange * 0.5));
    plotArea.getChildren().add(boundaryTxt1);
    Text boundaryTxt2 = new Text(mLeftMargin - 7 + mScreenWidth * 3 / 4, mScreenHeight / 2 + topMargin + 15, String.format("%.2f", maxRange * 0.5));
    plotArea.getChildren().add(boundaryTxt2);
    Text boundaryTxt3 = new Text(mScreenWidth + topMargin - 7, mScreenHeight / 2 + topMargin + 15, String.format("%.2f", maxRange));
    plotArea.getChildren().add(boundaryTxt3);

    // add line at left boundary to use as reference point
    Line boundaryLine0 = new Line(mLeftMargin, mScreenHeight / 2 + topMargin - 3, mLeftMargin, mScreenHeight / 2 + topMargin + 3);
    boundaryLine0.setStrokeWidth(1);
    boundaryLine0.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine0);

    // add line between left boundary and center
    Line boundaryLine1 = new Line(mLeftMargin + mScreenWidth * 1 / 4, mScreenHeight / 2 + topMargin - 3,
                          mLeftMargin + mScreenWidth * 1 / 4, mScreenHeight / 2 + topMargin + 3);
    boundaryLine1.setStrokeWidth(1);
    boundaryLine1.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine1);

    // add line between center and right boundary
    Line boundaryLine2 = new Line(mLeftMargin + mScreenWidth * 3 / 4, mScreenHeight / 2 + topMargin - 3,
            mLeftMargin + mScreenWidth * 3 / 4, mScreenHeight / 2 + topMargin + 3);
    boundaryLine2.setStrokeWidth(1);
    boundaryLine2.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine2);

    // add line at right boundary
    Line boundaryLine3 = new Line(mLeftMargin + mScreenWidth, mScreenHeight / 2 + topMargin - 3,
            mLeftMargin + mScreenWidth, mScreenHeight / 2 + topMargin + 3);

    boundaryLine3.setStrokeWidth(1);
    boundaryLine3.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine3);
  }

  // draw y-axis by using a Line shape
  private void drawYAxis() {
    // margin from top of screen
    double topMargin = 5;

    // use maximum as boundary
    double maxRange = Math.max(Math.abs(mStartRangeX), Math.abs(mEndRangeX));

    // draw vertical line across window for y-axis
    //Line YAxis = new Line(screenWidth / 2, 5, screenWidth / 2, 505);
    Line YAxis = new Line(mScreenWidth / 2 + mLeftMargin, topMargin, mScreenWidth / 2 + mLeftMargin, mScreenHeight + topMargin);

    //Line YAxis = new Line(305, 5, 305, 505);
    YAxis.setStrokeWidth(1.0f);
    YAxis.getStrokeDashArray().addAll(4d);
    YAxis.setStroke(Color.BLACK);
    plotArea.getChildren().add(YAxis);

    // add labels to the axis for reference
    Text boundaryTxt0 = new Text(mScreenWidth / 2 + mLeftMargin + 5, topMargin + 5, String.format("%.2f", maxRange));
    plotArea.getChildren().add(boundaryTxt0);
    Text boundaryTxt1 = new Text(mScreenWidth / 2 + mLeftMargin + 5, mScreenHeight / 4 + topMargin + 5 , String.format("%.2f", maxRange * 0.5));
    plotArea.getChildren().add(boundaryTxt1);
    Text boundaryTxt2 = new Text(mScreenWidth / 2 + mLeftMargin + 5, mScreenHeight * 3 / 4 + topMargin + 5, String.format("%.2f", -maxRange * 0.5));
    plotArea.getChildren().add(boundaryTxt2);
    Text boundaryTxt3 = new Text(mScreenWidth / 2 + mLeftMargin + 5, mScreenHeight + 0 + topMargin, String.format("%.2f", -maxRange));
    plotArea.getChildren().add(boundaryTxt3);

    // add horizontal line #1 (maximum Y)
    Line boundaryLine0 = new Line(mScreenWidth / 2 + mLeftMargin - 3, topMargin,
            mScreenWidth / 2 + mLeftMargin + 3, topMargin);
    boundaryLine0.setStrokeWidth(1);
    boundaryLine0.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine0);

    // add horizontal line #2 1/4 down
    Line boundaryLine1 = new Line(mScreenWidth / 2 + mLeftMargin - 3, mScreenHeight * 1 / 4 + topMargin,
            mScreenWidth / 2 + mLeftMargin + 3, mScreenHeight * 1 / 4 + topMargin);
    boundaryLine1.setStrokeWidth(1);
    boundaryLine1.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine1);

    // add horizontal line #3 3/4 down

    Line boundaryLine2 = new Line(mScreenWidth / 2 + mLeftMargin - 3, mScreenHeight * 3 / 4 + topMargin,
            mScreenWidth / 2 + mLeftMargin + 3, mScreenHeight * 3 / 4 + topMargin);
    boundaryLine2.setStrokeWidth(1);
    boundaryLine2.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine2);

    // add horizontal line #4 minimum y
    Line boundaryLine3 = new Line(mScreenWidth / 2 + mLeftMargin - 3, mScreenHeight + topMargin,
            mScreenWidth / 2 + mLeftMargin + 3, mScreenHeight + topMargin);
    boundaryLine3.setStrokeWidth(1);
    boundaryLine3.setStroke(Color.BLACK);
    plotArea.getChildren().add(boundaryLine3);
  }

  // build a menu with the settings needed to change how graph is plotted
  private Menu buildSettingsMenu() {
    Menu settingsMenu = new Menu("_Settings");
    settingsMenu.setMnemonicParsing(true);
    MenuItem rangeMenuItem = new MenuItem("Enter _Range");
    rangeMenuItem.setMnemonicParsing(true);
    MenuItem stepMenuItem = new MenuItem("Enter _Step Size");
    stepMenuItem.setMnemonicParsing(true);

    // add the MenuItem options
    settingsMenu.getItems().addAll(
            rangeMenuItem,
            stepMenuItem
    );

    // set action handler for clicking to change range
    rangeMenuItem.setOnAction(e -> {
      mStartRangeX = askRangePrompt("Enter minimum for x: ", "Range for x values");
      if(mStartRangeX >= 0) {
        // show a dialog message to notify what went wrong
        showGraphSettingsAlertMsg("Error: The minimum value for x must be less than 0");
        return;
      }
      mEndRangeX = askRangePrompt("Enter maximum for x: ", "Range for x values");
      if(mEndRangeX <= 0) {
        showGraphSettingsAlertMsg("Error: The maximum value for x must be greater than 0");
        return;
      }
      if(calcTotalPoints() > MAX_GRAPH_STEPS) {
        showGraphSettingsAlertMsg("Error: The range is too large. Try setting a smaller step size first.");
        return;
      }
      // call plotGraph again to show the graph with the changes
      plotGraph();
    });

    // action handler for changing delta x (distance between points
    stepMenuItem.setOnAction(e -> {
      mStepSize = askRangePrompt("Enter step size: ", "Delta of x");
      if(mStepSize <= 0) {
        showGraphSettingsAlertMsg("Error: The step size must be greater than 0");
        return;
      }
      if(calcTotalPoints() > MAX_GRAPH_STEPS) {
        showGraphSettingsAlertMsg("Error: The step size is too small. Try setting a larger step size.");
        return;
      }
      plotGraph();
    });
    return settingsMenu;
  }

  // calculate the number of points that will be plotted when graph is made
  private int calcTotalPoints() {
    int numPoints = 0;
    if(mStepSize != 0) {
      numPoints = (int) Math.round((mEndRangeX - mStartRangeX) / mStepSize);
    }
    return numPoints;
  }

  // build a menu for window options
  private Menu buildWindowMenu() {
    Menu windowMenu = new Menu("_Window");
    windowMenu.setMnemonicParsing(true);
    MenuItem closeMenuItem = new MenuItem("_Close");
    closeMenuItem.setMnemonicParsing(true);
    windowMenu.getItems().addAll(closeMenuItem);

    // action handler to close window
    closeMenuItem.setOnAction(e -> mWindow.close());
    return windowMenu;
  }

  // show an alert dialog if something went wrong
  private void showGraphSettingsAlertMsg(String errorMsg) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText("Graph was not plotted");
    alert.setContentText(errorMsg);
    alert.setTitle("Graph Settings Error");
    alert.showAndWait();
  }
}