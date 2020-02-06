package javaFX.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.FeaturesGroup;
import featuresCalculation.featureGroups.attribute.*;
import featuresCalculation.featureGroups.featurable.Hint_MinimumTreeDistanceGroup;
import featuresCalculation.featureGroups.record.Hint_DensityOfSlotGroup;
import featuresCalculation.featureGroups.record.Hint_NumberOfSlotsRecordGroup;
import featuresCalculation.featureGroups.record.NumberOfChildrenGroup;
import featuresCalculation.featureGroups.slot.Hint_DensityOfBrothersGroup;
import featuresCalculation.featureGroups.slot.NodeDepthGroup;
import featuresCalculation.featureGroups.slot.NumberOfBrothersGroup;
import javaFX.EntryPoint;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.randomForest.ModelHandlerRandomForest;
import org.apache.parquet.Strings;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.DatasetReader;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class ModelCreationController implements Initializable {

	@FXML
	private Button createModelButton;

	@FXML
	private TextArea consoleArea;

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
	private TableView<DomainSelection> tableDomains;
	@FXML
	private TableColumn<DomainSelection, String> domainNames;
	@FXML
	private TableColumn<DomainSelection, Boolean> domainSelected;

	@FXML
	private TableView<FeatureSelection> tableFeatures;
	@FXML
	private TableColumn<FeatureSelection, String> featureNames;
	@FXML
	private TableColumn<FeatureSelection, Boolean> featureSelected;
	@FXML
	private TableColumn<FeatureSelection, String> featureTypes;

	@FXML
	private TableView<FeatureSelection> tableFeaturesHB;
	@FXML
	private TableColumn<FeatureSelection, String> featureNamesHB;
	@FXML
	private TableColumn<FeatureSelection, Boolean> featureSelectedHB;
	@FXML
	private TableColumn<FeatureSelection, String> featureTypesHB;

	@FXML
	private Slider numTrees;
	@FXML
	private Slider maxBins;
	@FXML
	private Slider maxDepth;

	@FXML
	private VBox domainDetails;

	private Integer numAttributes;
	private Integer numNumeric;
	private Integer numRecords;
	private Integer numAttributeChildren;
	private Integer numRecordChildren;
	private Integer depth;
	private Integer sumDepth;
	private Map<String, Integer> attributeClassesCount;
	private Map<String, Integer> recordClassesCount;
	private Map<Integer, Integer> depthCount;



	public void viewDetails(DomainSelection ds) throws IOException, ParseException {
		Label label;
		File detailsFile;
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		JsonParser parser;
		JsonElement element;
		Gson gson;

		detailsFile = new File(ds.getFolderPath(), "metadata.json");
		if(detailsFile.exists()){
			domainDetails.getChildren().clear();
			fileReader = new FileReader(detailsFile);
			jsonParser = new JSONParser();
			jsonObject = (JSONObject)jsonParser.parse(fileReader);
			System.out.println(jsonObject.toJSONString());
			jsonObject.forEach((key, value) -> {
				Label keyLabel = new Label(String.format("%s: ", key));
				if(!(value instanceof JSONObject)){
					Label valueLabel = new Label(value.toString());
					HBox hBox = new HBox();
					hBox.getChildren().add(keyLabel);
					hBox.getChildren().add(valueLabel);
					hBox.setPadding(new Insets(5, 10, 5, 10));
					hBox.getStyleClass().add("details-element");
					domainDetails.getChildren().add(hBox);
				} else {
					VBox vBox = new VBox();
					vBox.getStyleClass().add("details-element");
					vBox.setSpacing(5.0);
					vBox.setPadding(new Insets(5,5,5,5));
					vBox.getChildren().add(keyLabel);
					VBox vBox2 = new VBox();
					vBox2.setPadding(new Insets(0, 0, 0, 15));
					vBox2.setSpacing(5.0);
					((JSONObject)value).forEach((key2, value2)->{
						Label keyLabel2 = new Label(String.format("%s: ", key2));
						Label valueLabel2 = new Label(value2.toString());
						HBox hBox2 = new HBox();
						hBox2.getChildren().add(keyLabel2);
						hBox2.getChildren().add(valueLabel2);
						hBox2.setPadding(new Insets(5, 10, 5, 10));
						hBox2.getStyleClass().add("details-element");
						hBox2.getStyleClass().add("details-element-2");
						vBox2.getChildren().add(hBox2);
					});
					vBox.getChildren().add(vBox2);
					domainDetails.getChildren().add(vBox);
				}
			});
			fileReader.close();

		} else {
			Label ndLabel = new Label("There are no details about this dataset. Try using the analyze dataset option");
			ndLabel.setPadding(new Insets(10, 10, 10, 10));
			ndLabel.setLayoutX(10.0);
			ndLabel.setLayoutY(10.0);
			domainDetails.getChildren().clear();
			domainDetails.getChildren().add(ndLabel);
		}


	}

	public void analyzeDomain(DomainSelection ds) throws IOException, ParseException {
		File detailsFile;
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		File[] foldFolders;
		List<Dataset> datasets;
		DatasetReader datasetReader;
		List<Slot> children;
		Integer numDatasets;
		JSONObject classesCountJson;

		detailsFile = new File(ds.getFolderPath(), "metadata.json");
		if(detailsFile.exists()){
			fileReader = new FileReader(detailsFile);
			jsonParser = new JSONParser();
			jsonObject = (JSONObject)jsonParser.parse(fileReader);
			fileReader.close();
		} else{
			jsonObject = new JSONObject();
		}

		foldFolders = new File(ds.getFolderPath()).listFiles(File::isDirectory);
		datasetReader = new DatasetReader();
		numDatasets = 0;
		numAttributes = 0;
		numRecords = 0;
		numNumeric = 0;
		numAttributeChildren = 0;
		numRecordChildren = 0;
		sumDepth = 0;
		attributeClassesCount = new HashMap<>();
		recordClassesCount = new HashMap<>();
		depthCount = new HashMap<>();
		for (File foldFolder : foldFolders) {
			datasets = new ArrayList<>();
			datasetReader.addDataset(foldFolder.getPath(), 1.0, datasets);
			numDatasets += datasets.size();
			for (Dataset dataset: datasets) {
				depth = 0;
				children = dataset.getSlots();
				for (Slot child : children) {
					process(child, 1);
				}
				sumDepth += depth;
				depthCount.put(depth, depthCount.getOrDefault(depth, 0));
			}
		}

		jsonObject.put("Number of datasets", numDatasets);
		jsonObject.put("Number of attributes", numAttributes);
		jsonObject.put("Number of records", numRecords);
		jsonObject.put("Fraction of attributes that have numerical values", ((double)numNumeric)/numAttributes);
		jsonObject.put("Average number of children", ((double)(numAttributeChildren+numRecordChildren))/numDatasets);
		jsonObject.put("Average dataset depth", ((double)sumDepth)/numDatasets);
		jsonObject.put("Number of classes", attributeClassesCount.size()+recordClassesCount.size());
		classesCountJson = new JSONObject();
		for (String slotClass : attributeClassesCount.keySet()) {
			classesCountJson.put(slotClass, attributeClassesCount.get(slotClass));
		}
		jsonObject.put("Attribute classes count", classesCountJson);

		classesCountJson = new JSONObject();
		for (String slotClass : recordClassesCount.keySet()) {
			classesCountJson.put(slotClass, recordClassesCount.get(slotClass));
		}
		jsonObject.put("Record classes count", classesCountJson);

		String realName = detailsFile.getPath();
		File newFile = new File(detailsFile.getPath()+".tmp");
		FileWriter writer = new FileWriter(newFile);
		writer.write(jsonObject.toJSONString());
		writer.flush();
		writer.close();
		detailsFile.delete();
		writer.close();
		newFile.renameTo(detailsFile);
	}

	private void process(Slot slot, int currentDepth) {
		assert slot != null;

		if(currentDepth>depth){
			depth=currentDepth;
		}

		List<Slot> children;
		if(slot instanceof Attribute){
			numAttributes++;
			if(((Attribute)slot).getNumericValue()!=null){
				numNumeric++;
			}
			attributeClassesCount.put(slot.getSlotClass(), attributeClassesCount.getOrDefault(slot.getSlotClass(), 0)+1);
		}else{
			numRecords++;
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				if(child instanceof Attribute){
					numAttributeChildren++;
				}else{
					numRecordChildren++;
				}
				process(child, currentDepth+1);
			}
			recordClassesCount.put(slot.getSlotClass(), recordClassesCount.getOrDefault(slot.getSlotClass(), 0)+1);
		}
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

		// Domain table initialization
		tableDomains.setRowFactory(new Callback<TableView<DomainSelection>, TableRow<DomainSelection>>() {
			@Override
			public TableRow<DomainSelection> call(TableView<DomainSelection> param) {
				final TableRow<DomainSelection> row = new TableRow<>();
				ContextMenu menu;
				MenuItem menuItem;
				menu = new ContextMenu();
				menuItem = new MenuItem("View details");
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							viewDetails(row.getItem());
						} catch (Exception e) {
							displayError("Domain details visualization error", "There was an error while trying to load the domain details");
						}
					}
				});
				menu.getItems().add(menuItem);

				menuItem = new MenuItem("Analyze domain");
				menuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						try {
							analyzeDomain(row.getItem());
						} catch (Exception e) {
							displayError("Domain analysis error", "There was an error while trying to analyze the domain ");
							e.printStackTrace();
						}
					}
				});
				menu.getItems().add(menuItem);
				row.contextMenuProperty().bind(
						Bindings.when(row.emptyProperty())
								.then((ContextMenu)null)
								.otherwise(menu)
				);
				return row;
			}
		});
		tableDomains.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DomainSelection>() {
			@Override
			public void changed(ObservableValue<? extends DomainSelection> observable, DomainSelection oldValue, DomainSelection newValue) {
				if(newValue != null) {
					try {
						viewDetails(newValue);
					} catch (Exception e) {
						displayError("Domain details visualization error", "There was an error while trying to load the domain details");
					}
				}
			}
		});
		domainNames.setCellValueFactory(new PropertyValueFactory<DomainSelection, String>("name"));
		domainSelected.setCellFactory(CheckBoxTableCell.forTableColumn(domainSelected));
		domainSelected.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<DomainSelection, Boolean>, ObservableValue<Boolean>>() {

					@Override
					public ObservableValue<Boolean> call(CellDataFeatures<DomainSelection, Boolean> param) {
						return param.getValue().getSelected();
					}
				});
		Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);
		String datasetsPath = pref.get("datasetsPath", null);

		datasetsFolder = new File(datasetsPath);
		domainFolders = datasetsFolder.listFiles(File::isDirectory);
		ContextMenu menu;
		MenuItem menuItem;
		for (File file : domainFolders) {
			ds = new DomainSelection();
			ds.setName(file.getName());
			ds.setSelected(false);
			ds.setFolderPath(file.getPath());
			tableDomains.getItems().add(ds);
		}
		tableDomains.setEditable(true);
		domainSelected.setEditable(true);

		// Features table initialization
		populateFeatures();

		featureNames.setCellValueFactory(new PropertyValueFactory<FeatureSelection, String>("name"));
		featureTypes.setCellValueFactory(new PropertyValueFactory<FeatureSelection, String>("type"));
		featureSelected.setCellFactory(CheckBoxTableCell.forTableColumn(featureSelected));
		featureSelected.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<FeatureSelection, Boolean>, ObservableValue<Boolean>>() {

					@Override
					public ObservableValue<Boolean> call(CellDataFeatures<FeatureSelection, Boolean> param) {
						return param.getValue().getSelected();
					}
				});

		tableFeatures.setEditable(true);
		featureSelected.setEditable(true);

		// Hint-based

		featureNamesHB.setCellValueFactory(new PropertyValueFactory<FeatureSelection, String>("name"));
		featureTypesHB.setCellValueFactory(new PropertyValueFactory<FeatureSelection, String>("type"));
		featureSelectedHB.setCellFactory(CheckBoxTableCell.forTableColumn(featureSelected));
		featureSelectedHB.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<FeatureSelection, Boolean>, ObservableValue<Boolean>>() {

					@Override
					public ObservableValue<Boolean> call(CellDataFeatures<FeatureSelection, Boolean> param) {
						return param.getValue().getSelected();
					}
				});

		tableFeatures.setEditable(true);
		featureSelected.setEditable(true);
		tableFeaturesHB.setEditable(true);
		featureSelectedHB.setEditable(true);
	}

	private void populateFeatures() {
		ObservableList<FeatureSelection> fl = tableFeatures.getItems();
		ObservableList<FeatureSelection> flhb = tableFeaturesHB.getItems();

		fl.add(new FeatureSelection("Number of pattern occurrences", "A", 1));
		fl.add(new FeatureSelection("Token types density", "A", 2));
		fl.add(new FeatureSelection("Character types density", "A", 3));
		fl.add(new FeatureSelection("Avg. common prefix/suffix length per class", "A", 4));
		fl.add(new FeatureSelection("Avg. edit distance per class", "A", 5));
		fl.add(new FeatureSelection("Lucene document score per class", "A", 6));
		fl.add(new FeatureSelection("Numeric value", "A", 7));
		fl.add(new FeatureSelection("Tree depth", "A & R", 8));
		fl.add(new FeatureSelection("Number of siblings", "A & R", 9));
		fl.add(new FeatureSelection("Number of children", "R", 10));

		flhb.add(new FeatureSelection("Minimum tree distance to each class", "A & R", 11));
		flhb.add(new FeatureSelection("Density of siblings per class", "A & R", 12));
		flhb.add(new FeatureSelection("Number of children per class", "R", 13));
		flhb.add(new FeatureSelection("Density of children per class", "R", 14));
	}

	@FXML
	public void createModel() throws Exception {
		createModelButton.setDisable(true);
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				createModelRunnable();
				return null;
			}
			@Override
			protected void succeeded(){
				createModelButton.setDisable(false);
			}

			@Override
			protected void failed(){
				createModelButton.setDisable(false);
			}

		};
		new Thread(task).start();
	}

	public void createModelRunnable() throws Exception {
		Set<FeaturesGroup> slotFeaturesGroups;
		Set<FeaturesGroup> featurableFeaturesGroups;
		Set<FeaturesGroup> hintSlotFeaturesGroups;
		Set<FeaturesGroup> hintFeaturableFeaturesGroups;

		// MODEL APPLICATION
		List<Dataset> trainingDatasets;
		DatasetReader datasetReader;
		String modelRoot;
		String datasetsRoot;
		String datasetsPath;
		ModelHandlerRandomForest modelHandler;

		//CHECKS
		boolean selected = foldsMap.values().stream().anyMatch(f -> f.isSelected());
		if(! selected){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					displayError("No folds selected", "Plese, select at least one testing fold.");
				}
			});
			return;
		}

		selected = tableDomains.getItems().stream().anyMatch(f -> f.isSelected());
		if(! selected){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					displayError("No domains selected", "Plese, select at least one testing domain.");
				}
			});
			return;
		}

		Map<String, String> params = new HashMap<>();
		Integer numTrees = (int)Math.round(this.numTrees.getValue());
		if(numTrees <= 0){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					displayError("Invalid number of trees", "The number of trees must be above 0");
				}
			});
			return;
		}
		params.put( "numTrees", String.valueOf(numTrees));
		Integer maxBins = (int)Math.round(this.maxBins.getValue());
		if(maxBins <= 0){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					displayError("Invalid maxBins", "maxBins must be above 0");
				}
			});
			return;
		}
		params.put("maxBins", String.valueOf(maxBins));
		Integer maxDepth = (int)Math.round(this.maxDepth.getValue());
		if(maxDepth <= 0){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					displayError("Invalid maximum depth", "The maximum tree depth must be above 0");
				}
			});
			return;
		}
		//END OF CHECKS

		params.put("maxDepth", String.valueOf(maxDepth));

		Preferences pref = Preferences.userNodeForPackage(EntryPoint.class);

		slotFeaturesGroups = new HashSet<>();
		hintSlotFeaturesGroups = new HashSet<>();
		featurableFeaturesGroups = new HashSet<>();
		hintFeaturableFeaturesGroups = new HashSet<>();

		//Adding the feature groups that will be used
		for (FeatureSelection fs :
				tableFeatures.getItems()) {
			if(fs.isSelected()) {
				addFeatureGroup(slotFeaturesGroups, hintSlotFeaturesGroups, featurableFeaturesGroups, hintFeaturableFeaturesGroups, fs);
			}
		}

		for (FeatureSelection fs :
				tableFeaturesHB.getItems()) {
			if(fs.isSelected()) {
				addFeatureGroup(slotFeaturesGroups, hintSlotFeaturesGroups, featurableFeaturesGroups, hintFeaturableFeaturesGroups, fs);
			}
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Parent root;
				try{
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(getClass().getResource("/views/ModelProgress.fxml"));
					root = loader.load();
					Scene scene = new Scene(root);
					ModelProgressController controller = loader.getController();
					Stage stage = new Stage();
					stage.setTitle("Model creation");
					stage.getIcons().setAll(new Image("/icon.png"));
					stage.setScene(scene);
					stage.show();
					controller.createModel(foldsMap, tableDomains, params, slotFeaturesGroups, hintSlotFeaturesGroups, featurableFeaturesGroups, hintFeaturableFeaturesGroups);
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		});

	}

	private void addFeatureGroup(Set<FeaturesGroup> slotFeaturesGroups,
			Set<FeaturesGroup> hintSlotFeaturesGroups, Set<FeaturesGroup> featurableFeaturesGroups,
			Set<FeaturesGroup> hintFeaturableFeaturesGroups, FeatureSelection fs) {
		switch (fs.getId()) {
			case 1:
				slotFeaturesGroups.add(new NumberOfOccurrencesGroup());
				break;
			case 2:
				slotFeaturesGroups.add(new TokenDensityGroup());
				break;
			case 3:
				slotFeaturesGroups.add(new CharacterDensityGroup());
				break;
			case 4:
				slotFeaturesGroups.add(new CommonPrefixSuffixLengthGroup());
				break;
			case 5:
				slotFeaturesGroups.add(new EditDistanceGroup());
				break;
			case 6:
				slotFeaturesGroups.add(new TyperScoreGroup());
				break;
			case 7:
				slotFeaturesGroups.add(new NumericValueGroup());
				break;
			case 8:
				slotFeaturesGroups.add(new NodeDepthGroup());
				break;
			case 9:
				slotFeaturesGroups.add(new NumberOfBrothersGroup());
				break;
			case 10:
				slotFeaturesGroups.add(new NumberOfChildrenGroup());
				break;
			case 11:
				hintFeaturableFeaturesGroups.add(new Hint_MinimumTreeDistanceGroup());
				break;
			case 12:
				hintSlotFeaturesGroups.add(new Hint_DensityOfBrothersGroup());
				break;
			case 13:
				hintSlotFeaturesGroups.add(new Hint_NumberOfSlotsRecordGroup());
				break;
			case 14:
				hintSlotFeaturesGroups.add(new Hint_DensityOfSlotGroup());

		default:
			break;
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
