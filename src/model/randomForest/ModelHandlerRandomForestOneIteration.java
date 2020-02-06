package model.randomForest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.ClassesConfiguration;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import utils.DatasetReader;

public class ModelHandlerRandomForestOneIteration {
	//Constructors---------------------------------------------------
	public ModelHandlerRandomForestOneIteration() {
		this.sparkHandler = new SparkHandlerRandomForest();
		this.folderCount = 0;
		this.useProbabilities = true;
	}
	
	//Properties-----------------------------------------------------
	private Set<FeaturesGroup> slotFeaturesGroups;
	private Set<FeaturesGroup> featurableFeaturesGroups;
	private Set<FeaturesGroup> hintsSlotFeaturesGroups;
	private Set<FeaturesGroup> hintsFeaturableFeaturesGroups;
	private String tablesRootFolder;
	private String classifiersRootFolder;
	private boolean useProbabilities;
	
	public Set<FeaturesGroup> getSlotFeaturesGroups() {
		return slotFeaturesGroups;
	}

	public void setSlotFeaturesGroups(Set<FeaturesGroup> slotFeaturesGroups) {
		assert slotFeaturesGroups != null;
	
		this.slotFeaturesGroups = slotFeaturesGroups;
	}

	public Set<FeaturesGroup> getFeaturableFeaturesGroups() {
		return featurableFeaturesGroups;
	}

	public void setFeaturableFeaturesGroups(Set<FeaturesGroup> featurableFeaturesGroups) {
		assert featurableFeaturesGroups != null;
	
		this.featurableFeaturesGroups = featurableFeaturesGroups;
	}

	public Set<FeaturesGroup> getHintsSlotFeaturesGroups() {
		return hintsSlotFeaturesGroups;
	}

	public void setHintsSlotFeaturesGroups(Set<FeaturesGroup> hintsSlotFeaturesGroups) {
		assert hintsSlotFeaturesGroups != null;
	
		this.hintsSlotFeaturesGroups = hintsSlotFeaturesGroups;
	}

	public Set<FeaturesGroup> getHintsFeaturableFeaturesGroups() {
		return hintsFeaturableFeaturesGroups;
	}

	public void setHintsFeaturableFeaturesGroups(Set<FeaturesGroup> hintsFeaturableFeaturesGroups) {
		assert hintsFeaturableFeaturesGroups != null;
	
		this.hintsFeaturableFeaturesGroups = hintsFeaturableFeaturesGroups;
	}

	public String getTablesRootFolder() {
		return tablesRootFolder;
	}

	public void setTablesRootFolder(String tablesRootFolder) {
		assert tablesRootFolder != null;
	
		this.tablesRootFolder = tablesRootFolder;
	}

	public String getClassifiersRootFolder() {
		return classifiersRootFolder;
	}

	public void setClassifiersRootFolder(String classifiersRootFolder) {
		assert classifiersRootFolder != null;
	
		this.classifiersRootFolder = classifiersRootFolder;
	}
	
	public void loadClassifiers(boolean hints) {
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.loadClassifiers(hints);
	}

	//Internal state-------------------------------------------------
	private SparkHandlerRandomForest sparkHandler;
	private DatasetFeaturesCalculator firstDatasetFeaturesCalculator;
	private DatasetFeaturesCalculator secondDatasetFeaturesCalculator;
	private DatasetFeaturesCalculator secondFeaturesCalculator;
	private Integer folderCount;
	
	//Interface methods----------------------------------------------
	
	public void resetFolderCount(){
		this.folderCount = 0;
	}
	
