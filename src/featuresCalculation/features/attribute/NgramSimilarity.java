package featuresCalculation.features.attribute;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import jersey.repackaged.com.google.common.collect.Sets;

public class NgramSimilarity extends Feature<Attribute>{
	
	public NgramSimilarity() {
		ngramsList = new ArrayList<Set<String>>();
	}
	
	//Properties-----------------------------------------------------
	
	private String className;
	private String indexPath;
	private Integer n;

	public String getClassName() {
		return className;
	}
	
	public void setIndexPath(String indexPath) throws IOException {
		assert indexPath != null;

		this.indexPath = indexPath;
	}

	public Integer getN() {
		return n;
	}

	public void setN(Integer n) {
		assert n != null;
	
		this.n = n;
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
		Set<String> ngrams;

		ngramsList.clear();
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
			if(attributeValue.length() > n) {
				ngrams = Sets.newHashSet();
				for (int i = 0; i <= attributeValue.length() - n; i++) {
					ngrams.add(attributeValue.substring(i, i + n).toLowerCase());
				}
				ngramsList.add(ngrams);
			}
		}
		indexReader.close();
		this.className = className;
	}

	//Internal state-------------------------------------------------

	private List<Set<String>> ngramsList;
	
	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Attribute element) {
		assert element != null;
		assert className != null;
		
		FeatureValue result;
		Set<String> ngrams1;
		double mean;
		double size;
		String attributeValue1;
		int length1;
		double matches;
		double ngramSimilarity;
		double delta;
		double value;
		
		size = 0.0;
		mean = 0.0;
		ngrams1 = Sets.newHashSet();
		attributeValue1 = element.getValue();
		length1 = attributeValue1.length();
		for (int i = 0; i <= length1 - n; i++) {
			ngrams1.add(attributeValue1.substring(i, i + n).toLowerCase());
		}
		if(!ngramsList.isEmpty() && !ngrams1.isEmpty()) {
			for (Set<String> ngrams2 : ngramsList) {
				matches = 0.0;
				for (String ngram : ngrams1) {
					if (ngrams2.contains(ngram)) {
						matches++;
					}
				}
				ngramSimilarity = 2 * matches / (ngrams1.size() + ngrams2.size());
				size = size + 1;
				delta = ngramSimilarity - mean;
				mean = mean + delta / size;
			}
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
		
		result = String.format("Dynamic-Average %s-gram similarity for class %s",n, className);
		
		return result;
	}
}
