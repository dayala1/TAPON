package model.randomForest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import utils.ClockMonitor;
import utils.DatasetReader;

public class ModelHandlerRandomForestNoDifferences {
	//Constructors---------------------------------------------------
	public ModelHandlerRandomForestNoDifferences() {
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
	private Long firstModelTrainTime;
	private Long secondModelTrainTime;
	
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
	
	public Long getFirstModelTrainTime() {
		return firstModelTrainTime;
	}

	public void setFirstModelTrainTime(Long firstModelTrainTime) {
		assert firstModelTrainTime != null;
	
		this.firstModelTrainTime = firstModelTrainTime;
	}

	public Long getSecondModelTrainTime() {
		return secondModelTrainTime;
	}

	public void setSecondModelTrainTime(Long secondModelTrainTime) {
		assert secondModelTrainTime != null;
	
		this.secondModelTrainTime = secondModelTrainTime;
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
	
	public void trainModel(Double fractionTesting, Collection<Dataset> trainingDatasets) throws Exception {
		assert fractionTesting != null;
		assert fractionTesting <= 1.0;
		assert this.tablesRootFolder != null;
		assert this.classifiersRootFolder != null;
		assert this.featurableFeaturesGroups != null;
		assert this.slotFeaturesGroups != null;
		assert this.hintsFeaturableFeaturesGroups != null;
		assert this.hintsSlotFeaturesGroups != null;
		
		File tablesRootFolderFile;
		File classifiersRootFolderFile;
		ClockMonitor clock;
		
		clock = new ClockMonitor();
		sparkHandler.createNewContext();
		
		tablesRootFolderFile = new File(tablesRootFolder);
		classifiersRootFolderFile = new File(classifiersRootFolder);
		firstDatasetFeaturesCalculator = new DatasetFeaturesCalculator();
		firstDatasetFeaturesCalculator.setSlotFeaturesGroups(slotFeaturesGroups);
		firstDatasetFeaturesCalculator.setFeaturableFeaturesGroups(featurableFeaturesGroups);
		firstDatasetFeaturesCalculator.setIndexPath(String.format("%s/index", tablesRootFolder));
		
		secondDatasetFeaturesCalculator = new DatasetFeaturesCalculator();
		secondDatasetFeaturesCalculator.setSlotFeaturesGroups(hintsSlotFeaturesGroups);
		secondDatasetFeaturesCalculator.setFeaturableFeaturesGroups(hintsFeaturableFeaturesGroups);
		secondDatasetFeaturesCalculator.setIndexPath(String.format("%s/index", tablesRootFolder));
		
		tablesRootFolderFile.mkdirs();
		classifiersRootFolderFile.mkdirs();
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		clock.start();
		try {
			firstDatasetFeaturesCalculator.initializeClasses(trainingDatasets);
			firstDatasetFeaturesCalculator.storeClasses(String.format("%s/classes.json", tablesRootFolder));
			secondDatasetFeaturesCalculator.loadClasses(String.format("%s/classes.json", tablesRootFolder));
			firstDatasetFeaturesCalculator.initialize(false);
			firstDatasetFeaturesCalculator.store(String.format("%s/firstFeaturesCalculator", tablesRootFolder));
			secondDatasetFeaturesCalculator.initialize(false);
			secondDatasetFeaturesCalculator.store(String.format("%s/secondFeaturesCalculator", tablesRootFolder));
			
			for (Dataset dataset : trainingDatasets) {
				setHintsToClass(dataset);
				firstDatasetFeaturesCalculator.setDataset(dataset);
				firstDatasetFeaturesCalculator.run(String.format("%s/trainingTablesHint", tablesRootFolder), false);
				secondDatasetFeaturesCalculator.setDataset(dataset);
				secondDatasetFeaturesCalculator.run(String.format("%s/trainingTablesHint", tablesRootFolder), true);
			}
		} catch(Exception e) {
			System.out.println("There was a problem while trying to create the feature tables: ");
			e.printStackTrace();
			throw e;
		}
		
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		
		//Classifiers creation
		sparkHandler.setNumberOfFeatures(firstDatasetFeaturesCalculator.getNumberOfFeatures()+secondDatasetFeaturesCalculator.getNumberOfFeatures());
		
		try{
			sparkHandler.createBinaryClassifiers(true);
			sparkHandler.createMulticlassClassifierCsv(useProbabilities, true);
			sparkHandler.trainMulticlassClassifier(true);
			sparkHandler.loadClassifiers(true);
		} catch(Exception e) {
			System.out.println("There was a problem while trying to create the classifiers: ");
			e.printStackTrace();
			throw e;
		}
		clock.stop();
		setFirstModelTrainTime(clock.getCPUTime());
		sparkHandler.closeContext();
		System.out.println("Model creation has finished!");
	}
	
	public void loadFeaturesCalculators() throws IOException, ClassNotFoundException {
		assert firstDatasetFeaturesCalculator != null;
		assert secondDatasetFeaturesCalculator != null;
		
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;
		
		fileInputStream = new FileInputStream(String.format("%s/firstFeaturesCalculator", tablesRootFolder));
		objectInputStream = new ObjectInputStream(fileInputStream);
		firstDatasetFeaturesCalculator = (DatasetFeaturesCalculator) objectInputStream.readObject();
		objectInputStream.close();
		
		fileInputStream = new FileInputStream(String.format("%s/secondFeaturesCalculator", tablesRootFolder));
		objectInputStream = new ObjectInputStream(fileInputStream);
		secondDatasetFeaturesCalculator = (DatasetFeaturesCalculator) objectInputStream.readObject();
		objectInputStream.close();
	}
	
	public void classifySlots(Dataset dataset, int numberOfIterations) throws IOException, ParseException {
		assert firstDatasetFeaturesCalculator != null;
		assert secondDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;
		
		sparkHandler.createNewContext();
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		
		firstDatasetFeaturesCalculator.setDataset(dataset);
		firstDatasetFeaturesCalculator.run("none", false);
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		secondDatasetFeaturesCalculator.setDataset(dataset);
		
		for (int i = 0; i < numberOfIterations; i++) {
			secondDatasetFeaturesCalculator.run("none", false);
			refineHints(dataset, true);
		}
		
		sparkHandler.closeContext();
	}
	
	public void createNewContext(){
		sparkHandler.createNewContext();
	}
	
	public void closeContext(){
		sparkHandler.closeContext();
	}
	
	public void runFirstFeaturesCalculator(Dataset dataset) throws IOException, ParseException {
		assert firstDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;
		
		firstDatasetFeaturesCalculator.setDataset(dataset);
		firstDatasetFeaturesCalculator.run("none", false);
	}
	
	public void refineHintsOnce(Dataset dataset) throws IOException, ParseException {
		assert secondDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;
		
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		
		secondDatasetFeaturesCalculator.setDataset(dataset);
		secondDatasetFeaturesCalculator.run("none", false);
		refineHints(dataset, true);
	}
	
	public void classifySlotsUntilConvergence(Dataset dataset, int hintsIterationsLimit) throws IOException, ParseException {
		assert firstDatasetFeaturesCalculator != null;
		assert secondDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;
		
		Map<String, String> hintsBefore;
		Map<String, String> hintsAfter;
		String hintBefore;
		String hintAfter;
		Integer iterationsCount;
		
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);
		
		firstDatasetFeaturesCalculator.setDataset(dataset);
		firstDatasetFeaturesCalculator.run("none", false);
		refineHints(dataset, false);

		secondDatasetFeaturesCalculator.setDataset(dataset);
		
		boolean convergence = false;
		iterationsCount = 0;
		while(!convergence) {
			hintsBefore = getHintsMap(dataset);
			secondDatasetFeaturesCalculator.run("none", false);
			refineHints(dataset, true);
			hintsAfter = getHintsMap(dataset);
			iterationsCount++;
			
			if(iterationsCount >= hintsIterationsLimit) {
				break;
			}
			
			convergence = true;
			
			for (String slotName : hintsBefore.keySet()) {
				hintBefore = hintsBefore.get(slotName);
				hintAfter = hintsAfter.get(slotName);
				if(!hintBefore.equals(hintAfter)) {
					convergence = false;
					break;
				}
			}
		}
		
	}
	

	//Ancillary methods----------------------------------------------
	
	private void setHintsToClass(Dataset dataset) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			setHintsToClass(child);
		}
	}
	
	private void setHintsToClass(Slot slot) {
		assert slot != null;
		List<Slot> children;
		
		slot.setHint(slot.getSlotClass());
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				setHintsToClass(child);
			}
		}
	}
	
	private void refineHints(Dataset dataset, boolean hints) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			refineHints(child, hints);
		}
	}
	
	private void refineHints(Slot slot, boolean hints) {
		assert slot != null;
		List<Slot> children;
		String slotClass;
		LinkedHashMap<String, Double> ranking;
		Pair<String, LinkedHashMap<String, Double>> prediction;
		
		try {
			prediction = sparkHandler.classify(slot, useProbabilities, hints);
			slotClass = prediction.getLeft();
			ranking = prediction.getRight();
			System.out.println(String.format("True class: %s ==== infered class: %s ==== ranking: %s", slot.getSlotClass(), slotClass, ranking));
			if(slot instanceof Attribute){
				System.out.println(String.format("String value: %s",((Attribute)slot).getValue()));
			}
			//System.out.println(String.format("Predicted class %s", slotClass));
			slot.setHint(slotClass);
			slot.setHintsRanking(ranking);
		} catch (Exception e) {
			System.out.println("There has been a problem while trying to classify a slot: ");
			e.printStackTrace();
		}
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				refineHints(child, hints);
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
}
