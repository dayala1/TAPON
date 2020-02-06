package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import utils.DatasetReader;

public class StorePatriciaJsonDataDriver {
	
	public static void main(String[] args) throws IOException {
		String datasetsRoot = "resources/Movies/www.imdb.com";
		String textFilesRoot = "D:/knowledgebases/knowledgebases/Movies/www.imdb.com";
		String seedRecord = "movie";
		File rootDir = new File(datasetsRoot+"/"+seedRecord);
		File[] rootExtractions = rootDir.listFiles();
		JSONObject jsonObject;
		DatasetReader reader = new DatasetReader();
		FileWriter fileWriter;
		String filePath;
		Gson gson;
		JsonParser jsonParser;
		JsonWriter jsonWriter;
		
		gson = new GsonBuilder().setPrettyPrinting().create();
		jsonObject = new JSONObject();
		if (rootExtractions != null) {
			for (File file : rootExtractions) {
				String extractionID = file.getName();
				//System.out.println(String.format("Extraction %s", extractionID));
				jsonObject = reader.readDataset(datasetsRoot, textFilesRoot, extractionID, seedRecord);
				extractionID = extractionID.substring(0, extractionID.length()-3);
				extractionID = String.format("%s.json", extractionID);
				filePath = String.format("resources/Datasets/Patricia/Movies/www.imdb.com/%s", extractionID);
				fileWriter = new FileWriter(filePath);
				jsonWriter = new JsonWriter(fileWriter);
				jsonWriter.setIndent("    ");
				jsonParser = new JsonParser();
				gson.toJson(jsonParser.parse(jsonObject.toJSONString()), jsonWriter);
				fileWriter.close();
				//System.out.println(jsonObject.toString(4));
			}
		}
	}
	
	public void processElement(String path){
		
	}
}
