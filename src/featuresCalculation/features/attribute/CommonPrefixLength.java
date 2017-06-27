package featuresCalculation.features.attribute;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

import dataset.Attribute;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;

public class CommonPrefixLength extends Feature<Attribute>{
	
	public CommonPrefixLength() {
		attributeValues = new ArrayList<String>();
	}
	
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
		String attributeValue;
		
		attributeValues.clear();
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
		for (ScoreDoc scoreDoc : scoreDocs) {
			document = indexReader.document(scoreDoc.doc);
			attributeValue = document.get("value");
			attributeValues.add(attributeValue);
		}
		indexReader.close();
		this.className = className;
	}

	//Internal state-------------------------------------------------
	
	private List<String> attributeValues;
	
	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Attribute element) {
		assert element != null;
		assert className != null;
		
		FeatureValue result;
		double mean;
		double size;
		String attributeValue1;
		int length1;
		int length2;
		int minLength;
		int i;
		double commonPrefixLength;
		double delta;
		double value;
		
		size = 0.0;
		mean = 0.0;
		
		attributeValue1 = element.getValue();
		for (String attributeValue2 : attributeValues) {
			length1 = attributeValue1.length();
			length2 = attributeValue2.length();
			minLength = Math.min(length1, length2);
			i = 0;
			commonPrefixLength = 0.0;
			while(i<minLength && attributeValue1.charAt(i) == attributeValue2.charAt(i)) {
				commonPrefixLength = commonPrefixLength + 1;
				i = i + 1;
			}
			size = size + 1;
			delta = commonPrefixLength - mean;
			mean = mean + delta / size;
		}
		
		value = mean;
		
		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(element);
		updateObservers(result);
		
		return result;
	}
	
	public String toString() {
		String result;
		
		result = String.format("Average common prefix length for class %s", className);
		
		return result;
	}
}
