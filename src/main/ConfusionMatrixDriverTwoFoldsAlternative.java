package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.simmetrics.StringMetric;
import org.spark_project.guava.collect.Lists;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import dataset.Slot;
import jersey.repackaged.com.google.common.collect.Maps;
import utils.FileUtilsCust;

public class ConfusionMatrixDriverTwoFoldsAlternative {
	
	private static Table<String, String, Integer> confusionMatrix;
	private static Table<String, String, Integer> globalConfusionMatrix;
	private static Table<String, String, Double> similarityMatrix;
	private static Map<String, Double> precisions;
	private static Map<String, Double> recalls;
	private static Map<String, Double> F1s;
	private static Integer numSlots;

	public static void main(String[] args) throws IOException, ParseException {
		String datasetsRootPath;
		Integer maxNumIterations;
		String datasetsFolderPath;
		String resultsFilePath;
		String similarityFilePath;
		String globalConfusionFilePath;
		String classesPath;
		List<String> row;
		List<String> header;
		List<List<String>> rows;
		List<List<String>> rowsSimilarity;
		List<List<String>> rowsConfusion;
		File datasetsFolder;
		File[] datasetFolders;
		File jsonFile;
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		List<String> slotClasses;
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
		double[] vector1;
		double[] vector2;
		double dotProduct;
		double normA;
		double normB;
		double mean;
		double size;
		String attributeValue1;
		StringMetric stringMetric;
		double cosineSimilarity;
		int numTests;
		
		datasetsRootPath = "C:/Users/Boss/Documents/RandomForestTwoFoldsNoDifferences/results/results/10-domains";
		classesPath = "C:/Users/Boss/Documents/RandomForestNew/classifiersAndTables/modelTables/10-domains/fold-1/classes.json";
		techName = "Neville";
		addIterations = false;
		slotClasses = Lists.newArrayList();
		addClasses(slotClasses, classesPath);
		//slotClasses.add("none");
		maxNumIterations = 10;
		resultsFilePath = String.format("%s/experimentalResults.csv", datasetsRootPath);
		similarityFilePath = String.format("%s/similarity.csv", datasetsRootPath);
		globalConfusionFilePath = String.format("%s/confusion.csv", datasetsRootPath);
		FileUtilsCust.createCSV(resultsFilePath);
		FileUtilsCust.createCSV(similarityFilePath);
		FileUtilsCust.createCSV(globalConfusionFilePath);
		rows = Lists.newArrayList();
		header = Lists.newArrayList();
		header.add("TECH");
		header.add("CLASS");
		header.add("PRECISION");
		header.add("RECALL");
		header.add("F1");
		numSlots = 0;
		numTests = 0;
		similarityMatrix = HashBasedTable.create();
		globalConfusionMatrix = HashBasedTable.create();
		for (String slotClass1 : slotClasses) {
			for (String slotClass2 : slotClasses) {
				similarityMatrix.put(slotClass1, slotClass2, 0.0);
				globalConfusionMatrix.put(slotClass1, slotClass2, 0);
			}
		}
		rowsConfusion = new ArrayList<>();
		rowsSimilarity = new ArrayList<>();
		row = new ArrayList<>();
		row.add("CLASS1");
		row.add("CLASS2");
		row.add("COSINE_SIMILARITY");
		rowsSimilarity.add(row);
		row = new ArrayList<>();
		row.add("CLASS1");
		row.add("CLASS2");
		row.add("VALUE");
		rowsConfusion.add(row);
		for(int j2=1; j2<=9; j2++) {
			for(int j = j2+1; j <= 10; j++) {
				numTests++;
				for (int i = 10; i <= maxNumIterations; i++) {
					
					confusionMatrix = HashBasedTable.create();
					precisions = Maps.newHashMap();
					recalls = Maps.newHashMap();
					F1s = Maps.newHashMap();
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
					datasetsFolderPath = String.format("%s/fold-%s-%s/%s-iterations", datasetsRootPath, j2, j, i);
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
						precisions.put(slotClass, precision);
						recalls.put(slotClass, recall);
						F1s.put(slotClass, f1);
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
			
			/*
			confusionMatrix = HashBasedTable.create();
			datasetsFolderPath = String.format("%s/convergence", datasetsRootPath);
			datasetsFolder = new File(datasetsFolderPath);
			datasetFolders = datasetsFolder.listFiles();
			for (File datasetFolder : datasetFolders) {
				jsonFile = new File(String.format("%s/both.json", datasetFolder.getAbsolutePath()));
				fileReader = new FileReader(jsonFile);
				jsonParser = new JSONParser();
				jsonObject = (JSONObject)jsonParser.parse(fileReader);
				processJSONObject(jsonObject);
				fileReader.close();
			}*/
			//System.out.println(confusionMatrix);
			/*
			slotClasses = confusionMatrix.rowKeySet();
			for (String slotClass : slotClasses) {
				TP = 0.0;
				TN = 0.0;
				FP = 0.0;
				FN = 0.0;
				for (String slotClass1 : slotClasses) {
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
					//System.out.println(String.format("%s:\nTrue positives: %s\nFalse negatives: %s\nFalse positives: %s", slotClass, TP, FN, FP));
				}
			}
			*/
		//System.out.println(numSlots);
			}
		}
		FileUtilsCust.addLine(resultsFilePath, header);
		for (List<String> fileRow : rows) {
			FileUtilsCust.addLine(resultsFilePath, fileRow);
		}
		for (String slotClass1 : slotClasses) {
			for (String slotClass2 : slotClasses) {
				int vectorSize = slotClasses.size();
				vector1 = new double[vectorSize];
				vector2 = new double[vectorSize];
				for (int k = 0; k < slotClasses.size(); k++) {
					vector1[k] = globalConfusionMatrix.get(slotClass1, slotClasses.get(k));
					vector2[k] = globalConfusionMatrix.get(slotClass2, slotClasses.get(k));
				}
				
				dotProduct = 0.0;
			    normA = 0.0;
			    normB = 0.0;
			    for (int k = 0; k < vector1.length; k++) {
			        dotProduct += vector1[k] * vector2[k];
			        normA += vector1[k]*vector1[k];
			        normB += vector2[k]*vector2[k];
			    }

			    cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
			    
				
				row = new ArrayList<>();
				row.add(slotClass1);
				row.add(slotClass2);
				row.add(Double.toString(cosineSimilarity));
				rowsSimilarity.add(row);
				
				row = new ArrayList<>();
				row.add(slotClass1);
				row.add(slotClass2);
				row.add(globalConfusionMatrix.get(slotClass1, slotClass2).toString());
				rowsConfusion.add(row);
			}
		}
		for (List<String> fileRow : rowsSimilarity) {
			FileUtilsCust.addLine(similarityFilePath, fileRow);
		}
		for (List<String> fileRow : rowsConfusion) {
			FileUtilsCust.addLine(globalConfusionFilePath, fileRow);
		}
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
		Integer globalMatrixValue;
		
		trueClass = (String)jsonObject.get("trueClass");
		inferedClass = (String)jsonObject.get("inferedClass");
		if(inferedClass==null || inferedClass.length()<2){
			inferedClass="none";
		}
		
		if (confusionMatrix.contains(trueClass, inferedClass)) {
			matrixValue = confusionMatrix.get(trueClass, inferedClass);
		} else {
			matrixValue = 0;
		}
		if (globalConfusionMatrix.contains(trueClass, inferedClass)) {
			globalMatrixValue = globalConfusionMatrix.get(trueClass, inferedClass);
		} else {
			globalMatrixValue = 0;
		}
		matrixValue++;
		confusionMatrix.put(trueClass, inferedClass, matrixValue);
		globalConfusionMatrix.put(trueClass, inferedClass, matrixValue);
		
		if (jsonObject.containsKey("children")) {
			children = (JSONArray)jsonObject.get("children");
			for (int i = 0; i < children.size(); i++) {
				child = (JSONObject)children.get(i);
				processJSONObjectSlot(child);
			}
		}
		numSlots++;
	}
	
	public static void addClasses(List<String> emptyClasses, String classesFilePath) throws IOException, ParseException {
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
		emptyClasses.addAll(recordClasses);
	}

}
