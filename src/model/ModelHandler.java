package model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import featuresCalculation.ClassesConfiguration;
import featuresCalculation.DatasetFeaturesCalculator;
import featuresCalculation.FeaturesGroup;
import model.SparkHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.LuceneUtils;

import java.io.*;
import java.util.*;

public abstract class ModelHandler {
	// Constructors---------------------------------------------------
	public ModelHandler() {
		this.sparkHandler = initializeSparkHandler();
		this.folderCount = 0;
		this.useProbabilities = true;
	}

	public abstract SparkHandler initializeSparkHandler();

	// Properties-----------------------------------------------------
	protected Set<FeaturesGroup> slotFeaturesGroups;
	protected Set<FeaturesGroup> featurableFeaturesGroups;
	protected Set<FeaturesGroup> hintsSlotFeaturesGroups;
	protected Set<FeaturesGroup> hintsFeaturableFeaturesGroups;
	protected String tablesRootFolder;
	protected String classifiersRootFolder;
	protected boolean useProbabilities;
	protected Long firstModelTrainTime;
	protected Long secondModelTrainTime;
	protected Long firstFeaturesComputingTime;
	protected Long secondFeaturesComputingTime;

	public Set<FeaturesGroup> getSlotFeaturesGroups() {
		return slotFeaturesGroups;
	}

	public SparkHandler getSparkHandler(){
		return this.sparkHandler;
	}