	public void saveResults(Dataset dataset, String rootPath) throws IOException {
		assert dataset != null;
		assert rootPath != null;
		
		JSONObject trueLabels;
		JSONObject inferedLabels;
		JSONObject bothLabels;
		JSONArray inferedArray;
		JSONArray trueArray;
		JSONArray bothArray;
		FileWriter fileWriter;
		String filePath;
		Gson gson;
		JsonParser jsonParser;
		JsonWriter jsonWriter;
		File file;
		
		trueLabels = new JSONObject();
		inferedLabels = new JSONObject();
		bothLabels = new JSONObject();
		inferedArray = new JSONArray();
		trueArray = new JSONArray();
		bothArray = new JSONArray();
		
		for (Slot slot : dataset.getSlots()) {
			saveResults(slot, trueArray, inferedArray, bothArray);
		}
		trueLabels.put("children", trueArray);
		inferedLabels.put("children", inferedArray);
		bothLabels.put("children", bothArray);
		
		file = new File(String.format("%s/%s", rootPath, folderCount));
		file.mkdirs();
		gson = new GsonBuilder().setPrettyPrinting().create();
		filePath = String.format("%s/%s/true.json", rootPath, folderCount);
		fileWriter = new FileWriter(filePath);
		jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		jsonParser = new JsonParser();
		gson.toJson(jsonParser.parse(trueLabels.toJSONString()), jsonWriter);
		jsonWriter.close();
		filePath = String.format("%s/%s/infered.json", rootPath, folderCount);
		fileWriter = new FileWriter(filePath);
		jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		jsonParser = new JsonParser();
		gson.toJson(jsonParser.parse(inferedLabels.toJSONString()), jsonWriter);
		jsonWriter.close();
		filePath = String.format("%s/%s/both.json", rootPath, folderCount);
		fileWriter = new FileWriter(filePath);
		jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		jsonParser = new JsonParser();
		gson.toJson(jsonParser.parse(bothLabels.toJSONString()), jsonWriter);
		jsonWriter.close();
		
		folderCount++;
	}
	
	private void saveResults(Slot slot, JSONArray trueLabels, JSONArray inferedLabels, JSONArray bothLabels) {
		assert slot != null;
		assert trueLabels != null;
		assert inferedLabels != null;
		
		JSONObject trueChild;
		JSONObject inferedChild;
		JSONObject bothChild;
		JSONArray inferedArray;
		JSONArray trueArray;
		JSONArray bothArray;
		List<Slot> children;
		
		trueChild = new JSONObject();
		inferedChild = new JSONObject();
		bothChild = new JSONObject();
		
		if (slot instanceof Record) {
			inferedArray = new JSONArray();
			trueArray = new JSONArray();
			bothArray = new JSONArray();
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				saveResults(child, trueArray, inferedArray, bothArray);
			}
			trueChild.put("type", "record");
			inferedChild.put("type", "record");
			
			trueChild.put("children", trueArray);
			inferedChild.put("children", inferedArray);
			bothChild.put("children", bothArray);
		} else {
			trueChild.put("type", "attribute");
			inferedChild.put("type", "attribute");
		}
		
		trueChild.put("class", slot.getSlotClass());
		inferedChild.put("class", slot.getHint());
		bothChild.put("trueClass", slot.getSlotClass());
		bothChild.put("inferedClass", slot.getHint());
		
		trueChild.put("id", slot.getName());
		inferedChild.put("id", slot.getName());
		bothChild.put("id", slot.getName());
		
