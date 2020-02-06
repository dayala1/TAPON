package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spark_project.guava.collect.Lists;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import dataset.Slot;
import jersey.repackaged.com.google.common.collect.Sets;
import utils.FileUtilsCust;

public class ConfusionMatrixDriverTwoFoldsAlternative_AttributesOnly {
	
	private static Table<String, String, Integer> confusionMatrix;
	private static Integer numSlots;

	public static void main(String[] args) throws IOException, ParseException {
		String datasetsRootPath;
		Integer maxNumIterations;
		String datasetsFolderPath;
		String resultsFilePath;
		String classesPath;
		List<String> row;
		List<String> header;
		List<List<String>> rows;
		File datasetsFolder;
		File[] datasetFolders;
		File jsonFile;
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		Set<String> slotClasses;
		Map<String, Integer> rowColumn;
		Double TP;
		Double TN;
		Double FP;
		Double FN;
		Double totalTP;
		Double totalTN;
		Double totalFP;
		Double totalFN;
		Double totalPrecision;
		Double totalRecall;
		Double numClasses;
		String techName;
		Boolean addIterations;
		String finalTechName;
		Double precision;
		Double recall;
		Double f1;

		datasetsRootPath = "E:/model/testsPham/8-folds";
		classesPath = "E:/Documents/US/Tesis/datasets/classes.json";
		techName = "Pham";
		addIterations = false;
		slotClasses = Sets.newHashSet();
		addClasses(slotClasses, classesPath);
		slotClasses.add("none");
		maxNumIterations = 1;
		resultsFilePath = String.format("%s/experimentalResults2.csv", datasetsRootPath);
		FileUtilsCust.createCSV(resultsFilePath);
		rows = Lists.newArrayList();
		header = Lists.newArrayList();
		header.add("TECH");
		header.add("CLASS");
		header.add("PRECISION");
		header.add("RECALL");
		header.add("F1");
		numSlots = 0;
		for(int j2 = 0; j2 <= 9; j2++){
			//for(int j = j2+1; j <= 10; j++) {
				for (int i = 1; i <= maxNumIterations; i++) {
					confusionMatrix = HashBasedTable.create();
					
					for (String slotClass1 : slotClasses) {
						for (String slotClass2 : slotClasses) {
							confusionMatrix.put(slotClass1, slotClass2, 0);
						}
					}
					totalTP = 0.0;
					totalTN = 0.0;
					totalFP = 0.0;
					totalFN = 0.0;
					numClasses = 0.0;
					totalPrecision = 0.0;
					totalRecall = 0.0;
					datasetsFolderPath = String.format("%s/%s/results/results/%s-iterations", datasetsRootPath, j2, i);
					datasetsFolder = new File(datasetsFolderPath);
					datasetFolders = datasetsFolder.listFiles();
					for (File datasetFolder : datasetFolders) {
						if (datasetFolder.isDirectory()) {
							jsonFile = new File(String.format("%s/both.json", datasetFolder.getAbsolutePath()));
							fileReader = new FileReader(jsonFile);
							jsonParser = new JSONParser();
							jsonObject = (JSONObject)jsonParser.parse(fileReader);
							processJSONObject(jsonObject);
							fileReader.close();
						}
					}
					//System.out.println(confusionMatrix);
					for (String slotClass : slotClasses) {
						row = Lists.newArrayList();
						finalTechName =techName;
						if (addIterations) {
							finalTechName = finalTechName.concat(String.format("%s-iterations", i));
						}
						row.add(finalTechName);
						System.out.println(i+" iterations");
						numClasses++;
						TP = 0.0;
						TN = 0.0;
						FP = 0.0;
						FN = 0.0;
						for (String slotClass1 : slotClasses) {
							boolean flag = false;
							for (String slotClass2 : slotClasses) {
								if (slotClass1.equals(slotClass)) {
									if (slotClass1.equals(slotClass2)) {
										TP += (confusionMatrix.get(slotClass1, slotClass2) == null ? 0 : confusionMatrix.get(slotClass1, slotClass2));
									} else {
										FN += (confusionMatrix.get(slotClass1, slotClass2) == null ? 0 : confusionMatrix.get(slotClass1, slotClass2));
									}
								} else {
									if (slotClass2.equals(slotClass)) {
										FP += (confusionMatrix.get(slotClass1, slotClass2) == null ? 0 : confusionMatrix.get(slotClass1, slotClass2));
									} else {
										TN += (confusionMatrix.get(slotClass1, slotClass2) == null ? 0 : confusionMatrix.get(slotClass1, slotClass2));
									}
								}
							}
						}
						totalTP += TP;
						totalTN += TN;
						totalFP += FP;
						totalFN += FN;
						//System.out.println(String.format("%s:\nTrue positives: %s\nFalse negatives: %s\nFalse positives: %s", slotClass, TP, FN, FP));
						//System.out.println(slotClass);
						//System.out.println((1+TP)/(1+TP+FN));
						//System.out.println((1+TP)/(1+TP+FP));
						row.add(slotClass);
						recall = (1+TP)/(1+TP+FN);
						precision = (1+TP)/(1+TP+FP);
						f1 = 2*(precision*recall)/(precision+recall);
						row.add(Double.toString(precision));
						row.add(Double.toString(recall));
						row.add(Double.toString(f1));
						rows.add(row);
						totalPrecision += ((1+TP)/(1+TP+FP));
						totalRecall += ((1+TP)/(1+TP+FN));
					}
					row = Lists.newArrayList();
					finalTechName =techName;
					if (addIterations) {
						finalTechName = finalTechName.concat(String.format("%s-iterations", i));
					}
					row.add(finalTechName);
					row.add("micro");
					//Precision
					precision = (totalTP)/(totalTP+totalFP);
					row.add(Double.toString(precision));
					//System.out.println((totalTP)/(totalTP+totalFP));
					//Recall
					recall=(totalTP)/(totalTP+totalFN);
					row.add(Double.toString(recall));
					//F1
					f1 = 2*(precision*recall)/(precision+recall);
					row.add(Double.toString(f1));
					//System.out.println((totalTP)/(totalTP+totalFN));
					//AUC-ROC
					//System.out.println(0.5*(1+(totalTP/(totalTP+totalFN))-(totalFP/(totalFP+totalTN))));
					rows.add(row);
					
					row = Lists.newArrayList();
					finalTechName =techName;
					if (addIterations) {
						finalTechName = finalTechName.concat(String.format("%s-iterations", i));
					}
					row.add(finalTechName);
					row.add("macro");
					//Precision (macro)
					//System.out.println(totalPrecision/numClasses);
					precision = totalPrecision/numClasses;
					row.add(Double.toString(precision));
					//Recall (macro)
					//System.out.println(totalRecall/numClasses);
					recall = totalRecall/numClasses;
					row.add(Double.toString(recall));
					//F1
					f1 = 2*(precision*recall)/(precision+recall);
					row.add(Double.toString(f1));
					//System.out.println("----");
					rows.add(row);
				}
			
			//}
		}
		/*for (String	slotClass : slotClasses) {
			header.add(String.format("%s-PRECISION", slotClass));
			header.add(String.format("%s-RECALL", slotClass));
		}
		header.add("MICRO-PRECISION");
		header.add("MICRO-RECALL");
		header.add("MACRO-PRECISION");
		header.add("MACRO-RECALL");*/
		FileUtilsCust.addLine(resultsFilePath, header);
		for (List<String> fileRow : rows) {
			FileUtilsCust.addLine(resultsFilePath, fileRow);
		}
		printConfusionMatrix();
	}
	