	public Map<Integer, String> getFeatureNamesAttributesHF(){
		return this.sparkHandler.getFeatureNamesAttributesHF();
	}
	public Map<Integer, String> getFeatureNamesAttributesHB(){
		return this.sparkHandler.getFeatureNamesAttributesHB();
	}
	public Map<Integer, String> getFeatureNamesRecordsHF(){
		return this.sparkHandler.getFeatureNamesRecordsHF();
	}
	public Map<Integer, String> getFeatureNamesRecordsHB(){
		return this.sparkHandler.getFeatureNamesRecordsHB();
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

	public Long getFirstFeaturesComputingTime() {
		return firstFeaturesComputingTime;
	}

	public void setFirstFeaturesComputingTime(Long firstFeaturesComputingTime) {
		assert firstFeaturesComputingTime != null;

		this.firstFeaturesComputingTime = firstFeaturesComputingTime;
	}

	public Long getSecondFeaturesComputingTime() {
		return secondFeaturesComputingTime;
	}

	public void setSecondFeaturesComputingTime(Long secondFeaturesComputingTime) {
		assert secondFeaturesComputingTime != null;

		this.secondFeaturesComputingTime = secondFeaturesComputingTime;
	}

	public ClassesConfiguration getClassesConfiguration(){
		assert this.firstDatasetFeaturesCalculator != null;
		return this.firstDatasetFeaturesCalculator.getClassesConfiguration();
	}

	public void loadClassifiers(boolean hints) {
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.loadClassifiers(hints);
	}

	public void loadClassifiers(){
		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.loadClassifiers(false);
		sparkHandler.loadClassifiers(true);
	}

	// Internal state-------------------------------------------------
	protected SparkHandler sparkHandler;
	protected DatasetFeaturesCalculator firstDatasetFeaturesCalculator;
	protected DatasetFeaturesCalculator secondDatasetFeaturesCalculator;
	protected DatasetFeaturesCalculator secondFeaturesCalculator;
	protected Integer folderCount;

	// Interface methods----------------------------------------------

	public void resetFolderCount() {
		this.folderCount = 0;
	}

	public void loadFeatureNames() throws IOException {
		sparkHandler.setTablesFolder(tablesRootFolder);
		sparkHandler.loadFeatureNames();
	}

	public void writeClassifiersDebug() throws IOException {
		String dumpFolder = String.format("%s/classifiersParsed", this.classifiersRootFolder);
		new File(dumpFolder).mkdir();
		sparkHandler.writeClassifiersString(dumpFolder);
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

	protected void saveResults(Slot slot, JSONArray trueLabels, JSONArray inferedLabels, JSONArray bothLabels) {
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
			children = ((Record) slot).getSlots();
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
			children = ((Record) slot).getSlots();
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

	public void trainModel(Collection<Dataset> trainingDatasets, Map<String, String> params) throws Exception {
		trainModel(trainingDatasets, params, true, true);
	}

	public void trainModel(Collection<Dataset> trainingDatasets, Map<String, String> params, boolean createSecondModel, boolean useMulticlass) throws Exception {
		assert this.tablesRootFolder != null;
		assert this.classifiersRootFolder != null;
		assert this.featurableFeaturesGroups != null;
		assert this.slotFeaturesGroups != null;
		assert this.hintsFeaturableFeaturesGroups != null;
		assert this.hintsSlotFeaturesGroups != null;

		File tablesRootFolderFile;
		File classifiersRootFolderFile;
		DatasetReader datasetReader;
		List<Dataset> testingDatasets;
		List<File> trainingDatasetFiles;
		List<File> testingDatasetFiles;
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;
		File folderFile;
		ClockMonitor clock;
		ClassesConfiguration classesConfiguration;

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

		datasetReader = new DatasetReader();
		if (tablesRootFolderFile.exists()) {
			FileUtils.cleanDirectory(tablesRootFolderFile);
		}
		if (classifiersRootFolderFile.exists()) {
			FileUtils.cleanDirectory(classifiersRootFolderFile);
		}
		tablesRootFolderFile.mkdirs();
		classifiersRootFolderFile.mkdirs();
		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));
		setParams(params);
		try {
			firstDatasetFeaturesCalculator.initializeClasses(trainingDatasets);
			firstDatasetFeaturesCalculator.getClassesConfiguration()
					.store(String.format("%s/classes.json", tablesRootFolder));
			secondDatasetFeaturesCalculator
					.setClassesConfiguration(firstDatasetFeaturesCalculator.getClassesConfiguration());
			sparkHandler.setClassesConfiguration(firstDatasetFeaturesCalculator.getClassesConfiguration());
			firstDatasetFeaturesCalculator.initialize(false);
			System.out.println("Storing first features calculator");
			firstDatasetFeaturesCalculator.store(String.format("%s/firstFeaturesCalculator", tablesRootFolder));
			secondDatasetFeaturesCalculator.initialize(false);
			System.out.println("Storing second features calculator");
			secondDatasetFeaturesCalculator.store(String.format("%s/secondFeaturesCalculator", tablesRootFolder));
			//System.exit(0);
			clock.start();
			for (Dataset dataset : trainingDatasets) {
				firstDatasetFeaturesCalculator.setDataset(dataset);
				firstDatasetFeaturesCalculator.run(String.format("%s/trainingTablesNoHint", tablesRootFolder), true);
				System.out.println(String.format("Processed dataset %s", dataset));
			}
			clock.stop();
			setFirstFeaturesComputingTime(clock.getCPUTime());
			firstDatasetFeaturesCalculator.closeTablesBuilder();
		} catch (Exception e) {
			System.out.println("There was a problem while trying to create the first feature tables: ");
			e.printStackTrace();
			throw e;
		}

		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);

		// First classifiers creation
		sparkHandler.setNumberOfFeatures(firstDatasetFeaturesCalculator.getNumberOfFeatures());
		clock.start();
		sparkHandler.createBinaryClassifiers(false);
		clock.pause();
		sparkHandler.createMulticlassClassifierCsv(useProbabilities, false);
		sparkHandler.trainMulticlassClassifier(false);
		clock.resume();
		sparkHandler.loadClassifiers(false);
		clock.stop();
		setFirstModelTrainTime(clock.getCPUTime());

		///////////////////////////
		// PHASE TWO STARTS HERE //
		///////////////////////////
		if (createSecondModel) {
			try {
				clock.start();
				for (Dataset dataset : trainingDatasets) {
					refineHints(dataset, false, useMulticlass);
					secondDatasetFeaturesCalculator.setDataset(dataset);
					secondDatasetFeaturesCalculator.run(String.format("%s/trainingTablesHint", tablesRootFolder), true);
					System.out.println(String.format("Processed dataset %s", dataset));
				}
				clock.stop();
				setSecondModelTrainTime(clock.getCPUTime());
				secondDatasetFeaturesCalculator.closeTablesBuilder();

			} catch (Exception e) {
				System.out.println("There was a problem while trying to compute the features of datasets");
				throw e;
			}

			// Second classifiers creation
			sparkHandler.setNumberOfFeatures(secondDatasetFeaturesCalculator.getNumberOfFeatures());

			try {
				clock.start();
				sparkHandler.createBinaryClassifiers(true);
				clock.pause();
				sparkHandler.createMulticlassClassifierCsv(useProbabilities, true);
				sparkHandler.trainMulticlassClassifier(true);
				clock.resume();
				sparkHandler.loadClassifiers(true);
				clock.stop();

			} catch (Exception e) {
				System.out.println("There was a problem while trying to create the second classifiers: ");
				e.printStackTrace();
				throw e;
			}
			setSecondModelTrainTime(getFirstModelTrainTime() + clock.getCPUTime());
			System.out.println("Model creation has finished!");
		}
	}

