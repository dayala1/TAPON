package main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dataset.Attribute;
import dataset.Dataset;
import dataset.Record;
import dataset.Slot;
import jersey.repackaged.com.google.common.collect.Maps;
import scala.collection.mutable.HashSet;
import semanticTyper.LuceneBasedSTModelHandler;
import utils.DatasetReader;

public class SlotCountDriver {
	
	public static int countAttributes = 0;
	public static int countRecords = 0;
	public static int sumChildren = 0;
	public static int sumAttributeChildren = 0;
	public static int sumRecordChildren = 0;
	public static int numNumeric = 0;
	public static HashSet<String> uniqueValues = new HashSet<>();
	public static Map<String, Integer> numInstances = Maps.newHashMap();
	public static int depth = 0;
	public static int sumDepth = 0;
	public static void main(String[] args) throws Exception {

		LuceneBasedSTModelHandler typer;
		List<Dataset> datasets;
		DatasetReader datasetReader;
		String datasetsRoot;
		String[] domains;
		String datasetsPath;
		

		typer = new LuceneBasedSTModelHandler("1");
		typer.setModelHandlerEnabled(true);
		
		//MODEL APPLICATION
		datasetReader = new DatasetReader();
		datasets = new ArrayList<Dataset>();
		
		domains = Arrays.copyOfRange(args, 1, args.length);
		datasetsRoot = args[0];
		
		for (String domain : domains) {
			for (int i = 1; i < 3; i++) {
				datasetsPath = String.format("%s/Datasets/%s/%s",datasetsRoot, domain, i);
				datasetReader.addDataset(datasetsPath, 1.0, datasets);
			}
		}
		
		for (Dataset dataset : datasets) {
			checkHints(dataset);
		}
		System.out.println("Number of attributes: "+countAttributes);
		System.out.println("Number of records: "+countRecords);
		System.out.println("Average number of children: "+(sumChildren*1.0/countRecords));
		System.out.println("Average number of attribute children: "+(sumAttributeChildren*1.0/countRecords));
		System.out.println("Average number of record children: "+(sumRecordChildren*1.0/countRecords));
		System.out.println("Fraction of numeric attributes: "+(numNumeric*1.0/countAttributes));
		System.out.println("Unique attribute values: "+uniqueValues.size());
		System.out.println("Average depth: "+(sumDepth*1.0/datasets.size()));
		System.out.println("\nInstances occurrences:");
		for (Entry<String, Integer> entry : numInstances.entrySet()) {
			System.out.println(String.format("\t%s: %s", entry.getKey(), entry.getValue()));
		}
	}
	
	public static void checkHints(Dataset dataset) {
		depth = 0;
		assert dataset != null;
		List<Slot> children;
		
		children = dataset.getSlots();
		for (Slot child : children) {
			checkHints(child, 1);
		}
		System.out.println(depth);
		sumDepth+=depth;
	}
	
	public static void checkHints(Slot slot, int currentDepth) {
		assert slot != null;
		if(currentDepth>depth){
			depth=currentDepth;
		}
		
		numInstances.put(slot.getSlotClass(), numInstances.getOrDefault(slot.getSlotClass(), 0)+1);
		
		List<Slot> children;
		if(slot instanceof Attribute){
			countAttributes++;
			if(((Attribute)slot).getNumericValue()!=null){
				numNumeric++;
			}
			uniqueValues.add(((Attribute)slot).getValue());
		}else{
			countRecords++;
			children = ((Record)slot).getSlots();
			sumChildren += children.size();
			for (Slot child : children) {
				if(child instanceof Attribute){
					sumAttributeChildren++;
				}else{
					sumRecordChildren++;
				}
				checkHints(child, currentDepth+1);
			}
		}
	}
	
}
