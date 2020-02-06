package javaFX.controllers;

import javaFX.EntryPoint;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class RootSelectorController implements Initializable {

	@FXML
	private Button doneButton;
	@FXML
	private Button datasetsPathButton;
	@FXML
	private Button modelPathButton;
	@FXML
	private TextField datasetsPathText;
	@FXML
	private TextField modelPathText;

	private DirectoryChooser dc;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String storedDatasetsPath;
		String storedModelPath;

		Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);

		storedDatasetsPath = pref.get("datasetsPath", null);
		if (storedDatasetsPath != null) {
			this.datasetsPathText.setText(storedDatasetsPath);
		}

		storedModelPath = pref.get("modelPath", null);
		if (storedModelPath != null) {
			this.modelPathText.setText(storedModelPath);
		}
	}

	@FXML
	public void chooseDatasetsPath(ActionEvent event) {
		File chosenFile;
		String chosenFilePath;
		File userDirectory = null;

		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		dc = new DirectoryChooser();
		dc.setTitle("Choose the datasets root folder");

		String formerPath = datasetsPathText.getText();
		if(formerPath != null){
			userDirectory = new File(formerPath).getParentFile();
		}

		if(userDirectory == null || !userDirectory.canRead()) {
			String userDirectoryString = System.getProperty("user.home") + "\\documents";
			userDirectory = new File(userDirectoryString);
		}

		if (!userDirectory.canRead()) {
			userDirectory = new File("c:/");
		}

		dc.setInitialDirectory(userDirectory);
		chosenFile = dc.showDialog(stage);

		if (chosenFile != null) {
			chosenFilePath = chosenFile.getAbsolutePath();
			this.datasetsPathText.setText(chosenFilePath);
		}
	}

	@FXML
	public void chooseModelPath(ActionEvent event) {
		File chosenFile;
		String chosenFilePath;
		File userDirectory = null;

		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

		dc = new DirectoryChooser();
		dc.setTitle("Choose the model root folder");

		String formerPath = modelPathText.getText();
		if(formerPath != null){
			userDirectory = new File(formerPath).getParentFile();
		}

		if(userDirectory == null || !userDirectory.canRead()) {
			String userDirectoryString = System.getProperty("user.home") + "\\documents";
			userDirectory = new File(userDirectoryString);
		}

		if (!userDirectory.canRead()) {
			userDirectory = new File("c:/");
		}

		dc.setInitialDirectory(userDirectory);
		chosenFile = dc.showDialog(stage);

		if (chosenFile != null) {
			chosenFilePath = chosenFile.getAbsolutePath();
			this.modelPathText.setText(chosenFilePath);
		}
	}

	@FXML
	public void done(ActionEvent event) throws IOException {
		String datasetsPath = this.datasetsPathText.getText();
		String modelPath = this.modelPathText.getText();
		if(!new File(datasetsPath).exists()){
			displayError("Non-valid path", "Please, enter a valid datasets folder path");
			return;
		}
		if(!new File(modelPath).exists()){
			displayError("Non-valid path", "Please, enter a valid model folder path");
			return;
		}
		Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);
		pref.put("datasetsPath", datasetsPath);
		pref.put("modelPath", modelPath);

		Parent modelCreationParent = FXMLLoader.load(getClass().getResource("/views/TabPanel.fxml"));
		Scene modelCreationScene = new Scene(modelCreationParent);

		Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
		window.setTitle("TAPON modelling tool");

		window.setScene(modelCreationScene);
		window.show();

	}

	private void displayError(String errorHeader, String errorContext){
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(errorHeader);
		alert.setContentText(errorContext);
		alert.showAndWait();
	}

}
