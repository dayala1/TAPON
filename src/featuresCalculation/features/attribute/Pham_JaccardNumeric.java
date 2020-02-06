package featuresCalculation.features.attribute;

import com.google.common.primitives.Doubles;
import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.UnicodeWhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Pham_JaccardNumeric extends Feature<Attribute>{
	
	//Properties-----------------------------------------------------
	
	private String className;
	private String indexPath;

	public String getClassName() {
		return className;
	}
	
	public void setIndexPath(String indexPath) throws IOException {
		assert indexPath != null;

		this.indexPath = indexPath;
	}

	public void setClassName(String className) throws IOException {
		assert className != null;
		
		Path path;
		Directory directory;
		IndexReader indexReader;
		IndexSearcher indexSearcher;
		Analyzer analyzer;
		String field;
		String queryText;
		Query query;
		QueryBuilder queryBuilder;
		TopDocs topDocs;
		ScoreDoc[] scoreDocs;
		Document document;
		Double attributeValue;
		minValue = Double.MAX_VALUE;
		maxValue = Double.MIN_VALUE;
		path = Paths.get(indexPath);
		directory = FSDirectory.open(path);
		indexReader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(indexReader);
		analyzer = new UnicodeWhitespaceAnalyzer();
		field = "attributeClass";
		queryText = className;
		queryBuilder = new SimpleQueryParser(analyzer, field);
		query = queryBuilder.createBooleanQuery(field, queryText);
		topDocs = indexSearcher.search(query, indexReader.numDocs());
		scoreDocs = topDocs.scoreDocs;
		List<Double> values = new ArrayList<>();
		for (ScoreDoc scoreDoc : scoreDocs) {
			document = indexReader.document(scoreDoc.doc);
			try {
				attributeValue = Double.valueOf(document.get("value"));
				values.add(attributeValue);
			} catch (Exception e){
				System.out.println(String.format("Non numeric attribute in numeric class: %s in class %s", document.get("value"), className));
			}
		}
		//To avoid outliers, we only consider data between the 1st and 3rd quartiles
		Double firstQuartile = new Percentile().evaluate(Doubles.toArray(values), 25.0);
		Double thirdQuartile = new Percentile().evaluate(Doubles.toArray(values), 75.0);
		for (Double value:
			 values) {
			if (value >= firstQuartile && value <= thirdQuartile){
				if(value < minValue){
					minValue = value;
				}
				if (value > maxValue){
					maxValue = value;
				}
			}
		}
		indexReader.close();
		this.className = className;
	}

	//Internal state-------------------------------------------------
	
	private Double minValue;
	private Double maxValue;
	
	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Attribute element) {
		assert element != null;
		assert className != null;
		
		FeatureValue result;
		Double attributeValue;
		double value;

		value = -1.0;

		attributeValue = element.getNumericValue();
		if(attributeValue != null) {
			value = Math.max(0.0,
					(Math.min(maxValue, attributeValue) - Math.max(minValue, attributeValue)) / (Math.max(maxValue, attributeValue) - Math.min(minValue, attributeValue))
			);
			if(Double.isNaN(value)){
				value = 1.0;
			}
		}
		
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(element);
		updateObservers(result);
		
		return result;
	}
	
	public String toString() {
		String result;
		
		result = String.format("Numeric Jaccard similarity for class %s", className);
		
		return result;
	}
}
