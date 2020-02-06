package javaFX.controllers;

import javaFX.EntryPoint;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import utils.RandomForestParsing.RandomForestParser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class ModelVizController implements Initializable{

    @FXML
    TreeView<String> treeView;

    @FXML
    WebView webView;

    private String modelPath;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);
        this.modelPath = pref.get("modelPath", null);
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {

                TreeItem<String> selectedItem = (TreeItem<String>) newValue;
                if(selectedItem != null && selectedItem.isLeaf()){
                    setWeb(selectedItem);
                }
                // do what ever you want
            }
        };
        treeView.getSelectionModel().selectedItemProperty().addListener(listener);

        addModels();
    }

    private void setWeb(TreeItem<String> classItem){
        TreeItem<String> instanceItem = classItem.getParent();
        TreeItem<String> hintsItem = instanceItem.getParent();
        TreeItem<String> foldsItem = hintsItem.getParent();
        TreeItem<String> domainItem = foldsItem.getParent();
        String domains = domainItem.getValue().replace(' ', '_');
        String folds = foldsItem.getValue().replace(' ', '_');
        String hints = hintsItem.getValue().charAt(5)=='f'?"hintFree":"hintBased";
        String instance = instanceItem.getValue().charAt(0)=='A'?"attributes":"records";
        String instanceClass = classItem.getValue().replaceAll("#", "%23");
        String resourcePath = String.format("file:///%s/classifiersAndTables/%s/%s/modelClassifiers/classifiersParsed/%s/%s/%s/home.html", modelPath, domains, folds, hints, instance, instanceClass)
                .replace('\\', '/');
        webView.getEngine().load(resourcePath);
        System.out.println(resourcePath);
    }

    @FXML
    public void reloadTree(){
        treeView.getRoot().getChildren().clear();
        addModels();
    }

    public void addModels(){
        TreeItem<String> domainItem;
        TreeItem<String> foldsItem;
        TreeItem<String> hintFreeItem;
        TreeItem<String> hintBasedItem;
        TreeItem<String> attributesItem;
        TreeItem<String> recordsItem;
        TreeItem<String> classItem;
        File classesFolder;
        List<Pair<String, List<String>>> treeStrings;

        treeView.setRoot(new TreeItem<>("Domains"));
        treeView.setShowRoot(false);
        File modelsRootFolder = new File(String.format("%s/classifiersAndTables", this.modelPath));
        modelsRootFolder.mkdirs();
        for (File domainsFolder : modelsRootFolder.listFiles(File::isDirectory)) {
            domainItem = new TreeItem<>();
            domainItem.setValue(domainsFolder.getName().replace('_', ' '));
            for (File foldFolder : domainsFolder.listFiles(File::isDirectory)) {
                try {
                    foldsItem = new TreeItem<>();
                    foldsItem.setValue(foldFolder.getName().replace('_', ' '));

                    //HINT-FREE
                    hintFreeItem = new TreeItem<>("Hint-free");
                    //ATTRIBUTES
                    attributesItem = new TreeItem<>("Attribute classes");
                    classesFolder = new File(String.format("%s/modelClassifiers/classifiersParsed/hintFree/attributes", foldFolder.getPath()));
                    for (File classFile : classesFolder.listFiles(File::isDirectory)) {
                        classItem = new TreeItem<>(classFile.getName());
                        attributesItem.getChildren().add(classItem);
                    }
                    hintFreeItem.getChildren().add(attributesItem);
                    //RECORDS
                    recordsItem = new TreeItem<>("Record classes");
                    classesFolder = new File(String.format("%s/modelClassifiers/classifiersParsed/hintFree/records", foldFolder.getPath()));
                    for (File classFile : classesFolder.listFiles(File::isDirectory)) {
                        classItem = new TreeItem<>(classFile.getName());
                        recordsItem.getChildren().add(classItem);
                    }
                    hintFreeItem.getChildren().add(recordsItem);
                    foldsItem.getChildren().add(hintFreeItem);

                    //HINT-BASED
                    hintBasedItem = new TreeItem<>("Hint-based");
                    //ATTRIBUTES
                    attributesItem = new TreeItem<>("Attribute classes");
                    classesFolder = new File(String.format("%s/modelClassifiers/classifiersParsed/hintBased/attributes", foldFolder.getPath()));
                    for (File classFile : classesFolder.listFiles(File::isDirectory)) {
                        classItem = new TreeItem<>(classFile.getName());
                        attributesItem.getChildren().add(classItem);
                    }
                    hintBasedItem.getChildren().add(attributesItem);
                    //RECORDS
                    recordsItem = new TreeItem<>("Record classes");
                    classesFolder = new File(String.format("%s/modelClassifiers/classifiersParsed/hintBased/records", foldFolder.getPath()));
                    for (File classFile : classesFolder.listFiles(File::isDirectory)) {
                        classItem = new TreeItem<>(classFile.getName());
                        recordsItem.getChildren().add(classItem);
                    }
                    hintBasedItem.getChildren().add(recordsItem);
                    foldsItem.getChildren().add(hintBasedItem);

                    //Folds item is complete and can be added
                    domainItem.getChildren().add(foldsItem);
                } catch(Exception e){
                    System.out.println(String.format("Error while trying to load domain %s with folds %s", domainsFolder.getName(), foldFolder.getName()));
                }
            }
            treeView.getRoot().getChildren().add(domainItem);
        }
    }
}
