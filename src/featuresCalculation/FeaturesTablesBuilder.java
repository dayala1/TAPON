package featuresCalculation;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.parser.ParseException;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import utils.FileUtils;

public class FeaturesTablesBuilder implements Serializable{
	
	private static final long serialVersionUID = 1295466241822143370L;
	
	//Constructors---------------------------------------------------

	public FeaturesTablesBuilder() {
		//this.tablesMap = new HashMap<String, List<FeaturesTable>>();
	}
	
	//Properties-----------------------------------------------------
	
	//private Map<String, List<FeaturesTable>> tablesMap;
	
	//public Map<String, List<FeaturesTable>> getTablesMap() {
		//return tablesMap;
	//}
	
	//Interface methods----------------------------------------------
	
	public void initialize(String outputFolder, Set<Feature<?>> datasetFeatures, Set<Feature<?>> recordFeatures, Set<Feature<?>> attributeFeatures) throws IOException, ParseException {
		//List<FeaturesTable> datasetTables;
		//List<FeaturesTable> recordTables;
		//List<FeaturesTable> attributeTables;
		//FeaturesTable featuresTable;
		File outFile;
		String featureName;
		List<String> features;

		/*
		datasetTables = new ArrayList<FeaturesTable>();
		recordTables = new ArrayList<FeaturesTable>();
		attributeTables = new ArrayList<FeaturesTable>();
		outFile = new File(String.format("resources/%s/attributes", outputFolder));
		if (!outFile.exists()) {
			outFile.mkdirs();
		}
		outFile = new File(String.format("resources/%s/records", outputFolder));
		if (!outFile.exists()) {
			outFile.mkdirs();
		}*/
		outFile = new File(outputFolder);
		if (!outFile.exists()) {
			outFile.mkdirs();
		}
		
		outFile = new File(String.format("%s/datasets.csv", outputFolder));
		if(!outFile.exists()) {
			FileUtils.createCSV(String.format("%s/datasets.csv", outputFolder));
			features = new ArrayList<String>();
			features.add("name");
			for (Feature feature : datasetFeatures) {
				featureName = feature.toString();
				features.add(featureName);
			}
			FileUtils.addLine(String.format("%s/datasets.csv", outputFolder), features);
		}
		
		outFile = new File(String.format("%s/records.csv", outputFolder));
		if (!outFile.exists()) {
			FileUtils.createCSV(String.format("%s/records.csv", outputFolder));
			features = new ArrayList<String>();
			features.add("name");
			for (Feature feature : recordFeatures) {
				featureName = feature.toString();
				features.add(featureName);
			}
			features.add("class");
			FileUtils.addLine(String.format("%s/records.csv", outputFolder), features);
		}
		
		/*
		for (String recordClass : recordClasses) {
			featuresTable = new FeaturesTable();
			featuresTable.setName(recordClass);
			recordTables.add(featuresTable);
			outFile = new File(String.format("resources/%s/records/%s.csv", outputFolder, recordClass));
			if (!outFile.exists()) {
				createCSV(String.format("resources/%s/records/%s.csv", outputFolder, recordClass));
				features = new ArrayList<String>();
				features.add("name");
				for (Feature feature : recordFeatures) {
					featureName = feature.toString();
					features.add(featureName);
				}
				features.add("class");
				features.add("slotClass");
				addLine(String.format("resources/%s/records/%s.csv", outputFolder, recordClass), features);
			}
			
		}*/
		
		outFile = new File(String.format("%s/attributes.csv", outputFolder));
		if (!outFile.exists()) {
			FileUtils.createCSV(String.format("%s/attributes.csv", outputFolder));
			features = new ArrayList<String>();
			features.add("name");
			for (Feature feature : attributeFeatures) {
				featureName = feature.toString();
				features.add(featureName);
			}
			features.add("class");
			FileUtils.addLine(String.format("%s/attributes.csv", outputFolder), features);
		}
		
		/*
		for (String attributeClass : attributeClasses) {
			featuresTable = new FeaturesTable();
			featuresTable.setName(attributeClass);
			attributeTables.add(featuresTable);
			outFile = new File(String.format("resources/%s/attributes/%s.csv", outputFolder, attributeClass));
			if (!outFile.exists()) {
				createCSV(String.format("resources/%s/attributes/%s.csv", outputFolder, attributeClass));
				features = new ArrayList<String>();
				features.add("name");
				for (Feature feature : attributeFeatures) {
					featureName = feature.toString();
					features.add(featureName);
				}
				features.add("class");
				features.add("slotClass");
				addLine(String.format("resources/%s/attributes/%s.csv", outputFolder, attributeClass), features);
			}
		}*/
		
		/*
		tablesMap.put("dataset", datasetTables);
		tablesMap.put("records", recordTables);
		tablesMap.put("attributes", attributeTables);
		*/
	}
	
