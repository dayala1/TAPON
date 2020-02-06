package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import semanticTyper.LuceneBasedSTModelHandler;
import utils.ClockMonitor;
import utils.DatasetReader;
import utils.FileUtilsCust;

public class LermanInputBuilderDriver {
	
	public static double incorrect = 0.0;
	public static double correct = 0.0;
	public static Integer folderCount = 0;

	public static void main(String[] args) throws Exception {
		ClockMonitor clock;
		
		//MODEL APPLICATION
		LuceneBasedSTModelHandler typer;
		List<Dataset> trainingDatasets;
		List<Dataset> testingDatasets;
		DatasetReader datasetReader;
		String resultsRoot;
		String datasetsRoot;
		String[] domains;
		int testingFoldNumber;
		String datasetsPath;
		String resultsPath;
		
		clock = new ClockMonitor();
		typer = new LuceneBasedSTModelHandler("1");
		typer.setModelHandlerEnabled(true);
		
		//MODEL APPLICATION
		datasetReader = new DatasetReader();
		trainingDatasets = new ArrayList<Dataset>();
		testingDatasets = new ArrayList<Dataset>();
		
		domains = Arrays.copyOfRange(args, 4, args.length);
		resultsRoot = args[1];
		datasetsRoot = args[2];
		testingFoldNumber = Integer.valueOf(args[3]);
		
		for (String domain : domains) {
			for (int i = 1; i < 11; i++) {
				datasetsPath = String.format("%s/Datasets/%s/%s",datasetsRoot, domain, i);
				if (i == testingFoldNumber) {
					datasetReader.addDataset(datasetsPath, 1.0, trainingDatasets);
				} else {
					datasetReader.addDataset(datasetsPath, 1.0, testingDatasets);
				}
			}
		}
		resultsPath = String.format("%s/results", resultsRoot);
		String outputPath = String.format("%s/examples.csv", resultsPath);
		FileUtilsCust.createCSV(outputPath);
		clock.start();
		for (Dataset trainingDataset : trainingDatasets) {
			indexExamples(trainingDataset, outputPath);
		}
	}
	
	
	public static void indexExamples(Dataset dataset, String outputPath) {
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			indexExamples(child, outputPath);
		}
	}
	
	public static void indexExamples(Slot slot, String outputPath) {
		assert slot != null;
		List<Slot> children;
		String slotClass;
		String textualValue;
		String line;
		
		//System.out.println(String.format("Slot of class %s classified as %s", slot.getSlotClass(), slot.getHint()));
		
		if (slot instanceof Record) {
			children = ((Record)slot).getSlots();
			for (Slot child : children) {
				indexExamples(child, outputPath);
			}
		}else{
			slotClass = slot.getSlotClass();
			textualValue = ((Attribute)slot).getValue().replaceAll("(\\r|\\n)", " ");
			line = String.format("%s=====%s", textualValue, slotClass);
			FileUtilsCust.addLine(outputPath, line);
		}
	}
	
}