	protected abstract void setParams(Map<String, String> params);

	public void loadFeaturesCalculators() throws IOException, ClassNotFoundException {
		assert firstDatasetFeaturesCalculator != null;
		assert secondDatasetFeaturesCalculator != null;

		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;

		// Loading the first features calculator
		fileInputStream = new FileInputStream(String.format("%s/firstFeaturesCalculator", tablesRootFolder));
		objectInputStream = new ObjectInputStream(fileInputStream);
		firstDatasetFeaturesCalculator = (DatasetFeaturesCalculator) objectInputStream.readObject();
		objectInputStream.close();

		// Loading the second one
		fileInputStream = new FileInputStream(String.format("%s/secondFeaturesCalculator", tablesRootFolder));
		objectInputStream = new ObjectInputStream(fileInputStream);
		secondDatasetFeaturesCalculator = (DatasetFeaturesCalculator) objectInputStream.readObject();
		objectInputStream.close();

		// Updating the classes configuration of the sparkHandler
		sparkHandler.setClassesConfiguration(firstDatasetFeaturesCalculator.getClassesConfiguration());
	}

	public void classifySlots(Dataset dataset, int numberOfHintsIterations) throws IOException, ParseException {
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
		refineHints(dataset, false);

		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);

		secondDatasetFeaturesCalculator.setDataset(dataset);

		for (int i = 0; i < numberOfHintsIterations; i++) {
			secondDatasetFeaturesCalculator.run("none", false);
			refineHints(dataset, true);
		}

