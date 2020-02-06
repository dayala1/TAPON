package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import au.com.bytecode.opencsv.CSVReader;
import jersey.repackaged.com.google.common.collect.Lists;

public class FileUtilsCust {
	public static Set<String> visitedElements = new HashSet<String>();
	
	public static List<String> searchInFile(String regexp, File file, int groupIndex) throws FileNotFoundException, IOException{
		List<String> result;
		Pattern pattern;
		Matcher matcher;
		String match;
		
		result = new ArrayList<String>();
		pattern = Pattern.compile(regexp);
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	matcher = pattern.matcher(line);
		    	while(matcher.find()){
		    		match = matcher.group(groupIndex);
		    		result.add(match);
		    	}
		    }
		}
		
		return result;
	}
	
	public static void processElement(String path, String elementClass, String extractionID) throws FileNotFoundException, IOException{
		Matcher matcher;
		Pattern pattern = Pattern.compile("\\p{L}");
		String datasetsRoot = "E:/Documents/US/Tesis/Dataset/Patricia/pl-files/Books/www.abebooks.com";
		File dir = new File(datasetsRoot);
		File rootDir = new File(datasetsRoot+"/"+elementClass);
		File file = new File(rootDir.getAbsolutePath()+"/"+extractionID);
		File[] directories = dir.listFiles();
		
		List<String> children = FileUtilsCust.searchInFile("^child\\('"+path+"', '(.*?)'\\).", file, 1);
		for(String child: children) {
			String childClass = "null";
			for (File directory : directories) {
				File extractionFile = new File(directory.getAbsolutePath()+"/"+extractionID);
				String regexp = "^"+directory.getName()+"\\('"+child+"'\\)";
				List<String> foundChild = searchInFile(regexp, extractionFile, 0);
				if(!foundChild.isEmpty()){
					childClass = directory.getName();
				}
			}
			System.out.println("encontrado hijo de la clase "+childClass);
			processElement(child, (childClass=="null")?elementClass:childClass, extractionID);
		}
		
		
	}
	
	public static JSONObject processElementAlt(String path, String extractionID, File infoSource, String datasetsRoot) throws FileNotFoundException, IOException{
		JSONObject result;
		File dir = new File(datasetsRoot);
		File[] directories = dir.listFiles();
		String elementClass;
		List<String> foundChild;
		JSONObject jsonChild;
		JSONArray jsonArray;
		
		visitedElements.add(path);
		result = new JSONObject();
		elementClass = "null";
		
		for (File directory : directories) {
			File extractionFile = new File(directory.getAbsolutePath()+"/"+extractionID);
			String regexp = "^"+directory.getName()+"\\('"+path+"'\\)";
			try{
				foundChild = searchInFile(regexp, extractionFile, 0);
				if(!foundChild.isEmpty()){
					elementClass = directory.getName();
					System.out.println(elementClass);
				}
			}catch(FileNotFoundException e){
				
			}
		}
		result.put("class", elementClass);
		result.put("path", path);
		
		List<String> children = FileUtilsCust.searchInFile("^child(?:ren)?\\('"+path+"', ?'(.*?)'\\).", infoSource, 1);
		if(!children.isEmpty()){
			jsonArray = new JSONArray();
			for(String child: children) {
				if(!visitedElements.contains(child)){
					jsonChild = processElementAlt(child, extractionID, infoSource, datasetsRoot);
					if((String)jsonChild.get("class")=="null"){
						if(jsonChild.has("children")){
							for (Object newChild : jsonChild.getJSONArray("children")) {
								jsonArray.put(newChild);
							}
						}
					}
					else{
						jsonArray.put(jsonChild);
					}
				}
			}
			if(jsonArray.length()>0) {
				result.put("children", jsonArray);
			}
		}
		
		return result;
	}
	
	public static void createCSV(String filePath) {
		FileWriter fileWriter;
		CSVPrinter csvFilePrinter;
		String lineSeparator;
		CSVFormat csvFormat;
		File folderFile;
		
		fileWriter = null;
		csvFilePrinter = null;
		lineSeparator = "\n";
		csvFormat = CSVFormat.DEFAULT.withRecordSeparator(lineSeparator);
		folderFile = new File(filePath);
		folderFile.getParentFile().mkdirs();
		try {
			fileWriter = new FileWriter(filePath, true);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);
		} catch (IOException e) {
			System.out.println(String.format("There were problems while creating the csv file '%s'", filePath));
			e.printStackTrace();
		} finally {
			try {
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println(String.format("There were problems while closing the new csv file '%s'", filePath));
			}
		}
	}
	
	public static void addLine(String filePath, Collection<?> values) {
		FileWriter fileWriter;
		CSVPrinter csvFilePrinter;
		String lineSeparator;
		CSVFormat csvFormat;
		
		fileWriter = null;
		csvFilePrinter = null;
		lineSeparator = "\n";
		csvFormat = CSVFormat.DEFAULT.withRecordSeparator(lineSeparator);
		try {
			fileWriter = new FileWriter(filePath, true);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);
			csvFilePrinter.printRecord(values);
	
		} catch (IOException e) {
			System.out.println(String.format("There were problems while appending a line to the csv file '%s'", filePath));
		} finally {
			try {
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println(String.format("There were problems while closing the csv file '%s'", filePath));
			}
		}
	}
	
	public static void addLine(String filePath, Object... value) {
		FileWriter fileWriter;
		CSVPrinter csvFilePrinter;
		String lineSeparator;
		CSVFormat csvFormat;
		List<Object> values;
		
		fileWriter = null;
		csvFilePrinter = null;
		lineSeparator = "\n";
		csvFormat = CSVFormat.DEFAULT.withRecordSeparator(lineSeparator);
		values = Lists.newArrayList();
		for (Object o:value) {
			values.add(o);
		}
		try {
			fileWriter = new FileWriter(filePath, true);
			csvFilePrinter = new CSVPrinter(fileWriter, csvFormat);
			csvFilePrinter.printRecord(values);
	
		} catch (IOException e) {
			System.out.println(String.format("There were problems while appending a line to the csv file '%s'", filePath));
		} finally {
			try {
				fileWriter.close();
				csvFilePrinter.close();
			} catch (IOException e) {
				System.out.println(String.format("There were problems while closing the csv file '%s'", filePath));
			}
		}
	}
	
	public static void joinClassifierCSVs(String filePath1, String filePath2, String outputPath) {
		assert filePath1 != null;
		assert filePath2 != null;
		assert outputPath != null;
		
		BufferedReader br1;
		BufferedReader br2;
		FileReader fr1;
		FileReader fr2;
		File file1;
		File file2;
		String lineSeparator;
		CSVFormat csvFormat;
		CSVReader csvReader1;
		CSVReader csvReader2;
		String[] line1;
		String[] line2;
		List<String> line1List;
		List<String> line2List;
		
		createCSV(outputPath);
		try {
			file1 = new File(filePath1);
			file2 = new File(filePath2);
			fr1 = new FileReader(file1);
			fr2 = new FileReader(file2);
			csvReader1 = new CSVReader(fr1);
			csvReader2 = new CSVReader(fr2);
		    while ((line1 = csvReader1.readNext()) != null) {
		    	line2 = csvReader2.readNext();
		    	line1 = Arrays.copyOfRange(line1, 0, line1.length-1);
		    	line2 = Arrays.copyOfRange(line2, 1, line2.length);
		    	line1List = Arrays.asList(line1);
		    	line2List = Arrays.asList(line2);
		    	line1List.addAll(line2List);
		    	addLine(outputPath, line1List);
		    }
		} catch (Exception e) {
			System.out.println("There was a problem while trying to create the union of csv files: ");
			e.printStackTrace();
		}
		   
	}

	public static void save(Serializable object, String objectPath) throws IOException {
		FileOutputStream fileOutputStream;
		ObjectOutputStream objectOutputStream;

		fileOutputStream = new FileOutputStream(objectPath);
		objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(object);
		objectOutputStream.close();
	}

	public static Object load(String objectPath) throws IOException, ClassNotFoundException {
		Object res;
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;

		fileInputStream = new FileInputStream(objectPath);
		objectInputStream = new ObjectInputStream(fileInputStream);
		res =  objectInputStream.readObject();
		objectInputStream.close();

		return res;
	}
}