	public static void processJSONObject(JSONObject jsonObject) throws FileNotFoundException, IOException {
		assert jsonObject != null;
		
		JSONArray children;
		JSONObject child;
		Slot slot;
		
		children = (JSONArray)jsonObject.get("children");
		for (int i = 0; i < children.size(); i++) {
			child = (JSONObject)children.get(i);
			processJSONObjectSlot(child);
		}
	}
	
	public static void processJSONObjectSlot(JSONObject jsonObject) throws FileNotFoundException, IOException {
		assert jsonObject != null;
		
		String trueClass;
		String inferedClass;
		JSONArray children;
		JSONObject child;
		Integer matrixValue;
		
		if (!jsonObject.containsKey("children")) {
			trueClass = (String)jsonObject.get("trueClass");
			inferedClass = (String)jsonObject.get("inferedClass");
			if(inferedClass.length()<2){
				inferedClass="none";
			}
			
			if (confusionMatrix.contains(trueClass, inferedClass)) {
				matrixValue = confusionMatrix.get(trueClass, inferedClass);
			} else {
				matrixValue = 0;
			}
			matrixValue++;
			confusionMatrix.put(trueClass, inferedClass, matrixValue);
		}
		
		if (jsonObject.containsKey("children")) {
			children = (JSONArray)jsonObject.get("children");
			for (int i = 0; i < children.size(); i++) {
				child = (JSONObject)children.get(i);
				processJSONObjectSlot(child);
			}
		}
		numSlots++;
	}
	
