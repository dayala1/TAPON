package javaFX.controllers;

import javaFX.TextConsole;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;

import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

public class TabPanelController implements Initializable {
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab modelCreationTab;
    @FXML
    private Tab modelVizBar;
    @FXML
    private Tab modelTestingBar;

    @FXML
    private ModelCreationController modelCreationController;
    @FXML
    private ModelVizController modelVizController;
    @FXML
    private ModelVizController modelTestingController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TextConsole textConsole = new TextConsole(console);
        //PrintStream ps = new PrintStream(textConsole, true);
        //System.setOut(ps);
        //System.setErr(ps);
    }
}
