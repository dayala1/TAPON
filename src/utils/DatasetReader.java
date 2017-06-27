package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;

public class DatasetReader {
	//Constructors---------------------------------------------------
	public DatasetReader(){
		elementCount = 0;
	}

	//Internal state-------------------------------------------------
	private Set<String> visitedElements;
	Integer elementCount;
	
	//Interface methods----------------------------------------------
	public JSONObject readDataset(String datasetRoot, String textFilesRoot, String extractionID, String seedRecord) throws FileNotFoundException, IOException{
		assert datasetRoot != null;
		assert extractionID != null;
		assert seedRecord != null;
		
		JSONObject result;
		File rootDir;
		JSONArray jsonArray;
		File file;
		List<String> rootElements;
		
		result = new JSONObject();
		rootDir = new File(datasetRoot+"/"+seedRecord);
		jsonArray = new JSONArray();
		
		visitedElements = new HashSet<String>();
		file = new File(rootDir+"/"+extractionID);
		rootElements = FileUtils.searchInFile("^"+seedRecord+"\\('(.*?)'\\).", file, 1);
		for (String rootElement : rootElements) {
			jsonArray.add(processElement(rootElement, extractionID, file, datasetRoot, textFilesRoot));
		}
		result.put("children", jsonArray);
		result.put("extractionID", extractionID);
		
		return result;

	}
	
	public Dataset loadJSONObject(JSONObject jsonObject) throws FileNotFoundException, IOException {
		assert jsonObject != null;
		
		Dataset result;
		String name;
		JSONArray children;
		JSONObject child;
		Slot slot;
		
		result = new Dataset();
		if(jsonObject.containsKey("name")){
			name = (String)jsonObject.get("name");
		} else {
			name = elementCount.toString();
			elementCount ++;
		}
		result.setName(name);
		children = (JSONArray)jsonObject.get("children");
		for (int i = 0; i < children.size(); i++) {
			child = (JSONObject)children.get(i);
			slot = loadJSONObjectSlot(child);
			result.addSlot(slot);
		}
		
		return result;
	}
	
	public void getDatasetsFiles(String folderPath, Double fractionAll, Double fractionTesting, List<File> trainingDatasetsFiles, List<File> testingDatasetsFiles) throws IOException, ParseException {
		assert folderPath != null;
		assert fractionTesting != null;
		assert trainingDatasetsFiles != null;
		assert testingDatasetsFiles != null;
		
		File folderFile;
		File[] files;
		List<File> filesList;
		List<File> trainingFiles;
		List<File> testingFiles;
		Integer numAll;
		Integer numTesting;

		folderFile = new File(folderPath);
		files = folderFile.listFiles();
		filesList = Arrays.asList(files);
		numAll = (int)(files.length*fractionAll);
		numTesting = (int)(numAll*fractionTesting);
		
		testingFiles = filesList.subList(0, numTesting);
		trainingFiles = filesList.subList(numTesting, numAll);
		
		trainingDatasetsFiles.clear();
		testingDatasetsFiles.clear();
		
		trainingDatasetsFiles.addAll(trainingFiles);
		testingDatasetsFiles.addAll(testingFiles);
		
	}
	
	public Dataset loadDatasetFile(File datasetFile) throws IOException, ParseException{
		assert datasetFile != null;
		
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		Dataset dataset;
		
		fileReader = new FileReader(datasetFile);
		jsonParser = new JSONParser();
		jsonObject = (JSONObject)jsonParser.parse(fileReader);
		dataset = loadJSONObject(jsonObject);
		
		return dataset;
	}
	