	public static void addClasses(Set<String> emptyClasses, String classesFilePath) throws IOException, ParseException {
		assert classesFilePath != null;
		
		JSONParser jsonParser;
		File classesFile;
		FileReader fileReader;
		JSONObject jsonObject;
		List<String> recordClasses;
		List<String> attributeClasses;
		
		jsonParser = new JSONParser();
		classesFile = new File(String.format("%s", classesFilePath));
		fileReader = new FileReader(classesFile);
		jsonObject = (JSONObject)jsonParser.parse(fileReader);
		recordClasses = (List<String>)jsonObject.get("recordClasses");
		attributeClasses = (List<String>)jsonObject.get("attributeClasses");
		
		emptyClasses.addAll(attributeClasses);
		//emptyClasses.addAll(recordClasses);
	}
	
	public static void printConfusionMatrix(){
		String reducedClass;
		System.out.print(String.format("%16s╔", ""));
		for (int i = 0; i < confusionMatrix.rowKeySet().size()-1; i++) {
			System.out.print("════════════════╦");
		}
		System.out.print("════════════════╗");
		
		System.out.print("\n");
		System.out.print(String.format("%16s║", ""));
		for (String matrixClass : confusionMatrix.rowKeySet()) {
			if((matrixClass.length())>16){
				reducedClass = matrixClass.substring(matrixClass.length()-16, matrixClass.length());
			}else{
				reducedClass = matrixClass;
			}
			System.out.print(String.format("%16s║", reducedClass));
		}
		System.out.print("\n");
		
		for (String matrixClass : confusionMatrix.rowKeySet()) {
			System.out.print("╠═══════════════");
			for (int i = 0; i < confusionMatrix.rowKeySet().size(); i++) {
				System.out.print("╬════════════════");
			}
			System.out.print("╣\n");
			if((matrixClass.length())>15){
				reducedClass = matrixClass.substring(matrixClass.length()-15, matrixClass.length());
			}else{
				reducedClass = matrixClass;
			}
			System.out.print(String.format("║%15s║", reducedClass));
			for (String matrixClass2 : confusionMatrix.rowKeySet()) {
				System.out.print(String.format("%16s║", confusionMatrix.get(matrixClass, matrixClass2)));
			}
			System.out.print("\n");
		}
		System.out.print("╚═══════════════");
		for (int i = 0; i < confusionMatrix.rowKeySet().size(); i++) {
			System.out.print("╩════════════════");
		}
		System.out.print("╝\n");
	}

}
