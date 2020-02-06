package rest;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Path;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import model.randomForest.ModelHandlerRandomForest;
import utils.DatasetReader;

public class RestService extends ServerResource {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;
	public static ModelHandlerRandomForest modelHandler;
	
	public static void init(){
		/*C:/Users/Boss/Documents/RandomForestNewProbabilities
C:/Users/Boss/Documents/RandomForestNewProbabilities
C:/Users/Boss/Documents
1
*/
		Integer numberOfDomains;
		String classifiersTablesRoot;
		Integer testingFoldNumber;
		
		numberOfDomains = 10;
		classifiersTablesRoot = "C:/Users/Boss/Documents/DemoModels1Fold";
		testingFoldNumber = 1;
		modelHandler = new ModelHandlerRandomForest();
		modelHandler.setClassifiersRootFolder(String.format("%s/classifiersAndTables/modelClassifiers/%s-domains", classifiersTablesRoot, numberOfDomains));
		modelHandler.setTablesRootFolder(String.format("%s/classifiersAndTables/modelTables/%s-domains", classifiersTablesRoot, numberOfDomains));
		try {
			modelHandler.loadFeaturesCalculators();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		modelHandler.createNewContext();
		System.out.println("Started loading classifiers");
		modelHandler.loadClassifiers(false);
		modelHandler.loadClassifiers(true);
		System.out.println("Finished loading the model!");
	}
	
	@Post("?hints=false")
	public String giveHints(String entity) throws ParseException, IOException{
		System.out.println("\nGIVING HINTS");
		JSONObject res;
		JSONParser parser;
		DatasetReader datasetReader;
		
		System.out.println("Starting classification.");
		res = new JSONObject();
		res.put("Campo", "valor");
		parser = new JSONParser();
		JSONObject req = (JSONObject) parser.parse(entity);
		System.out.println(req.toJSONString());
		datasetReader = new DatasetReader();
		Dataset dataset = datasetReader.loadJSONObject(req, null);
		modelHandler.refineHintsUnlabelledDataset(dataset);
		System.out.println(dataset.toJSONObject().toJSONString());
		return dataset.toJSONObject().toJSONString();
	}
	
	@Path("?hints=false")
	@Post("json")
	public String giveLabels(String entity) throws ParseException, IOException{
		System.out.println("\nGIVING LABELS");
		String res;
		JSONParser parser;
		DatasetReader datasetReader;
		
		System.out.println("Starting classification.");
		parser = new JSONParser();
		JSONObject req = (JSONObject) parser.parse(entity);
		System.out.println(req.toJSONString());
		datasetReader = new DatasetReader();
		Dataset dataset = datasetReader.loadJSONObject(req, null);
		modelHandler.refineHintsOnceBothClassifiers(dataset);
		System.out.println(dataset.toJSONObject().toJSONString());
		res = dataset.toJSONObject().toJSONString();
		return res;
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
		System.out.println("Clase inferida: "+slot.getHint());
		//System.out.println(String.format("Slot of class %s classified as %s", slot.getSlotClass(), slot.getHint()));
		
		if(slot.getSlotClass().equals(slot.getHint())) {
			correct++;
		} else {
			incorrect++;
		}
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				checkHints(child);
			}
		}
	}
}