	public void addDataset(String folderPath, Double fractionAll, Double fractionTesting, List<Dataset> trainingDatasets, List<Dataset> testingDatasets) throws IOException, ParseException {
		assert folderPath != null;
		assert fractionTesting != null;
		assert trainingDatasets != null;
		assert testingDatasets != null;
		
		File folderFile;
		File[] files;
		List<File> filesList;
		List<File> trainingFiles;
		List<File> testingFiles;
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		Integer numAll;
		Integer numTesting;
		Dataset dataset;

		jsonParser = new JSONParser();
		folderFile = new File(folderPath);
		files = folderFile.listFiles();
		filesList = Arrays.asList(files);
		numAll = (int)(files.length*fractionAll);
		numTesting = (int)(numAll*fractionTesting);
		
		testingFiles = filesList.subList(0, numTesting);
		trainingFiles = filesList.subList(numTesting, numAll);
		
		//Dataset storing
		for (File file : trainingFiles) {
			fileReader = new FileReader(file);
			jsonObject = (JSONObject)jsonParser.parse(fileReader);
			dataset = loadJSONObject(jsonObject);
			trainingDatasets.add(dataset);
		}
		
		for (File file : testingFiles) {
			fileReader = new FileReader(file);
			jsonObject = (JSONObject)jsonParser.parse(fileReader);
			dataset = loadJSONObject(jsonObject);
			testingDatasets.add(dataset);
		}
	}
	
	//Ancillary methods----------------------------------------------
	public Slot loadJSONObjectSlot(JSONObject jsonObject) throws FileNotFoundException, IOException {
		assert jsonObject != null;
		
		Slot result;
		String name;
		String slotClass;
		JSONArray children;
		JSONObject child;
		Slot slot;
		
		slotClass = (String)jsonObject.get("class");
		if (!jsonObject.containsKey("children")) {
			result = new Attribute();
			((Attribute)result).setValue((String)jsonObject.get("textualValue"));
		} else {
			result = new Record();
			children = (JSONArray)jsonObject.get("children");
			for (int i = 0; i < children.size(); i++) {
				child = (JSONObject)children.get(i);
				slot = loadJSONObjectSlot(child);
				((Record)result).addSlot(slot);
			}
		}
		if(jsonObject.containsKey("startIndex")) {
			result.setStartIndex((Integer)jsonObject.get("startIndex"));
		}
		
		if(jsonObject.containsKey("endIndex")) {
			result.setEndIndex((Integer)jsonObject.get("endIndex"));
		}
		if(jsonObject.containsKey("name")){
			name = (String)jsonObject.get("name");
		} else {
			name = elementCount.toString();
			elementCount ++;
		}
		result.setName(name);
		result.setSlotClass(slotClass);
		
		return result;
	}
	
	private JSONObject processElement(String id, String extractionID, File infoSource, String datasetsRoot, String textFilesRoot) throws FileNotFoundException, IOException{
		assert id != null;
		assert extractionID != null;
		assert infoSource != null;
		assert datasetsRoot != null;
		
		JSONObject result;
		File dir = new File(datasetsRoot);
		File[] directories = dir.listFiles();
		String elementClass;
		File extractionFile;
		String regexp;
		String path;
		List<String> foundElement;
		List<String> children;
		JSONObject jsonChild;
		JSONArray jsonArray;
		String clearExtractionID;
		File textFile;
		String textualValue;
		List<String> regexpResults;
		
		visitedElements.add(id);
		result = new JSONObject();
		elementClass = "null";
		
		for (File directory : directories) {
			extractionFile = new File(String.format("%s/%s", directory.getAbsolutePath(), extractionID));
			regexp =String.format("^%s\\('%s'\\)", directory.getName(), id);
			try{
				foundElement = FileUtils.searchInFile(regexp, extractionFile, 0);
				if(!foundElement.isEmpty()){
					elementClass = directory.getName();
				}
			}catch(FileNotFoundException e){
				System.out.println(String.format("Didn't find a file corresponding to class %s for extraction %s", directory.getName(), extractionID));
			}
		}
		result.put("class", elementClass);
		result.put("id", id);
		//regexp = String.format("^?:%% XPATH: %s = (.*?)$", id);
		//regexpResults = FileUtils.searchInFile(regexp, infoSource, 1);
		//if (regexpResults.isEmpty()) {
		regexp = String.format(".*?%s.*?, xpath = (.*?)$", id);
		regexpResults = FileUtils.searchInFile(regexp, infoSource, 1);
		//}
		path = regexpResults.get(0);
		result.put("path", path);
		regexp = String.format("^child(?:ren)?\\('%s', ?'(.*?)'\\).", id);
		children = FileUtils.searchInFile(regexp, infoSource, 1);
		//Check that children have been found
		if(!children.isEmpty()){
			jsonArray = new JSONArray();
			for(String child: children) {
				//Check that the child hasn't already been processed
				if(!visitedElements.contains(child)){
					jsonChild = processElement(child, extractionID, infoSource, datasetsRoot, textFilesRoot);
					//Check that the class of the child isn't null
					if((String)jsonChild.get("class")!="null"){
						jsonArray.add(jsonChild);
					}
					else{
						//If it is null, skip the child and add its children to the current element
						if(jsonChild.containsKey("children")){
							for (Object newChild : (JSONArray)jsonChild.get("children")) {
								jsonArray.add(newChild);
							}
						}
					}
				}
			}
			//Check that some children finally remain. If they do, this element is a record
			if(jsonArray.size()>0) {
				//Sometimes there is a textual value given to the record. We store this value as an attribute
				if(elementClass != "null"){
					clearExtractionID = extractionID.substring(0, extractionID.length()-3);
					textualValue = getTextualValue(clearExtractionID, textFilesRoot, elementClass, path);
					if (!textualValue.isEmpty()) {
						jsonChild = new JSONObject();
						jsonChild.put("class", String.format("%s-value", elementClass));
						jsonChild.put("id", id);
						jsonChild.put("textualValue", textualValue);
						jsonChild.put("path", path);
						jsonArray.add(jsonChild);
					}
				}
				result.put("children", jsonArray);
			//If they don't, it could be an attribute
			} else if(elementClass != "null"){
				clearExtractionID = extractionID.substring(0, extractionID.length()-3);
				textualValue = getTextualValue(clearExtractionID, textFilesRoot, elementClass, path);
				result.put("textualValue", textualValue);
			}
		//If there are no children to begin with, it could be an attribute
		} else if (elementClass != "null") {
			clearExtractionID = extractionID.substring(0, extractionID.length()-3);
			textualValue = getTextualValue(clearExtractionID, textFilesRoot, elementClass, path);
			result.put("textualValue", textualValue);
		}
		
		return result;
	}
	
