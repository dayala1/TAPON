package featuresCalculation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import dataset.*;
import edu.stanford.nlp.maxent.Features;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.UnicodeWhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.util.Version;
import org.json.simple.parser.ParseException;

import featuresCalculation.featureGroups.dataset.NumberOfSlotsDatasetGroup;
import featuresCalculation.featureGroups.dataset.StatisticsGroup;
import observer.Observable;
import observer.Observer;
import org.spark_project.guava.collect.Lists;
import utils.ReflectionUtils;

public class DatasetFeaturesCalculator extends Observable<Featurable> implements Serializable {

	private static final long serialVersionUID = 3074873363325220627L;

	// Constructors---------------------------------------------------

	public DatasetFeaturesCalculator() {
		super();
		this.numberOfFeatures = 0;
		this.classesConfiguration = new ClassesConfiguration();
		this.tablesMap = new HashMap<String, List<FeaturesTable>>();
		this.featurables = new ArrayList<Featurable>();
		this.builder = new FeaturesTablesBuilder();
		this.incrementalFeaturesGroups = new HashSet<FeaturesGroup>();
		this.up = false;
		this.down = false;
		this.updown = false;
		this.downup = false;
		this.firstIteration = false;
		this.secondIteration = false;
		this.thirdIteration = false;
	}

	// Properties-----------------------------------------------------

	private Dataset dataset;
	private ClassesConfiguration classesConfiguration;
	private Set<FeaturesGroup> slotFeaturesGroups;
	private Set<FeaturesGroup> featurableFeaturesGroups;
	private Set<FeaturesGroup> incrementalFeaturesGroups;
	private Set<FeaturesGroup> classesPairsFeaturesGroups;
	private String indexPath;
	private Integer numberOfFeatures;

