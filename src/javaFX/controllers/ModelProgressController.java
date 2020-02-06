package javaFX.controllers;

import dataset.Dataset;
import experimentation.ResultsDataset;
import featuresCalculation.FeaturesGroup;
import javaFX.EntryPoint;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.randomForest.ModelHandlerRandomForest;
import org.apache.parquet.Strings;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ModelProgressController implements Initializable {
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
    private Map<String, String> params;
    private Set<FeaturesGroup> slotFeaturesGroups;
    private Set<FeaturesGroup> hintSlotFeaturesGroups;
    private Set<FeaturesGroup> featurableFeaturesGroups;
    private Set<FeaturesGroup> hintFeaturableFeaturesGroups;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void createModel(Map<Integer, CheckBox> foldsMap,
                            TableView<DomainSelection> tableDomains,
                            Map<String, String> params,
                            Set<FeaturesGroup> slotFeaturesGroups,
                            Set<FeaturesGroup> hintSlotFeaturesGroups,
                            Set<FeaturesGroup> featurableFeaturesGroups,
                            Set<FeaturesGroup> hintFeaturableFeaturesGroups){

        this.foldsMap = foldsMap;
        this.tableDomains = tableDomains;
        this.params = params;
        this.slotFeaturesGroups = slotFeaturesGroups;
        this.hintSlotFeaturesGroups = hintSlotFeaturesGroups;
        this.featurableFeaturesGroups = featurableFeaturesGroups;
        this.hintFeaturableFeaturesGroups = hintFeaturableFeaturesGroups;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                createModelRunnable();
                return null;
            }
        };
        new Thread(task).start();

    }

    public void createModelRunnable() throws Exception {
        ClockMonitor clock;
        List<Dataset> trainingDatasets;
        DatasetReader datasetReader;
        String modelRoot;
        String datasetsRoot;
        String datasetsPath;
        ModelHandlerRandomForest modelHandler;

        clock = new ClockMonitor();
        Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);
        datasetReader = new DatasetReader();
        trainingDatasets = new ArrayList<>();
        datasetsRoot = pref.get("datasetsPath", null);

        //Training dataset import
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.0);
                textArea.appendText("Importing training datasets...");
            }
        });
        for (DomainSelection domainSelection:
                tableDomains.getItems()) {
            if(domainSelection.isSelected()) {
                for (int i = 1; i <= 10; i++) {
                    if (this.foldsMap.get(i).isSelected()) {
                        datasetsPath = String.format("%s/%s/%s", datasetsRoot, domainSelection.getName(), i);
                        try {
                            datasetReader.addDataset(datasetsPath, 1.0, trainingDatasets);
                        } catch (IOException e) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    displayError("Dataset reading error", "There was an error while importing the dataset files. Please, check that every domain folder contains 10 folder (named 1, 2, 3 ... 10), and that every folder only contains json folders with datasets in the appropiate format");
                                }
                            });
                            return;
                        } catch (Exception e){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    displayError("Unknown error", "There was an unknown error while importing the datasets, unrelated to finding the dataset files.");
                                }
                            });
                            return;
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
        modelRoot = pref.get("modelPath", null);
        java.util.List<String> folds = foldsMap.entrySet().stream().filter(e -> e.getValue().isSelected()).map(e -> e.getKey().toString()).collect(Collectors.toList());
        java.util.List<String> domains = tableDomains.getItems().stream().filter(d -> d.isSelected()).map(d -> d.getName()).collect(Collectors.toList());
        Collections.sort(domains);
        String foldsString = Strings.join(folds, "_");
        String domainsString = Strings.join(domains, "_");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                l1.setText(domainsString);
                l2.setText(foldsString);
            }
        });
        modelHandler = new ModelHandlerRandomForest();
        modelHandler.setClassifiersRootFolder(String.format("%s/classifiersAndTables/%s/%s/modelClassifiers", modelRoot, domainsString, foldsString));
        modelHandler.setTablesRootFolder(String.format("%s/classifiersAndTables/%s/%s/modelTables", modelRoot, domainsString, foldsString));
        modelHandler.setFeaturableFeaturesGroups(featurableFeaturesGroups);
        modelHandler.setSlotFeaturesGroups(slotFeaturesGroups);
        modelHandler.setHintsFeaturableFeaturesGroups(hintFeaturableFeaturesGroups);
        modelHandler.setHintsSlotFeaturesGroups(hintSlotFeaturesGroups);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.3);
                textArea.appendText("\nTraining the model. This may take some time...");
            }
        });
        //Training the model
        clock.start();
        modelHandler.trainModel(trainingDatasets, params);
        clock.stop();
        FileUtilsCust.createCSV(String.format("%s/classifiersAndTables/%s/%s/modelClassifiers/trainingTime.csv", modelRoot, domainsString, foldsString));
        FileUtilsCust.addLine(String.format("%s/classifiersAndTables/%s/%s/modelClassifiers/trainingTime.csv", modelRoot, domainsString, foldsString), clock.getCPUTime());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(0.85);
                textArea.appendText("\nCreating the model visualization...");
            }
        });
        modelHandler.loadFeatureNames();
        modelHandler.writeClassifiersDebug();
        modelHandler.closeContext();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                bar.setProgress(1.0);
                textArea.appendText("\nDONE");
            }
        });
    }

    private void displayError(String errorHeader, String errorContext){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(errorHeader);
        alert.setContentText(errorContext);
        alert.showAndWait();
    }
}
