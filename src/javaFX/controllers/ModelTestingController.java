package javaFX.controllers;

import com.sun.javafx.tk.Toolkit;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import experimentation.ResultsClass;
import experimentation.ResultsDataset;
import javaFX.Colors.Color;
import javaFX.Colors.Colors;
import javaFX.EntryPoint;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import model.randomForest.ModelHandlerRandomForest;
import org.apache.commons.io.FileUtils;
import org.apache.parquet.Strings;
import org.json.simple.parser.ParseException;
import org.w3c.dom.css.RGBColor;
import scala.tools.nsc.interpreter.Results;
import utils.DatasetReader;
import utils.FileUtilsCust;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ModelTestingController implements Initializable {

    @FXML
    TreeView<String> modelsTreeView;

    @FXML
    Button testButton;

    @FXML
    private TableView<DomainSelection> tableDomains;
    @FXML
    private TableColumn<DomainSelection, String> domainNames;
    @FXML
    private TableColumn<DomainSelection, Boolean> domainSelected;

    @FXML
    private CheckBox fold1;
    @FXML
    private CheckBox fold2;
    @FXML
    private CheckBox fold3;
    @FXML
    private CheckBox fold4;
    @FXML
    private CheckBox fold5;
    @FXML
    private CheckBox fold6;
    @FXML
    private CheckBox fold7;
    @FXML
    private CheckBox fold8;
    @FXML
    private CheckBox fold9;
    @FXML
    private CheckBox fold10;

    private Map<Integer, CheckBox> foldsMap;

    @FXML
    TreeView<String> resultsTreeView;

    @FXML
    private VBox vBox;

    private String modelPath;
    private String modelRoot;
    private String datasetsPath;
    private String tablesRoot;
    private String resultsRoot;

    private void displayMatrix(ActionEvent event, ResultsDataset results){
        Parent root;
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/MatrixViz.fxml"));
            root = loader.load();
            Scene scene = new Scene(root);
            MatrixVizController controller = loader.getController();
            controller.setResults(results);
            Stage stage = new Stage();
            stage.getIcons().setAll(new Image("/icon.png"));
            stage.setTitle("Similarity matrix visualization");
            stage.setScene(scene);
            stage.show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void selectedTest(TreeItem<String> resultsItem) throws IOException, ClassNotFoundException {
        TreeItem<String> domainItem;
        TreeItem<String> foldsItem;
        String classString = null;

        if(resultsItem.getParent().getValue().startsWith("A") || resultsItem.getParent().getValue().startsWith("R")){
            foldsItem = resultsItem.getParent().getParent();
            classString = resultsItem.getValue();
        } else {
            foldsItem = resultsItem.getParent();
        }
        domainItem = foldsItem.getParent();
        String domains = domainItem.getValue().replace(' ', '_');
        String folds = foldsItem.getValue().replace(' ', '_');
        String resultsPath = String.format("%s/%s/%s/results", resultsRoot, domains, folds);
        ResultsDataset results = (ResultsDataset)FileUtilsCust.load(resultsPath);
        vBox.getChildren().clear();
        if(resultsItem.getParent().getValue().startsWith("A") || resultsItem.getParent().getValue().startsWith("R")){
            //Class results
            Label classLabel = new Label(classString);
            classLabel.setFont(new Font(25));
            classLabel.setPadding(new Insets(50,0,50,0));
            vBox.getChildren().add(classLabel);
            ResultsClass rc = results.getResultsClasses().get(classString);
            vBox.getChildren().add(new Label(rc.toString()));
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            yAxis.setUpperBound(1.0);
            yAxis.setLowerBound(0.0);
            yAxis.setTickUnit(0.1);
            yAxis.setAutoRanging(false);
            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            chart.setTitle("Class results");
            xAxis.setLabel("Measure");
            yAxis.setLabel("Value");
            XYChart.Series series = new XYChart.Series();
            series.getData().add(new XYChart.Data("Precision", rc.getPrecision()));
            series.getData().add(new XYChart.Data("Recall", rc.getRecall()));
            series.getData().add(new XYChart.Data("F1", rc.getF1()));
            chart.getData().add(series);
            chart.setLegendVisible(false);
            chart.setStyle("CHART_COLOR_1: #414487");

            //Classified as pie chart
            Map<String, Integer> classifiedAs = results.getConfusionMatrix().row(classString);
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    classifiedAs.entrySet().stream()
                            .filter(e->e.getValue()>0)
                            .sorted(Comparator.comparing(Map.Entry::getValue))
                            .map(e->new PieChart.Data(e.getKey()+" ("+e.getValue()+")", e.getValue()))
                            .collect(Collectors.toList())
            );

            PieChart chart2 = new PieChart(pieChartData);
            chart2.setLabelsVisible(false);
            vBox.getChildren().add(chart);
            Label chart2Label = new Label("How instances were classified");
            chart2Label.setPadding(new Insets(20,0,0,0));
            chart2Label.setFont(new Font(20));
            vBox.getChildren().add(chart2Label);
            vBox.getChildren().add(chart2);

        } else {
            //Global results
            System.out.println(results.toString());
            Label resultsLabel = new Label(results.toString());
            vBox.getChildren().add(resultsLabel);
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            yAxis.setUpperBound(1.0);
            yAxis.setLowerBound(0.0);
            yAxis.setTickUnit(0.1);
            yAxis.setAutoRanging(false);
            BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
            chart.setTitle("Global results");
            xAxis.setLabel("Measure");
            yAxis.setLabel("Value");
            XYChart.Series series = new XYChart.Series();
            series.getData().add(new XYChart.Data("Accuracy", results.getAccuracy()));
            series.getData().add(new XYChart.Data("Macro Precision", results.getMacroPrecision()));
            series.getData().add(new XYChart.Data("Macro Recall", results.getMacroRecall()));
            series.getData().add(new XYChart.Data("Macro F1", results.getMacroF1()));
            chart.getData().add(series);
            chart.setLegendVisible(false);
            chart.setStyle("CHART_COLOR_1: #414487");
            vBox.getChildren().add(chart);
            //Matrix button
            Button matButton = new Button("Similarity matrix");
            matButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    displayMatrix(event, results);
                }
            });
            vBox.getChildren().add(matButton);
        }
    }

    private void selectedModel(TreeItem<String> foldsItem) throws IOException, ClassNotFoundException {
        TreeItem<String> domainItem = foldsItem.getParent();
        String domains = domainItem.getValue().replace(' ', '_');
        String folds = foldsItem.getValue().replace(' ', '_');
        modelRoot = String.format("%s/classifiersAndTables/%s/%s/modelClassifiers", modelPath, domains, folds);
        tablesRoot = String.format("%s/classifiersAndTables/%s/%s/modelTables", modelPath, domains, folds);
        resultsRoot = String.format("%s/classifiersAndTables/%s/%s/modelResults", modelPath, domains, folds);
        testButton.setDisable(false);
        addResults();
        System.out.println(modelRoot);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        File datasetsFolder;
        File[] domainFolders;
        DomainSelection ds;

        //Fold Checkbox map creation
        this.foldsMap = new HashMap<>();
        this.foldsMap.put(1, fold1);
        this.foldsMap.put(2, fold2);
        this.foldsMap.put(3, fold3);
        this.foldsMap.put(4, fold4);
        this.foldsMap.put(5, fold5);
        this.foldsMap.put(6, fold6);
        this.foldsMap.put(7, fold7);
        this.foldsMap.put(8, fold8);
        this.foldsMap.put(9, fold9);
        this.foldsMap.put(10, fold10);

        domainNames.setCellValueFactory(new PropertyValueFactory<DomainSelection, String>("name"));
        domainSelected.setCellFactory(CheckBoxTableCell.forTableColumn(domainSelected));
        domainSelected.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<DomainSelection, Boolean>, ObservableValue<Boolean>>() {

                    @Override
                    public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<DomainSelection, Boolean> param) {
                        return param.getValue().getSelected();
                    }
                });

        Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);
        this.modelPath = pref.get("modelPath", null);
        this.datasetsPath = pref.get("datasetsPath", null);

        datasetsFolder = new File(datasetsPath);
        domainFolders = datasetsFolder.listFiles(File::isDirectory);
        for (File file : domainFolders) {
            ds = new DomainSelection();
            ds.setName(file.getName());
            ds.setSelected(false);
            tableDomains.getItems().add(ds);
        }
        tableDomains.setEditable(true);
        domainSelected.setEditable(true);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {

                TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                if(selectedItem != null && selectedItem.isLeaf()){
                    try {
                        selectedModel(selectedItem);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    modelsTreeView.getSelectionModel().clearSelection();
                    testButton.setDisable(true);
                }
                // do what ever you want
            }
        };

        ChangeListener listener2 = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {

                TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                if(selectedItem != null && selectedItem.isLeaf()){
                    try {
                        selectedTest(selectedItem);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    resultsTreeView.getSelectionModel().clearSelection();
                }
                // do what ever you want
            }
        };

        modelsTreeView.getSelectionModel().selectedItemProperty().addListener(listener);
        modelsTreeView.getSelectionModel().clearSelection();
        resultsTreeView.getSelectionModel().selectedItemProperty().addListener(listener2);
        resultsTreeView.getSelectionModel().clearSelection();
        testButton.setDisable(true);
        addModels();
    }

    @FXML
    public void testModel() throws IOException, ClassNotFoundException, ParseException{
        testButton.setDisable(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                testModelRunnable();
                return null;
            }
            @Override
            protected void succeeded(){
                testButton.setDisable(false);
            }

            @Override
            protected void failed(){
                testButton.setDisable(false);
                //TODO borrar contenido de la carpeta
            }

        };
        new Thread(task).start();
    }

    public void testModelRunnable() throws IOException, ClassNotFoundException, ParseException {

        boolean selected = foldsMap.values().stream().anyMatch(f -> f.isSelected());
        if(! selected){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    displayError("No folds selected", "Please, select at least one testing fold.");
                }
            });
           return;
        }

        selected = tableDomains.getItems().stream().anyMatch(f -> f.isSelected());
        if(! selected){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    displayError("No domains selected", "Please, select at least one testing domain.");
                }
            });
            return;
        }

        ModelTestingController mtController = this;

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Parent root;
                try{
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/views/ModelTestingProgress.fxml"));
                    root = loader.load();
                    Scene scene = new Scene(root);
                    ModelTestingProgressController controller = loader.getController();
                    Stage stage = new Stage();
                    stage.getIcons().setAll(new Image("/icon.png"));
                    stage.setTitle("Model testing");
                    stage.setScene(scene);
                    stage.show();
                    controller.testModel(foldsMap, tableDomains, modelRoot, tablesRoot, resultsRoot, mtController);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void reloadTree(){
        modelsTreeView.getRoot().getChildren().clear();
        addModels();
    }

    public void addModels(){
        TreeItem<String> domainItem;
        TreeItem<String> foldsItem;

        modelsTreeView.setRoot(new TreeItem<>("Domains"));
        modelsTreeView.setShowRoot(false);
        File modelsRootFolder = new File(String.format("%s/classifiersAndTables", this.modelPath));
        for (File domainsFolder : modelsRootFolder.listFiles(File::isDirectory)) {
            domainItem = new TreeItem<>();
            domainItem.setValue(domainsFolder.getName().replace('_', ' '));
            for (File foldFolder : domainsFolder.listFiles(File::isDirectory)) {
                foldsItem = new TreeItem<>();
                foldsItem.setValue(foldFolder.getName().replace('_',' '));
                domainItem.getChildren().add(foldsItem);
            }
            modelsTreeView.getRoot().getChildren().add(domainItem);
        }
    }

    public void addResults() throws IOException, ClassNotFoundException {
        TreeItem<String> domainItem;
        TreeItem<String> foldsItem;
        TreeItem<String> recordItem;
        TreeItem<String> attributeItem;
        TreeItem<String> classItem;

        resultsTreeView.setRoot(new TreeItem<>("Domains"));
        resultsTreeView.setShowRoot(false);
        File resultsRootFolder = new File(resultsRoot);
        if(resultsRootFolder.exists()) {
            for (File domainsFolder : resultsRootFolder.listFiles(File::isDirectory)) {
                domainItem = new TreeItem<>();
                domainItem.setValue(domainsFolder.getName().replace('_', ' '));
                for (File foldFolder : domainsFolder.listFiles(File::isDirectory)) {
                    try {
                        foldsItem = new TreeItem<>();
                        foldsItem.setValue(foldFolder.getName().replace('_', ' '));

                        Set<String> attributeClasses = (HashSet<String>) FileUtilsCust.load(String.format("%s/attributeClasses", foldFolder.getPath()));
                        Set<String> recordClasses = (HashSet<String>) FileUtilsCust.load(String.format("%s/recordClasses", foldFolder.getPath()));

                        classItem = new TreeItem<>("Global results");
                        foldsItem.getChildren().add(classItem);

                        //Attributes
                        attributeItem = new TreeItem<>("Attribute classes");
                        for (String attributeClass : attributeClasses) {
                            classItem = new TreeItem<>(attributeClass);
                            attributeItem.getChildren().add(classItem);
                        }
                        foldsItem.getChildren().add(attributeItem);

                        //Records
                        recordItem = new TreeItem<>("Record classes");
                        for (String recordClass : recordClasses) {
                            classItem = new TreeItem<>(recordClass);
                            recordItem.getChildren().add(classItem);
                        }
                        foldsItem.getChildren().add(recordItem);

                        domainItem.getChildren().add(foldsItem);
                    }  catch(Exception e){
                        System.out.println(String.format("Error while trying to load domain %s with folds %s", domainsFolder.getName(), foldFolder.getName()));
                    }
                }
                resultsTreeView.getRoot().getChildren().add(domainItem);
            }
        }
    }

    private void displayError(String errorHeader, String errorContext){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(errorHeader);
        alert.setContentText(errorContext);
        alert.showAndWait();
    }
}
