package main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.spark_project.guava.collect.Sets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

public class GsonDriver {

	public static void main(String[] args) throws IOException {
		JSONObject jsonObject;
		JSONParser jsonParser;
		FileReader fileReader;
		File outFile;
		FileWriter fileWriter;
		JsonWriter jsonWriter;
		JsonParser gsonParser;
		Gson gson;
		
		jsonParser = new JSONParser();
		outFile = new File("C:/Users/Boss/Documents/test.json");
		jsonObject = new JSONObject();
		Set<String> recordClasses = Sets.newHashSet("http://epo.rkbexplorer.com/ontologies/epo#IPC-Classification", "http://www.aktors.org/ontology/portal#project-Reference");
		jsonObject.put("recordClasses", recordClasses);
		fileWriter = new FileWriter(outFile, false);
		jsonWriter = new JsonWriter(fileWriter);
		jsonWriter.setIndent("    ");
		gsonParser = new JsonParser();
		gson = new GsonBuilder().setPrettyPrinting().create();
		gson.toJson(gsonParser.parse(jsonObject.toJSONString()), jsonWriter);
		//http://epo.rkbexplorer.com/ontologies/epo#IPC-Classification, http://www.aktors.org/ontology/portal#project-Reference
		fileWriter.close();

	}
	//Constructors---------------------------------------------------

	//Properties-----------------------------------------------------

	//Internal state-------------------------------------------------

	//Interface methods----------------------------------------------

	//Ancillary methods----------------------------------------------
}
