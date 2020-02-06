package javaFX.controllers;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import experimentation.ResultsDataset;
import featuresCalculation.FeaturesGroup;
import javaFX.EntryPoint;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.randomForest.ModelHandlerRandomForest;
import org.apache.commons.io.FileUtils;
import org.apache.parquet.Strings;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ModelTestingProgressController implements Initializable {
    private ResultsDataset results;

    @FXML
    private ProgressBar bar;

    @FXML
    private Label l1;

    @FXML
    private Label l2;

    @FXML
    private TextArea textArea;

    private Map<Integer, CheckBox> foldsMap;
    private TableView<DomainSelection> tableDomains;
    private String modelRoot;
    private String tablesRoot;
    private String resultsRoot;
    private ModelTestingController modelTestingController;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void testModel(Map<Integer, CheckBox> foldsMap,
                          TableView<DomainSelection> tableDomains,
                          String modelRoot,
                          String tablesRoot,
                          String resultsRoot,
                          ModelTestingController modelTestingController){

        this.foldsMap = foldsMap;
        this.tableDomains = tableDomains;
        this.modelRoot = modelRoot;
        this.tablesRoot = tablesRoot;
        this.resultsRoot = resultsRoot;
        this.modelTestingController = modelTestingController;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                testModelRunnable();
                return null;
            }
        };
        new Thread(task).start();

    }

    public void testModelRunnable() throws Exception {
        List<Dataset> testingDatasets;
        DatasetReader datasetReader;
        ModelHandlerRandomForest modelHandler;
        String datasetsFolderPath;
        ClockMonitor clock;

        Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);
        String datasetsPath = pref.get("datasetsPath", null);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.0);
                textArea.appendText("Importing testing datasets...");
            }
        });
        //Testing dataset import
        datasetReader = new DatasetReader();
        testingDatasets = new ArrayList<>();
        for (DomainSelection domainSelection:
                tableDomains.getItems()) {
            if(domainSelection.isSelected()) {
                for (int i = 1; i <= 10; i++) {
                    if (this.foldsMap.get(i).isSelected()) {
                        datasetsFolderPath = String.format("%s/%s/%s", datasetsPath, domainSelection.getName(), i);
                        try {
                            datasetReader.addDataset(datasetsFolderPath, 1.0, testingDatasets);
                        } catch (IOException e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    displayError("Dataset reading error", "There was an error while importing the dataset files. Please, check that every domain folder contains 10 folder (named 1, 2, 3 ... 10), and that every folder only contains json folders with datasets in the appropiate format");
                                }
                            });
                            throw e;
                        } catch (Exception e){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    displayError("Unknown error", "There was an unknown error while importing the datasets, unrelated to finding the dataset files.");
                                }
                            });
                            throw e;
                        }
                    }
                }
            }
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.2);
                textArea.appendText("\nSetting up the model handler...");
            }
        });
        //Setting up the model handler
        List<String> folds = foldsMap.entrySet().stream().filter(e -> e.getValue().isSelected()).map(e -> e.getKey().toString()).collect(Collectors.toList());
        List<String> domains = tableDomains.getItems().stream().filter(d -> d.isSelected()).map(d -> d.getName()).collect(Collectors.toList());
        Collections.sort(domains);
        String foldsString = Strings.join(folds, "_");
        String domainsString = Strings.join(domains, "_");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                l1.setText(String.format("Using %s", modelRoot));
                l2.setText(String.format("Applying to domains %s, folds %s", domainsString, foldsString));
            }
        });
        modelHandler = new ModelHandlerRandomForest();
        System.out.println("Creating context");
        modelHandler.createNewContext();
        System.out.println("Setting folders");
        modelHandler.setClassifiersRootFolder(modelRoot);
        modelHandler.setTablesRootFolder(tablesRoot);
        System.out.println("Loading feature calculators");
        modelHandler.loadFeaturesCalculators();
        System.out.println("Loading classifiers");
        modelHandler.loadClassifiers();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.25);
                textArea.appendText("\nSetting up the results folder path...");
            }
        });
        //Setting up the results folder path
        String resultsRootSpecific = String.format("%s/%s/%s", resultsRoot, domainsString, foldsString);
        File resultsFolder = new File(resultsRootSpecific);
        if(!resultsFolder.exists()){
            resultsFolder.mkdirs();
        } else {
            FileUtils.cleanDirectory(resultsFolder);
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.3);
                textArea.appendText("\nTesting. This may take a while...");
            }
        });
        //Testing
        clock = new ClockMonitor();
        clock.start();
        for (Dataset dataset: testingDatasets) {
            modelHandler.refineHintsUnlabelledDataset(dataset);
            modelHandler.refineHintsOnce(dataset);
            modelHandler.saveResults(dataset, resultsRootSpecific);
        }
        modelHandler.closeContext();
        clock.stop();
        FileUtilsCust.createCSV(String.format("%s/applicationTime.csv", resultsRootSpecific));
        FileUtilsCust.addLine(String.format("%s/applicationTime.csv", resultsRootSpecific), clock.getCPUTime());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.8);
                textArea.appendText("\nComputing metrics...");
            }
        });
        //Results, and storing classes
        HashSet<String> attributeClasses = new HashSet<>();
        HashSet<String> recordClasses = new HashSet<>();
        ResultsDataset results = new ResultsDataset();
        /*
        for(String recordClass : modelHandler.getClassesConfiguration().getRecordClasses()){
            recordClasses.add(recordClass);
        }
        for(String attributeClass : modelHandler.getClassesConfiguration().getAttributeClasses()){
            recordClasses.add(attributeClass);
        }*/
        for (Dataset dataset : testingDatasets) {
            updateResults(results, dataset, attributeClasses, recordClasses);
        }
        results.computeMeasures();
        System.out.println(results);
        String resultPath = String.format("%s/results", resultsRootSpecific);
        FileUtilsCust.save(results, resultPath);
        FileUtilsCust.save(recordClasses, String.format("%s/recordClasses", resultsRootSpecific));
        FileUtilsCust.save(attributeClasses, String.format("%s/attributeClasses", resultsRootSpecific));
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(1.0);
                textArea.appendText("\nDONE");
                try {
                    modelTestingController.addResults();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateResults(ResultsDataset results, Dataset dataset, Set<String> attributeClasses, Set<String> recordClasses){
        assert dataset != null;
        List<Slot> children;

        children = dataset.getSlots();
        for (Slot child : children) {
            updateResults(results, child, attributeClasses, recordClasses);
        }
    }

    private void updateResults(ResultsDataset results, Slot slot, Set<String> attributeClasses, Set<String> recordClasses){
        assert slot != null;
        List<Slot> children;

        results.addPrediction(slot.getSlotClass(), slot.getHint());

        if (slot instanceof Record) {
            children = ((Record)slot).getSlots();
            recordClasses.add(slot.getSlotClass());
            recordClasses.add(slot.getHint());
            for (Slot child : children) {
                updateResults(results, child, attributeClasses, recordClasses);
            }
        } else {
            attributeClasses.add(slot.getSlotClass());
            attributeClasses.add(slot.getHint());
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