		sparkHandler.closeContext();
	}

	public void createNewContext() {
		sparkHandler.createNewContext();
	}

	public void closeContext() {
		sparkHandler.closeContext();
	}

	public void refineHintsUnlabelledDataset(Dataset dataset) throws IOException, ParseException {
		refineHintsUnlabelledDataset(dataset, true);
	}

	public void refineHintsUnlabelledDataset(Dataset dataset, boolean useMulticlass) throws IOException, ParseException {
		assert firstDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;

		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));

		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);

		firstDatasetFeaturesCalculator.setDataset(dataset);
		firstDatasetFeaturesCalculator.run("none", false);
		refineHints(dataset, false, useMulticlass);
	}

	public void refineHintsOnce(Dataset dataset) throws IOException, ParseException {
		refineHintsOnce(dataset, true);
	}

	public void refineHintsOnce(Dataset dataset, boolean useMulticlass) throws IOException, ParseException {
		assert secondDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;

		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));

		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);

		secondDatasetFeaturesCalculator.setDataset(dataset);
		secondDatasetFeaturesCalculator.run("none", false);
		refineHints(dataset, true, useMulticlass);
	}

	/*
	 * This method should be used when hints are already computed, yet features
	 * have not (i.e., hints were obtained beforehand)
	 */
	public void refineHintsOnceBothClassifiers(Dataset dataset) throws IOException, ParseException {
		refineHintsOnceBothClassifiers(dataset, true);
	}

	public void refineHintsOnceBothClassifiers(Dataset dataset, boolean useMulticlass) throws IOException, ParseException {
		assert secondDatasetFeaturesCalculator != null;
		assert tablesRootFolder != null;
		assert classifiersRootFolder != null;
		assert dataset != null;

		sparkHandler.setClassesFilePath(String.format("%s/classes.json", tablesRootFolder));

		sparkHandler.setClassifiersFolder(classifiersRootFolder);
		sparkHandler.setTablesFolder(tablesRootFolder);

		firstDatasetFeaturesCalculator.setDataset(dataset);
		firstDatasetFeaturesCalculator.run("none", false);
		secondDatasetFeaturesCalculator.setDataset(dataset);
		secondDatasetFeaturesCalculator.run("none", false);
		refineHints(dataset, true, useMulticlass);
	}

	public void classifySlotsUntilConvergence(Dataset dataset, int hintsIterationsLimit, boolean useMulticlass)
			throws IOException, ParseException {
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
		refineHints(dataset, false, useMulticlass);

		secondDatasetFeaturesCalculator.setDataset(dataset);

		boolean convergence = false;
		iterationsCount = 0;
		while (!convergence) {
			hintsBefore = getHintsMap(dataset);
			secondDatasetFeaturesCalculator.run("none", false);
			refineHints(dataset, true, useMulticlass);
			hintsAfter = getHintsMap(dataset);
			iterationsCount++;

			if (iterationsCount >= hintsIterationsLimit) {
				break;
			}

			convergence = true;

			for (String slotName : hintsBefore.keySet()) {
				hintBefore = hintsBefore.get(slotName);
				hintAfter = hintsAfter.get(slotName);
				if (!hintBefore.equals(hintAfter)) {
					convergence = false;
					break;
				}
			}
		}

	}

	// Ancillary methods----------------------------------------------

	protected void refineHints(Dataset dataset, boolean hints) {
		refineHints(dataset, hints, true);
	}

	protected void refineHints(Dataset dataset, boolean hints, boolean useMulticlass) {
		assert dataset != null;
		List<Slot> children;

		children = dataset.getSlots();
		for (Slot child : children) {
			refineHints(child, hints, useMulticlass);
		}
	}

	protected void refineHints(Slot slot, boolean hints, boolean useMulticlass) {
		assert slot != null;
		List<Slot> children;
		String slotClass;
		LinkedHashMap<String, Double> ranking;
		Pair<String, LinkedHashMap<String, Double>> prediction;
		if(slot instanceof Attribute){
		try {
			prediction = sparkHandler.classify(slot, useProbabilities, hints, useMulticlass);
			slotClass = prediction.getLeft();
			ranking = prediction.getRight();
			System.out.println(String.format("True class: %s ==== infered class: %s ==== ranking: %s",
					slot.getSlotClass(), slotClass, ranking));
			if (slot instanceof Attribute) {
				System.out.println(String.format("String value: %s", ((Attribute) slot).getValue()));
			}
			// System.out.println(String.format("Predicted class %s",
			// slotClass));
			slot.setHint(slotClass);
			//slot.setHint(new ArrayList<>(ranking.keySet()).get(0));
			slot.setHintsRanking(ranking);
		} catch (Exception e) {
			System.out.println("There has been a problem while trying to classify a slot: ");
			e.printStackTrace();
		}}

		if (slot instanceof Record) {
			children = ((Record) slot).getSlots();
			for (Slot child : children) {
				refineHints(child, hints, useMulticlass);
			}
		}
	}

	protected Map<String, String> getHintsMap(Dataset dataset) {
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

	protected void getHintsMap(Slot slot, Map<String, String> hintsMap) {
		assert slot != null;
		assert hintsMap != null;
		List<Slot> children;

		hintsMap.put(slot.getName(), slot.getHint());

		if (slot instanceof Record) {
			children = ((Record) slot).getSlots();
			for (Slot child : children) {
				getHintsMap(child, hintsMap);
			}
		}
	}
}