	public void addVector(Featurable featurable, FeaturesVector featuresVector, String outputFolder){
		List<FeaturesTable> datasetTables;
		List<FeaturesTable> recordTables;
		List<FeaturesTable> attributeTables;
		List<FeaturesTable> featuresTables;
		List<String> lineValues;
		FeaturesTable featuresTable;
		Dataset dataset;
		Slot slot;
		String slotClass;
		String folder;
		File folderFile;
		File file;
		File[] files;
		String fileName;
		String className;
		
		//datasetTables = tablesMap.get("datasets");
		//recordTables = tablesMap.get("records");
		//attributeTables = tablesMap.get("attributes");
		
		if(featurable instanceof Dataset){
			lineValues = new ArrayList<String>();
			lineValues.add(featurable.getName());
			lineValues.addAll(featuresVector.getRawValues());
			FileUtils.addLine(String.format("%s/datasets.csv", outputFolder), lineValues);
			dataset = (Dataset)featurable;
			featuresTable = new FeaturesTable();
			featuresTable.setName("datasets");
			featuresVector.setFeaturable(dataset);
			featuresTable.addFeaturesVector(featuresVector);
			//datasetTables.add(featuresTable);
		} else {
			slot = (Slot)featurable;
			slotClass = slot.getSlotClass();
			
			//Selection of the list that will be used
			if(slot instanceof Record) {
				//featuresTables = recordTables;
				folder = "records";
			} else {
				//featuresTables = attributeTables;
				folder = "attributes";
			}
			
			//Addition of the new vector to each Table
			file = new File(String.format("%s/%s.csv", outputFolder, folder));
			featuresVector.setVectorClass(slotClass);
		    lineValues = new ArrayList<String>();
			lineValues.add(featurable.getName());
			lineValues.addAll(featuresVector.getRawValues());
			FileUtils.addLine(String.format("%s/%s.csv", outputFolder, folder), lineValues);
			
			/*
			folderFile = new File(String.format("resources/%s/%s", outputFolder, folder));
			files = folderFile.listFiles();
			for (File file : files) {
		    	featuresVector = featuresVector.clone();
		    	fileName = file.getName();
		    	className = fileName.substring(0, fileName.length()-4);
		    	if(className.equals(slotClass)){
					featuresVector.setVectorClass("YES");
				}else{
					featuresVector.setVectorClass("NO");
				}
		    	lineValues = new ArrayList<String>();
				lineValues.add(featurable.getName());
				lineValues.addAll(featuresVector.getRawValues());
				lineValues.add(slotClass);
				addLine(String.format("resources/%s/%s/%s", outputFolder, folder, fileName), lineValues);
			}
			*/
			
			//Ancient tables creation code. Could be useful in the future
			/*for(FeaturesTable storedFeaturesTable : featuresTables){
				featuresVector = featuresVector.clone();
				if(storedFeaturesTable.getName().equals(slotClass)){
					featuresVector.setVectorClass("YES");
				}else{
					featuresVector.setVectorClass("NO");
				}
				storedFeaturesTable.addFeaturesVector(featuresVector);
			}*/
		}
	}
	
	//Ancillary methods----------------------------------------------
	
	
}
