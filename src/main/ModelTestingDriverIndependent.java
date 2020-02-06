package main;
import java.util.Arrays;
import java.util.List;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import model.randomForest.ModelHandlerRandomForest;
import utils.ClockMonitor;
import utils.DatasetReader;

public class ModelTestingDriverIndependent {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		
		//MODEL APPLICATION
		List<Dataset> trainingDatasets;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String classifiersTablesRoot;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		int testingFoldNumber;
		Integer numberOfDomains;
		String datasetsPath;
		ModelHandlerRandomForest modelHandler;
		String resultsPath;
		
		clock = new ClockMonitor();
		
		//MODEL CREATION
		
		//MODEL APPLICATION
		modelHandler = new ModelHandlerRandomForest();
		
		domains = Arrays.copyOfRange(args, 4, args.length);
		numberOfDomains = domains.length;
		classifiersTablesRoot = args[0];
		resultsRoot = args[1];
		datasetsRoot = args[2];
		testingFoldNumber = Integer.valueOf(args[3]);
		
		modelHandler.setClassifiersRootFolder(String.format("%s/classifiersAndTables/modelClassifiers/%s-domains/fold-%s", classifiersTablesRoot, numberOfDomains, testingFoldNumber));
		modelHandler.setTablesRootFolder(String.format("%s/classifiersAndTables/modelTables/%s-domains/fold-%s", classifiersTablesRoot, numberOfDomains, testingFoldNumber));
		modelHandler.loadFeaturesCalculators();
		
		Dataset dataset = new Dataset();
		Attribute a = new Attribute();
		a.setName("test");
		a.setValue("http://www.ayala.tk");
		a.setSlotClass("None");
		dataset.addSlot(a);
		
		modelHandler.createNewContext();
		modelHandler.loadClassifiers(false);
		modelHandler.loadClassifiers(true);
		System.out.println("Starting testing");
		modelHandler.refineHintsUnlabelledDataset(dataset);
		checkHints(dataset);
		System.out.println(" ");
		clock.stop();

		modelHandler.refineHintsOnce(dataset);
		checkHints(dataset);
		modelHandler.closeContext();
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