	private String getTextualValue(String extractionID, String textFilesRoot, String elementClass, String path) throws IOException {
		assert extractionID != null; 
		assert textFilesRoot != null;
		assert elementClass != null;
		assert path != null;
		
		String result;
		File textFile;
		String regexp;
		
		try{
			textFile = new File(String.format("%s/attribute_%s/%s.txt", textFilesRoot, elementClass, extractionID));
			if (!textFile.exists()) {
				throw new FileNotFoundException();
			}
			if (path.startsWith("/html[1]/body[1]")) {
				path = path.substring(16, path.length());
				path = String.format("%s%s", "body", path);
			}
			//THIS IS ONLY FOR BigBook
			//path = path.replaceAll("table\\[2\\]", "p[1]/table[1]");
			//THIS IS ONLY FOR ausopen.com
			//path = path.replaceAll("body\\/table\\[2\\]", "body/brclear=\"left\"[1]/table[2]");
			//if (elementClass.equals("name")) {
			//	path = path.concat("/text()[1]");
			//}
			//THIS IS ONLY FOR imdb.com
			if (elementClass.equals("runtime") || elementClass.equals("title")) {
				path = path.concat("/text()[1]");
			}
			
			path = path.replaceAll("/", "\\\\/");
			path = path.replaceAll("\\[", "\\\\[");
			path = path.replaceAll("\\]", "\\\\]");
			path = path.replaceAll("\\(", "\\\\(");
			path = path.replaceAll("\\)", "\\\\)");
			
			//THIS IS ONLY FOR careerbuilder.com
			//if (!elementClass.equals("location")) {
			//	path = String.format("(%s|%s)", path, path.substring(0, path.length()-10));
			//} else {
			//	path = String.format("(%s|%s)", path, path+"\\/text\\(\\)\\[1\\]");
			//}
			
			regexp = String.format(".*?xpath: %s ---- text: (.*?)(attribute|$)", path);
			//System.out.println(String.format("Using regexp to find textual value: %s", regexp));
			try{
				result = FileUtils.searchInFile(regexp, textFile, 1).get(0);
			} catch (Exception e) {
				System.out.println(String.format("There was a problem with attribute of class %s in extraction %s. Expected path: %s", elementClass, extractionID, path));
				result = "NOT FOUND";
			}
		} catch (Exception e){
			System.out.println("The given attribute had no file");
			result = "";
		}
		
		return result;
	}
}