	private Map<String, List<FeaturesTable>> tablesMap;

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		assert dataset != null;
		this.dataset = dataset;
	}

	public ClassesConfiguration getClassesConfiguration() {
		return this.classesConfiguration;
	}

	public void setClassesConfiguration(ClassesConfiguration classesConfiguration) {
		assert classesConfiguration != null;

		this.classesConfiguration = classesConfiguration;
	}

	public Set<FeaturesGroup> getSlotFeaturesGroups() {
		return slotFeaturesGroups;
	}

	public void setSlotFeaturesGroups(Set<FeaturesGroup> slotFeaturesGroups) {
		assert slotFeaturesGroups != null;
		assert !slotFeaturesGroups.isEmpty();

		this.slotFeaturesGroups = slotFeaturesGroups;
	}

	public Set<FeaturesGroup> getFeaturableFeaturesGroups() {
		return featurableFeaturesGroups;
	}

	public void setFeaturableFeaturesGroups(Set<FeaturesGroup> featurableFeaturesGroups) {
		assert featurableFeaturesGroups != null;
		assert !featurableFeaturesGroups.isEmpty();

		this.featurableFeaturesGroups = featurableFeaturesGroups;
	}

	public Set<FeaturesGroup> getClassesPairsFeaturesGroups() {
		return classesPairsFeaturesGroups;
	}

	public void setClassesPairsFeaturesGroups(Set<FeaturesGroup> classesPairsFeaturesGroups) {
		assert classesPairsFeaturesGroups != null;
		assert !classesPairsFeaturesGroups.isEmpty();

		this.classesPairsFeaturesGroups = classesPairsFeaturesGroups;
	}

	public Set<FeaturesGroup> getIncrementalFeaturesGroups() {
		return incrementalFeaturesGroups;
	}

	public void setIncrementalFeaturesGroups(Set<FeaturesGroup> incrementalFeaturesGroups) {
		assert incrementalFeaturesGroups != null;

		this.incrementalFeaturesGroups = incrementalFeaturesGroups;
	}

	public Map<String, List<FeaturesTable>> getTablesMap() {
		Map<String, List<FeaturesTable>> result;

		result = Collections.unmodifiableMap(tablesMap);

		return result;
	}

	public String getIndexPath() {
		return indexPath;
	}

	public void setIndexPath(String indexPath) {
		assert indexPath != null;

		this.indexPath = indexPath;
	}

	public Integer getNumberOfFeatures() {
		return numberOfFeatures;
	}

	// Internal state-------------------------------------------------

	private List<Featurable> featurables;
	private FeaturesTablesBuilder builder;
	private boolean up, down, updown, downup;
	private boolean firstIteration, secondIteration, thirdIteration;
	private Record recordExample;
	private Attribute attributeExample;

	protected void addFeaturable(Featurable featurable) {
		assert featurable != null;

		featurables.add(featurable);
	}

	// Interface methods----------------------------------------------

	public void initializeClasses(Dataset dataset) throws IOException {
		assert dataset != null;

		updateClasses(dataset);
	}

	public void initializeClasses(Collection<Dataset> datasets) throws IOException {
		assert datasets != null;

		IndexWriterConfig indexWriterConfig;
		IndexWriter indexWriter;
		Queue<Slot> queue;
		List<Slot> children;
		Slot slot;
		String slotClass;
		String value;
		Document document;
		Field field;
		List<String> examples;
		Analyzer analyzer;
		Directory directory;
		Path path;
		Map<String, List<String>> accumulatedExamples;
		StringBuilder sb;
		Map<String, Integer> numericExamples;
		Map<String, Integer> textualExamples;
		Map<String, List<String>> values;

		accumulatedExamples = new HashMap<String, List<String>>();
		path = Paths.get(getIndexPath());
		directory = FSDirectory.open(path);
		// analyzer = new UnicodeWhitespaceAnalyzer();
		analyzer = new UnicodeWhitespaceAnalyzer();
		analyzer.setVersion(Version.LUCENE_5_4_1);
		indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriter = new IndexWriter(directory, indexWriterConfig);
		numericExamples = new HashMap<>();
		textualExamples = new HashMap<>();
		values = new HashMap<>();
		for (Dataset dataset : datasets) {
			queue = new ArrayDeque<Slot>();
			children = dataset.getSlots();
			queue.addAll(children);
			while (!queue.isEmpty()) {
				slot = queue.remove();
				slotClass = slot.getSlotClass();
				if(!slotClass.isEmpty()) {
					if (slot instanceof Attribute) {
						value = ((Attribute) slot).getValue();
						if (!values.containsKey(slot.getSlotClass())) {
							values.put(slot.getSlotClass(), new ArrayList<>());
						}
						values.get(slot.getSlotClass()).add(value);
						if (((Attribute) slot).getNumericValue() != null) {
							numericExamples.put(slotClass, numericExamples.getOrDefault(slotClass, 0) + 1);
						} else {
							textualExamples.put(slotClass, textualExamples.getOrDefault(slotClass, 0) + 1);
						}
						classesConfiguration.addAttributeClass(slotClass);
						document = new Document();
						field = new StringField("attributeClass", slotClass, Store.YES);
						document.add(field);
						field = new TextField("value", value, Store.YES);
						document.add(field);
						indexWriter.addDocument(document);

						value = value.toLowerCase().replaceAll(" and ", " ").replaceAll(" or ", " ");
						if (accumulatedExamples.containsKey(slotClass)) {
							examples = accumulatedExamples.get(slotClass);
						} else {
							examples = new ArrayList<String>();
							accumulatedExamples.put(slotClass, examples);
						}
						examples.add(value);
						System.out.println("Stored an example");
					} else {
						children = ((Record) slot).getSlots();
						queue.addAll(children);
						classesConfiguration.addRecordClass(slotClass);
					}
				}
			}
		}

		classesConfiguration.setValues(values);

		for (String sC :
				classesConfiguration.getAttributeClasses()) {
				int numNumeric = numericExamples.getOrDefault(sC, 0);
				int numTextual = textualExamples.getOrDefault(sC, 0);
				if( 1.0*numNumeric / (numNumeric + numTextual)  > 0.80 ) {
					classesConfiguration.setIsNumeric(sC, true);
					System.out.println(String.format("Numeric class: %s", sC));

				} else {
					classesConfiguration.setIsNumeric(sC, false);
					System.out.println(String.format("Textual class: %s", sC));
				}
		}

		for (String attributeClass : classesConfiguration.getAttributeClasses()) {
			document = new Document();
			field = new StringField("attributeClass", String.format("document-%s", attributeClass), Store.YES);
			document.add(field);
			sb = new StringBuilder();
			try {
				for (String example : accumulatedExamples.get(attributeClass)) {
					sb.append(example);
					sb.append(' ');
				}
			} catch (Exception e) {
				System.out.println("Error aqui");
			}
			field = new TextField("value", sb.toString(), Store.YES);
			document.add(field);
			indexWriter.addDocument(document);
		}
		indexWriter.commit();
		indexWriter.close();
		directory.close();
	}

	public void initialize(boolean applyIncrementalFeatures) throws IOException {
		assert slotFeaturesGroups != null;
		assert featurableFeaturesGroups != null;
		assert dataset != null;

		// Features groups that correspond to incremental features
		StatisticsGroup statisticsGroup;
		NumberOfSlotsDatasetGroup slotsCountGroup;

		statisticsGroup = null;
		slotsCountGroup = null;
		if (applyIncrementalFeatures) {
			statisticsGroup = new StatisticsGroup();
			slotsCountGroup = new NumberOfSlotsDatasetGroup();
			// The DatasetFeaturesCalculator is observed by ONLY the slots count
			// incremental feature
			addObserver(slotsCountGroup);
		}

		// Initialisation of slot features groups, adding them to the
		// statistical group.
		for (FeaturesGroup featuresGroup : slotFeaturesGroups) {
			featuresGroup.setClassesConfiguration(classesConfiguration);
			featuresGroup.setIndexPath(getIndexPath());
			featuresGroup.initialize();
			numberOfFeatures += featuresGroup.getNumberOfFeatures();
			if (applyIncrementalFeatures) {
				statisticsGroup.addMeasuredFeatures(featuresGroup);
			}
			// We update what kinds of iterations are needed when applying the
			// groups
			updateIterations(featuresGroup);
		}

		// Initialisation of featurable features (applied to ALL featurables,
		// including the dataset).
		for (FeaturesGroup featuresGroup : featurableFeaturesGroups) {
			featuresGroup.setClassesConfiguration(classesConfiguration);
			featuresGroup.initialize();
			numberOfFeatures += featuresGroup.getNumberOfFeatures();
			if (applyIncrementalFeatures) {
				statisticsGroup.addMeasuredFeatures(featuresGroup);
			}
			updateIterations(featuresGroup);
		}

		//Initialisation of classes pairs features
		for (FeaturesGroup featuresGroup : classesPairsFeaturesGroups){
			featuresGroup.setClassesConfiguration(classesConfiguration);
			featuresGroup.initialize();
		}

		if (applyIncrementalFeatures) {
			incrementalFeaturesGroups.add(statisticsGroup);
			incrementalFeaturesGroups.add(slotsCountGroup);

			// Initialisation of incremental features, AFTER the other features
			// have been added to the statistical group.
			for (FeaturesGroup featuresGroup : incrementalFeaturesGroups) {
				featuresGroup.setClassesConfiguration(classesConfiguration);
				featuresGroup.initialize();
				updateIterations(featuresGroup);
			}
		}

		// Once all features to be applied are initialised, we decide how any
		// iterations we need.
		updateRequiredIterations();

	}

	public void store(String objectPath) throws IOException {
		try {
			FileOutputStream fileOutputStream;
			ObjectOutputStream objectOutputStream;

			fileOutputStream = new FileOutputStream(objectPath);
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(this);
			objectOutputStream.close();
		}catch (IOException e){
			System.out.println("Problem while trying to store the features calculator: ");
			e.printStackTrace();
			throw e;
		}
	}

	//TODO terminar caculo de feat. de pares
	public void computeFeaturesClassesPairs(String outputFolder, boolean createTables) throws IOException, ParseException {
		List<String> attributeClasses;
		String class1;
		String class2;
		ClassesPair classesPair;

		Set<Feature<?>> features = new HashSet<>();
		for(FeaturesGroup featuresGroup : classesPairsFeaturesGroups){
			features.addAll(featuresGroup.getFeatures());
		}

		attributeClasses = Lists.newArrayList(classesConfiguration.getAttributeClasses());
		if(createTables) {
			builder.initialize(outputFolder, null, null, null, features);
		}

		for (int i = 0; i < attributeClasses.size(); i++) {
			for (int j = i + 1; j < attributeClasses.size(); j++) {
				class1 = attributeClasses.get(i);
				class2 = attributeClasses.get(j);
				classesPair = new ClassesPair();
				classesPair.setClass1(class1);
				classesPair.setClass2(class2);

				FeaturesVector featuresVector = new FeaturesVector();
				System.out.println(classesPairsFeaturesGroups);
				for (FeaturesGroup featuresGroup : classesPairsFeaturesGroups) {
					FeaturesVector partialFeaturesVector = featuresGroup.apply(classesPair);
					for (FeatureValue featureValue : partialFeaturesVector.getFeatureValues().values()) {
						featuresVector.addFeatureValue(featureValue);
					}
				}

				builder.addVector(classesPair, featuresVector, outputFolder);
			}
		}
	}

	public void run(String outputFolder, boolean createTables) throws IOException, ParseException {
		assert dataset != null;

		Set<Feature<?>> datasetFeatures;
		Set<Feature<?>> attributeFeatures;
		Set<Feature<?>> recordFeatures;

		featurables.clear();
		featurables.add(dataset);
		featurables.addAll(getSlots());

		List<Featurable> datasetList;
		FeaturesVector featuresVector;
		int iterationIndex;

		removeFeatureValues(featurables, slotFeaturesGroups);
		removeFeatureValues(featurables, featurableFeaturesGroups);

		datasetList = new ArrayList<Featurable>();
		datasetList.add(dataset);

		// Initialization of the structures where feature values will be stored

		if (dataset.getFeaturesVector() == null) {
			featuresVector = new FeaturesVector();
			featuresVector.setFeaturable(dataset);
		}

		// Iterations where feature values are obtained. They can be up-down or
		// bottom-up
		if (firstIteration) {
			iterationIndex = 1;
			iterateOnce(iterationIndex, datasetList);
		}

		if (secondIteration) {
			iterationIndex = 2;
			iterateOnce(iterationIndex, datasetList);
		}

		if (thirdIteration) {
			iterationIndex = 3;
			iterateOnce(iterationIndex, datasetList);
		}

		// The features have already been computed. We store them using the
		// builder.
		for (Featurable featurable : featurables) {
			if (featurable instanceof Record) {
				recordExample = (Record) featurable;
			}
			if (featurable instanceof Attribute) {
				attributeExample = (Attribute) featurable;
				break;
			}
		}
		datasetFeatures = dataset.getFeaturesVector().getFeatureValues().keySet();
		if (recordExample != null) {
			recordFeatures = recordExample.getFeaturesVector().getFeatureValues().keySet();
		} else {
			recordFeatures = new HashSet<Feature<?>>();
		}
		attributeFeatures = attributeExample.getFeaturesVector().getFeatureValues().keySet();
		if (createTables) {
			builder.initialize(outputFolder, datasetFeatures, recordFeatures, attributeFeatures, null);
			for (Featurable featurable : featurables) {
				//System.out.println("\n" + featurable);
				/*
				 * for(FeatureValue featureValue :
				 * featurable.getFeaturesVector().getFeatureValues().values()){
				 * System.out.println(featureValue.getFeature()+": "
				 * +featureValue.getValue()); }
				 */
				builder.addVector(featurable, featurable.getFeaturesVector(), outputFolder);
			}
		}

		// We store the tables. If we are only interested in the csv files, this
		// part can be removed.
		// tablesMap = builder.getTablesMap();

	}

	public void updateObservers(Featurable info) {
		assert info != null;

		if (info instanceof Slot) {
			for (Observer<Featurable> observer : getObservers()) {
				observer.update(info);
			}
		}

	}

	public void closeTablesBuilder() throws IOException {
		builder.closeWriters();
	}

	// Ancillary methods----------------------------------------------

	// Uses the examples to obtain the known classes. Also stores the examples.
	protected void updateClasses(Dataset dataset) throws IOException {
		IndexReader indexReader;
		IndexSearcher indexSearcher;
		QueryBuilder queryBuilder;
		TopDocs topDocs;
		ScoreDoc[] scoreDocs;
		String queryText;
		Query query;
		String documentText;
		IndexWriterConfig indexWriterConfig;
		IndexWriter indexWriter;
		Queue<Slot> queue;
		List<Slot> children;
		Slot slot;
		String slotClass;
		String value;
		Document document;
		Field field;
		List<String> examples;
		Analyzer analyzer;
		Directory directory;
		Path path;

		path = Paths.get(getIndexPath());

		directory = FSDirectory.open(path);
		// analyzer = new UnicodeWhitespaceAnalyzer();
		analyzer = new UnicodeWhitespaceAnalyzer();
		analyzer.setVersion(Version.LUCENE_5_4_1);
		indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriter = new IndexWriter(directory, indexWriterConfig);
		indexReader = null;
		indexSearcher = null;
		queue = new ArrayDeque<Slot>();
		children = dataset.getSlots();
		queue.addAll(children);
		while (!queue.isEmpty()) {
			slot = queue.remove();
			slotClass = slot.getSlotClass();
			if(!slotClass.isEmpty()) {
				if (slot instanceof Attribute) {
					value = ((Attribute) slot).getValue();
					classesConfiguration.addAttributeClass(slotClass);
					document = new Document();
					field = new StringField("attributeClass", slotClass, Store.YES);
					document.add(field);
					field = new TextField("value", value, Store.YES);
					document.add(field);
					indexWriter.addDocument(document);
					indexWriter.commit();

					if (indexReader == null) {
						indexReader = DirectoryReader.open(directory);
						indexSearcher = new IndexSearcher(indexReader);
					}
					value = value.toLowerCase().replaceAll(" and ", " ").replaceAll(" or ", " ");
					queryText = String.format("document-%s", slotClass);
					queryBuilder = new SimpleQueryParser(analyzer, "attributeClass");
					query = queryBuilder.createBooleanQuery("attributeClass", queryText);
					topDocs = indexSearcher.search(query, 1);
					scoreDocs = topDocs.scoreDocs;
					if (scoreDocs.length > 0) {
						document = indexReader.document(scoreDocs[0].doc);
						documentText = document.get("value");
						documentText = documentText.concat(String.format(" %s", value));
					} else {
						documentText = value;
					}
					document = new Document();
					field = new StringField("attributeClass", queryText, Store.YES);
					document.add(field);
					field = new TextField("value", documentText, Store.YES);
					document.add(field);
					indexWriter.updateDocument(new Term("attributeClass", queryText), document);
					indexWriter.commit();
					System.out.println("Stored an example");
				} else {
					children = ((Record) slot).getSlots();
					queue.addAll(children);
					classesConfiguration.addRecordClass(slotClass);
					classesConfiguration.addRecordClass(slotClass);
				}
			}
		}
		indexWriter.close();
		directory.close();
	}

	protected List<Slot> getSlots() {
		List<Slot> result;
		Queue<Slot> queue;
		List<Slot> children;
		Slot slot;
		FeaturesVector featuresVector;

		result = new LinkedList<Slot>();
		queue = new ArrayDeque<Slot>();
		children = dataset.getSlots();
		queue.addAll(children);
		while (!queue.isEmpty()) {
			slot = queue.remove();
			if (slot instanceof Record) {
				children = ((Record) slot).getSlots();
				queue.addAll(children);
			}
			result.add(slot);
			if (slot.getFeaturesVector() == null) {
				featuresVector = new FeaturesVector();
				featuresVector.setFeaturable(slot);
			}
		}

		return result;
	}

	// Applies given features in an iteration
	protected void iterate(List<Featurable> featurables, boolean updown, Set<FeaturesGroup> featuresGroups) {
		int featurablesSize;
		Featurable featurable;
		FeaturesVector featuresVector;
		FeaturesVector partialFeaturesVector;
		Class<?> featuresGroupParameter;
		Class<?> featurableClass;

		featurablesSize = featurables.size();

		for (int i = startIndex(updown, featurablesSize); endCondition(updown, i,
				featurablesSize); i = updateIndex(updown, i)) {
			featurable = featurables.get(i);
			featuresVector = featurable.getFeaturesVector();
			updateObservers(featurable);

			// Features calculation
			for (FeaturesGroup featuresGroup : featuresGroups) {
				featuresGroupParameter = (Class) ReflectionUtils.getParameterClass(featuresGroup);
				featurableClass = featurable.getClass();
				if (featuresGroupParameter.isAssignableFrom(featurableClass)) {
					partialFeaturesVector = featuresGroup.apply(featurable);
					for (FeatureValue featureValue : partialFeaturesVector.getFeatureValues().values()) {
						featuresVector.addFeatureValue(featureValue);
					}
				}
			}
		}
	}

	protected void removeFeatureValues(List<Featurable> featurables, Set<FeaturesGroup> featuresGroups) {
		assert featurables != null;
		assert featuresGroups != null;

		Set<Feature> features;
		FeaturesVector featuresVector;

		for (Featurable featurable : featurables) {
			for (FeaturesGroup featuresGroup : featuresGroups) {
				features = featuresGroup.getFeatures();
				for (Feature feature : features) {
					featuresVector = featurable.getFeaturesVector();
					if (featuresVector != null) {
						featuresVector.removeFeatureValues(feature);
					}
				}
			}
		}
	}

	protected int startIndex(boolean updown, int featurablesSize) {
		int result;

		if (updown) {
			result = 0;
		} else {
			result = featurablesSize - 1;
		}

		return result;
	}

	protected boolean endCondition(boolean updown, int index, int featurablesSize) {
		boolean result;

		if (updown) {
			result = index < featurablesSize;
		} else {
			result = index >= 0;
		}

		return result;
	}

	protected int updateIndex(boolean updown, int index) {
		int result;

		if (updown) {
			result = index + 1;
		} else {
			result = index - 1;
		}

		return result;
	}

	protected void updateIterations(FeaturesGroup featuresGroup) {
		assert featuresGroup != null;

		switch (featuresGroup.getIterationType()) {
		case DOWN:
			down = true;
			break;
		case UP:
			up = true;
			break;
		case UP_DOWN:
			updown = true;
			break;
		case DOWN_UP:
			downup = true;
			break;
		default:
			break;
		}
	}

	protected void updateRequiredIterations() {
		if (updown || (up && !downup)) {
			firstIteration = true;
		}
		if (down || updown || downup) {
			secondIteration = true;
		}
		if (downup || up) {
			thirdIteration = true;
		}
	}

	protected boolean isFirstIteration(FeaturesGroup<?> featuresGroup) {
		assert featuresGroup != null;

		boolean result;
		IterationType iterationType;

		iterationType = featuresGroup.getIterationType();

		result = iterationType == IterationType.UP_DOWN || (iterationType == IterationType.UP && !thirdIteration);

		return result;
	}

	protected boolean isSecondIteration(FeaturesGroup<?> featuresGroup) {
		assert featuresGroup != null;

		boolean result;
		IterationType iterationType;

		iterationType = featuresGroup.getIterationType();

		result = iterationType == IterationType.UP_DOWN || iterationType == IterationType.DOWN_UP
				|| iterationType == IterationType.DOWN;

		return result;
	}

	protected boolean isThirdIteration(FeaturesGroup<?> featuresGroup) {
		assert featuresGroup != null;

		boolean result;
		IterationType iterationType;

		iterationType = featuresGroup.getIterationType();

		result = iterationType == IterationType.DOWN_UP || (iterationType == IterationType.UP && thirdIteration);

		return result;
	}

	// Iterates once, applying the correct features groups
	protected void iterateOnce(int iterationIndex, List<Featurable> datasetList) {
		assert iterationIndex >= 1;
		assert iterationIndex <= 3;
		assert datasetList != null;
		assert slotFeaturesGroups != null;
		assert featurableFeaturesGroups != null;
		assert incrementalFeaturesGroups != null;

		Set<FeaturesGroup> slotFeaturesGroups;
		Set<FeaturesGroup> featurableFeaturesGroups;
		Set<FeaturesGroup> incrementalFeaturesGroups;

		slotFeaturesGroups = new HashSet<FeaturesGroup>();
		featurableFeaturesGroups = new HashSet<FeaturesGroup>();
		incrementalFeaturesGroups = new HashSet<FeaturesGroup>();

		for (FeaturesGroup featuresGroup : this.slotFeaturesGroups) {
			if (iterationCondition(iterationIndex, featuresGroup)) {
				slotFeaturesGroups.add(featuresGroup);
			}
		}
		for (FeaturesGroup featuresGroup : this.featurableFeaturesGroups) {
			if (iterationCondition(iterationIndex, featuresGroup)) {
				featurableFeaturesGroups.add(featuresGroup);
			}
		}
		for (FeaturesGroup featuresGroup : this.incrementalFeaturesGroups) {
			if (iterationCondition(iterationIndex, featuresGroup)) {
				incrementalFeaturesGroups.add(featuresGroup);
			}
		}

		iterate(featurables, isUpDown(iterationIndex), slotFeaturesGroups);
		iterate(featurables, isUpDown(iterationIndex), featurableFeaturesGroups);
		iterate(datasetList, isUpDown(iterationIndex), incrementalFeaturesGroups);

	}

	protected boolean iterationCondition(int iterationIndex, FeaturesGroup<?> featuresGroup) {
		assert iterationIndex >= 1;
		assert iterationIndex <= 3;
		assert featuresGroup != null;

		boolean result;

		switch (iterationIndex) {
		case 1:
			result = isFirstIteration(featuresGroup);
			break;
		case 2:
			result = isSecondIteration(featuresGroup);
			break;
		case 3:
			result = isThirdIteration(featuresGroup);
			break;

		default:
			result = false;
			break;
		}

		return result;
	}

	public boolean isUpDown(int iterationIndex) {
		boolean result;

		switch (iterationIndex) {
		case 1:
			result = false;
			break;
		case 2:
			result = true;
			break;
		case 3:
			result = false;
			break;

		default:
			result = false;
			break;
		}

		return result;
	}
}