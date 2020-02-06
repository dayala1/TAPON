package featuresCalculation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ClassesConfiguration implements Serializable{
	// Constructors---------------------------------------------------
	private static final long serialVersionUID = 4074761028144779168L;

	public ClassesConfiguration() {
		super();
		this.attributeClasses = new TreeSet<>();
		this.recordClasses = new TreeSet<>();
		this.attributeClassesMapping = HashBiMap.create();
		this.recordClassesMapping = HashBiMap.create();
		this.isNumeric = new HashMap<>();
		this.values = new HashMap<>();
	}

	// Properties-----------------------------------------------------

	private SortedSet<String> attributeClasses;
	private SortedSet<String> recordClasses;
	private BiMap<String, Integer> attributeClassesMapping;
	private BiMap<String, Integer> recordClassesMapping;
	private Map<String, Boolean> isNumeric;
	private Map<String, List<String>> values;

	public void setIsNumeric(String slotClass, Boolean isNumeric){
		this.isNumeric.put(slotClass, isNumeric);
	}

	public Boolean getIsNumeric(String slotClass){
		return this.isNumeric.get(slotClass);
	}

	public BiMap<String, Integer> getAttributeClassesMapping() {
		return this.attributeClassesMapping;
	}
	
	public BiMap<String, Integer> getRecordClassesMapping() {
		return this.recordClassesMapping;
	}

	public Set<String> getAttributeClasses() {
		Set<String> result = Collections.unmodifiableSet(attributeClasses);

		return result;
	}

	public Set<String> getRecordClasses() {
		Set<String> result = Collections.unmodifiableSet(recordClasses);

		return result;
	}

	public void addAttributeClass(String attributeClass) {
		assert attributeClass != null;

		attributeClasses.add(attributeClass);
		if (!attributeClassesMapping.containsKey(attributeClass)) {
			attributeClassesMapping.put(attributeClass, attributeClassesMapping.size());
		}
	}

	public void addRecordClass(String recordClass) {
		assert recordClass != null;

		recordClasses.add(recordClass);
		if (!recordClassesMapping.containsKey(recordClass)) {
			recordClassesMapping.put(recordClass, recordClassesMapping.size());
		}
	}

	public void removeAttributeClas(String attributeClass) {
		assert attributeClass != null;
		assert attributeClasses.contains(attributeClass);

		attributeClasses.remove(attributeClass);
	}

	public void removeRecordClas(String recordClass) {
		assert recordClass != null;
		assert recordClasses.contains(recordClass);

		recordClasses.remove(recordClass);
	}

	// Internal state-------------------------------------------------

	// Interface methods----------------------------------------------

	public void setClassValues(String className, List<String> values){
		this.values.put(className, values);
	}

	public List<String> getClassValues(String className){
		return this.values.get(className);
	}

	public void setValues(Map<String, List<String>> values){
		this.values = values;
	}

	public void store(String objectPath) throws IOException {
		FileOutputStream fileOutputStream;
		ObjectOutputStream objectOutputStream;

		fileOutputStream = new FileOutputStream(objectPath);
		objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.close();
	}

	/*
	 * public void loadClasses(String classesPath) throws IOException,
	 * ParseException { JSONParser jsonParser; File classesFile; FileReader
	 * fileReader; JSONObject jsonObject; List<String> recordClasses;
	 * List<String> attributeClasses;
	 * 
	 * jsonParser = new JSONParser(); classesFile = new File(classesPath);
	 * fileReader = new FileReader(classesFile); jsonObject =
	 * (JSONObject)jsonParser.parse(fileReader); recordClasses =
	 * (List<String>)jsonObject.get("recordClasses"); attributeClasses =
	 * (List<String>)jsonObject.get("attributeClasses");
	 * 
	 * for (String attributeClass : attributeClasses) {
	 * classesConfiguration.addAttributeClass(attributeClass); } for (String
	 * recordClass : recordClasses) {
	 * classesConfiguration.addRecordClass(recordClass); } }
	 * 
	 * public void storeClasses(String classesPath) throws IOException { assert
	 * classesPath != null;
	 * 
	 * JSONObject jsonObject; JSONParser jsonParser; FileReader fileReader; File
	 * outFile; FileWriter fileWriter; JsonWriter jsonWriter; JsonParser
	 * gsonParser; Gson gson;
	 * 
	 * jsonParser = new JSONParser(); outFile = new File(classesPath);
	 * jsonObject = new JSONObject(); jsonObject.put("recordClasses",
	 * Lists.newArrayList(getRecordClasses()));
	 * jsonObject.put("attributeClasses",
	 * Lists.newArrayList(getAttributeClasses())); fileWriter = new
	 * FileWriter(outFile, false); jsonWriter = new JsonWriter(fileWriter);
	 * jsonWriter.setIndent("    "); gsonParser = new JsonParser(); gson = new
	 * GsonBuilder().setPrettyPrinting().create();
	 * gson.toJson(gsonParser.parse(jsonObject.toJSONString()), jsonWriter);
	 * fileWriter.close(); }
	 */

	// Ancillary methods----------------------------------------------
}