		trueLabels.add(trueChild);
		inferedLabels.add(inferedChild);
		bothLabels.add(bothChild);
	}
	
	public void saveResultsWithoutArrays(Dataset dataset, String rootPath) throws IOException {
		assert dataset != null;
		assert rootPath != null;
		
		JSONObject trueLabels;
		JSONObject inferedLabels;
		FileWriter fileWriter;
		String filePath;
		Gson gson;
		JsonParser jsonParser;
		JsonWriter jsonWriter;
		File file;
		
		trueLabels = new JSONObject();
		inferedLabels = new JSONObject();
		
		for (Slot slot : dataset.getSlots()) {
			saveResultsWithoutArrays(slot, trueLabels, inferedLabels);
		}
		
		file = new File(String.format("%s/%s", rootPath, folderCount));
		file.mkdirs();
		gson = new GsonBuilder().setPrettyPrinting().create();
		filePath = String.format("%s/%s/true.json", rootPath, folderCount);
		fileWriter = new FileWriter(filePath);
		jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		jsonParser = new JsonParser();
		gson.toJson(jsonParser.parse(trueLabels.toJSONString()), jsonWriter);
		jsonWriter.close();
		filePath = String.format("%s/%s/infered.json", rootPath, folderCount);
		fileWriter = new FileWriter(filePath);
		jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		jsonParser = new JsonParser();
		gson.toJson(jsonParser.parse(inferedLabels.toJSONString()), jsonWriter);
		jsonWriter.close();
		
		folderCount++;
	}
	
	private void saveResultsWithoutArrays(Slot slot, JSONObject trueLabels, JSONObject inferedLabels) {
		assert slot != null;
		assert trueLabels != null;
		assert inferedLabels != null;
		
		JSONObject trueChild;
		JSONObject inferedChild;
		List<Slot> children;
		
		trueChild = new JSONObject();
		inferedChild = new JSONObject();
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				saveResultsWithoutArrays(child, trueChild, inferedChild);
			}
			trueChild.put("type", "record");
			inferedChild.put("type", "record");
		} else {
			trueChild.put("type", "attribute");
			inferedChild.put("type", "attribute");
		}
		
		trueChild.put("class", slot.getSlotClass());
		inferedChild.put("class", slot.getHint());
		
		trueLabels.put(slot.getName(), trueChild);
		inferedLabels.put(slot.getName(), inferedChild);
	}
	
	public void trainModel(List<Dataset> trainingDatasets, Map<String, String> params) throws Exception {
		assert this.tablesRootFolder != null;
		assert this.classifiersRootFolder != null;
		assert this.featurableFeaturesGroups != null;
		assert this.slotFeaturesGroups != null;
		assert this.hintsFeaturableFeaturesGroups != null;
		assert this.hintsSlotFeaturesGroups != null;
		
		File tablesRootFolderFile;
		File classifiersRootFolderFile;
		DatasetReader datasetReader;
		
		sparkHandler.createNewContext();
		
		tablesRootFolderFile = new File(tablesRootFolder);
		classifiersRootFolderFile = new File(classifiersRootFolder);
		firstDatasetFeaturesCalculator = new DatasetFeaturesCalculator();
		firstDatasetFeaturesCalculator.setSlotFeaturesGroups(slotFeaturesGroups);
		firstDatasetFeaturesCalculator.setFeaturableFeaturesGroups(featurableFeaturesGroups);
		firstDatasetFeaturesCalculator.setIndexPath(String.format("%s/index", tablesRootFolder));

		datasetReader = new DatasetReader();
		if(tablesRootFolderFile.exists()) {
			FileUtils.cleanDirectory(tablesRootFolderFile);
		}
		if(classifiersRootFolderFile.exists()){
			FileUtils.cleanDirectory(classifiersRootFolderFile);
		}

		tablesRootFolderFile.mkdirs();
		classifiersRootFolderFile.mkdirs();
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		setParams(params);
		try {
			firstDatasetFeaturesCalculator.initializeClasses(trainingDatasets);
			firstDatasetFeaturesCalculator.getClassesConfiguration().store(String.format("%s/classes.json", tablesRootFolder));
			sparkHandler.setClassesConfiguration(firstDatasetFeaturesCalculator.getClassesConfiguration());
			firstDatasetFeaturesCalculator.initialize(false);
			firstDatasetFeaturesCalculator.store(String.format("%s/firstFeaturesCalculator", tablesRootFolder));
			for (Dataset dataset : trainingDatasets) {
				firstDatasetFeaturesCalculator.setDataset(dataset);
				firstDatasetFeaturesCalculator.run(String.format("%s/trainingTablesNoHint", tablesRootFolder), true);
				System.out.println(String.format("Added dataset %s", dataset));
			}
			firstDatasetFeaturesCalculator.closeTablesBuilder();
		} catch(Exception e) {
			System.out.println("There was a problem while trying to create the first feature tables: ");
			e.printStackTrace();
			throw e;
		}
		
		//First classifiers creation
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		sparkHandler.setNumberOfFeatures(firstDatasetFeaturesCalculator.getNumberOfFeatures());
		
		try{
			sparkHandler.createBinaryClassifiers(false);
			sparkHandler.createMulticlassClassifierCsv(useProbabilities, false);
			sparkHandler.trainMulticlassClassifier(false);
			sparkHandler.loadClassifiers(false);
		} catch(Exception e) {
			System.out.println("There was a problem while trying to create the classifiers: ");
			e.printStackTrace();
			throw e;
		}
		
		sparkHandler.closeContext();
		System.out.println("Model creation has finished!");
	}
	
	public void loadFeaturesCalculators() throws IOException, ClassNotFoundException {
		assert firstDatasetFeaturesCalculator != null;
		assert secondDatasetFeaturesCalculator != null;
		
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;
		
		fileInputStream = new FileInputStream(String.format("resources/%s/firstFeaturesCalculator", tablesRootFolder));
		objectInputStream = new ObjectInputStream(fileInputStream);
		firstDatasetFeaturesCalculator = (DatasetFeaturesCalculator) objectInputStream.readObject();
		objectInputStream.close();
		
		fileInputStream = new FileInputStream(String.format("resources/%s/secondFeaturesCalculator", tablesRootFolder));
		objectInputStream = new ObjectInputStream(fileInputStream);
		secondDatasetFeaturesCalculator = (DatasetFeaturesCalculator) objectInputStream.readObject();
		objectInputStream.close();
	}
	
	
	public void createNewContext(){
		sparkHandler.createNewContext();
	}
	
	public void closeContext(){
		sparkHandler.closeContext();
	}
	
	public void refineHintsUnlabelledDataset(Dataset dataset) throws IOException, ParseException {
		assert firstDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;
		
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		
		firstDatasetFeaturesCalculator.setDataset(dataset);
		firstDatasetFeaturesCalculator.run("none", false);
		refineHints(dataset);
		
	}
	

	//Ancillary methods----------------------------------------------
	
	private void refineHints(Dataset dataset) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			refineHints(child);
		}
	}
	
	private void refineHints(Slot slot) {
		assert slot != null;
		List<Slot> children;
		String slotClass;
		LinkedHashMap<String, Double> ranking;
		Pair<String, LinkedHashMap<String, Double>> prediction;
		
		try {
			prediction = sparkHandler.classify(slot, useProbabilities, false);
			slotClass = prediction.getLeft();
			ranking = prediction.getRight();
			//System.out.println(String.format("Predicted class %s", slotClass));
			System.out.println(String.format("True class: %s ==== infered class: %s ==== ranking: %s", slot.getSlotClass(), slotClass, ranking));
			slot.setHint(slotClass);
			slot.setHintsRanking(ranking);
		} catch (Exception e) {
			System.out.println("There has been a problem while trying to classify a slot: ");
			e.printStackTrace();
		}
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				refineHints(child);
			}
		}
	}
	
	private Map<String, String> getHintsMap(Dataset dataset) {
		assert dataset != null;
		List<Slot> children;
		
		Map<String, String> result;
		
		result = new HashMap<String, String>();
		children = dataset.getSlots();
		for (Slot child : children) {
			result.put(child.getName(), child.getHint());
			getHintsMap(child, result);
		}
		
		return result;
	}
	
	private void getHintsMap(Slot slot, Map<String, String> hintsMap) {
		assert slot != null;
		assert hintsMap != null;
		List<Slot> children;
		
		hintsMap.put(slot.getName(), slot.getHint());
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				getHintsMap(child, hintsMap);
			}
		}
	}

	private void setParams(Map<String, String> params) {
		assert sparkHandler != null;
		assert params != null;

		sparkHandler.setParam("numTrees", Integer.valueOf(params.getOrDefault("numTrees", "20")));
		sparkHandler.setParam("maxBins", Integer.valueOf(params.getOrDefault("maxBins", "32")));
		sparkHandler.setParam("maxDepth", Integer.valueOf(params.getOrDefault("maxDepth", "5")));
	}
}
