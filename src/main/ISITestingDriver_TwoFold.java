package main;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.google.re2j.Pattern;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import jersey.repackaged.com.google.common.collect.Lists;
import semanticTyper.LuceneBasedSTModelHandler;
import semanticTyper.SemanticTypeLabel;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

public class ISITestingDriver_TwoFold {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;
	public static Integer folderCount = 0;
	public static Map<String, List<String>> classExamples;
	public static Pattern emptyPattern = Pattern.compile("( )+");

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		//MODEL CREATION
		Dataset dataset;
		List<String> trainingDatasetsFolders;
		classExamples = new HashMap<String, List<String>>();
		
		//MODEL APPLICATION
		LuceneBasedSTModelHandler typer;
		List<Dataset> trainingDatasets;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		int testingFoldNumber;
		int testingFoldNumber2;
		Integer numberOfDomains;
		String datasetsPath;
		String resultsPath;
		
		clock = new ClockMonitor();
		typer = new LuceneBasedSTModelHandler("1");
		typer.setModelHandlerEnabled(true);
		
		//MODEL APPLICATION
		datasetReader = new DatasetReader();
		trainingDatasets = new ArrayList<Dataset>();
		testingDatasets = new ArrayList<Dataset>();
		
		domains = Arrays.copyOfRange(args, 5, args.length);
		numberOfDomains = domains.length;
		resultsRoot = args[1];
		datasetsRoot = args[2];
		testingFoldNumber = Integer.valueOf(args[3]);
		testingFoldNumber2 = Integer.valueOf(args[4]);
		
		trainingDatasetsFolders = new ArrayList<String>();
		
		for (String domain : domains) {
			for (int i = 1; i < 11; i++) {
				datasetsPath = String.format("%s/Datasets/%s/%s",datasetsRoot, domain, i);
				if (i == testingFoldNumber || i == testingFoldNumber2) {
					datasetReader.addDataset(datasetsPath, 1.0, trainingDatasets);
				} else {
					datasetReader.addDataset(datasetsPath, 1.0, testingDatasets);
				}
			}
		}
		
		resultsPath = String.format("%s/results", resultsRoot);
		clock.start();
		for (Dataset trainingDataset : trainingDatasets) {
			indexExamples(trainingDataset, typer);
		}
		System.out.println("finished storing examples");
		typer.setTextualDirectory(String.format("%s/results/%s-domains/fold-%s-%s/1-iterations", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		for (String examplesClass : classExamples.keySet()) {
			System.out.println(String.format("indexing examples of class %s", examplesClass));
			typer.addType(examplesClass, classExamples.get(examplesClass));
			System.out.println(String.format("Done", examplesClass));
		}
		clock.stop();
		FileUtilsCust.createCSV(String.format("%s/results/%s-domains/fold-%s-%s/trainingTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		FileUtilsCust.addLine(String.format("%s/results/%s-domains/fold-%s-%s/trainingTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2), clock.getCPUTime());
		
		clock.start();
		System.out.println("Starting testing");
		for (Dataset testingDataset : testingDatasets) {
			predictClasses(testingDataset, typer);
			checkHints(testingDataset);
			saveResults(testingDataset, String.format("%s/results/%s-domains/fold-%s-%s/1-iterations", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		}
		clock.stop();
		FileUtilsCust.createCSV(String.format("%s/results/%s-domains/fold-%s-%s/1-iterations/applicationTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2));
		FileUtilsCust.addLine(String.format("%s/results/%s-domains/fold-%s-%s/1-iterations/applicationTime.csv", resultsPath, numberOfDomains, testingFoldNumber, testingFoldNumber2), clock.getCPUTime());
		folderCount = 0;
	}
	
	public static void checkHints(Dataset dataset) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			checkHints(child);
		}
	}
	
	public static void checkHints(Slot slot) {
		assert slot != null;
		List<Slot> children;
		
		//System.out.println(String.format("Slot of class %s classified as %s", slot.getSlotClass(), slot.getHint()));
	
		if(slot instanceof Attribute){
			if(slot.getSlotClass().equals(slot.getHint())) {
				correct++;
			} else {
				incorrect++;
			}
		}else{
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				checkHints(child);
			}
		}
	}
	
	public static void indexExamples(Dataset dataset, LuceneBasedSTModelHandler typer) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			indexExamples(child, typer);
		}
	}
	
	public static void indexExamples(Slot slot, LuceneBasedSTModelHandler typer) {
		assert slot != null;
		List<Slot> children;
		String slotClass;
		String textualValue;
		List<String> examples;
		
		//System.out.println(String.format("Slot of class %s classified as %s", slot.getSlotClass(), slot.getHint()));
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				indexExamples(child, typer);
			}
		}else{
			slotClass = slot.getSlotClass();
			textualValue = ((Attribute)slot).getValue();
			if(classExamples.containsKey(slotClass)){
				examples = classExamples.get(slotClass);
			}else{
				examples = Lists.newArrayList();
			}
			examples.add(textualValue);
			classExamples.put(slotClass, examples);
			System.out.println("Indexed an example");
		}
	}
	
	public static void predictClasses(Dataset dataset, LuceneBasedSTModelHandler typer) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			predictClasses(child, typer);
		}
	}
	
	public static void predictClasses(Slot slot, LuceneBasedSTModelHandler typer) {
		assert slot != null;
		
		String predictedClass;
		List<Slot> children;
		String slotClass;
		String textualValue;
		List<String> examples;
		List<SemanticTypeLabel> predictions;
		
		//System.out.println(String.format("Slot of class %s classified as %s", slot.getSlotClass(), slot.getHint()));
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				predictClasses(child, typer);
			}
		}else{
			slotClass = slot.getSlotClass();
			textualValue = ((Attribute)slot).getValue();
			examples = Lists.newArrayList();
			examples.add(textualValue);
			if(!(textualValue.isEmpty() || emptyPattern.matcher(textualValue).matches())){
				predictions = typer.predictType(examples, 1);
				if(predictions.size()>0){
					predictedClass = predictions.get(0).getLabel();
				}else{
					predictedClass = "none";
				}
			}else{
				predictedClass = "none";
			}
			slot.setHint(predictedClass);
		}
	}
	
	public static void saveResults(Dataset dataset, String rootPath) throws IOException {
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
	
	@SuppressWarnings("unchecked")
	private static void saveResults(Slot slot, JSONArray trueLabels, JSONArray inferedLabels, JSONArray bothLabels) {
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
	
	public static void saveResultsWithoutArrays(Dataset dataset, String rootPath) throws IOException {
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
	
	private static void saveResultsWithoutArrays(Slot slot, JSONObject trueLabels, JSONObject inferedLabels) {
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
	
}
