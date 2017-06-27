package featuresCalculation.features.attribute;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

public class TyperScore extends Feature<Attribute>{

	//Properties-----------------------------------------------------
	
	private String className;
	private String indexPath;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) throws IOException {
		assert className != null;

		this.className = className;
	}
	
	public void setIndexPath(String indexPath) throws IOException {
		assert indexPath != null;

		this.indexPath = indexPath;
	}
	
	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Attribute element) {
		assert element != null;
		assert className != null;
		
		FeatureValue result;
		String attributeValue;
		Path path;
		Directory directory;
		IndexReader indexReader;
		IndexSearcher indexSearcher;
		Analyzer analyzer;
		QueryBuilder queryBuilder;
		TopDocs topDocs;
		ScoreDoc[] scoreDocs;
		String queryText;
		Query query;
		String field;
		Document document;
		String attributeClass;
		double value;
		
		value = 0.0;
		
		try {
			attributeValue = element.getValue().toLowerCase().replaceAll("and", " ").replaceAll("or", " ").replaceAll("\\+", "").replaceAll("\\-", "");
			if(!(attributeValue.equals("") || attributeValue.equals(" "))) {
				path = Paths.get(indexPath);
				directory = FSDirectory.open(path);
				indexReader = DirectoryReader.open(directory);
				indexSearcher = new IndexSearcher(indexReader);
				analyzer = new UnicodeWhitespaceAnalyzer();
				//analyzer = new StandardAnalyzer();
				field = "value";
				queryText = attributeValue;
				queryBuilder = new SimpleQueryParser(analyzer, field);
				query = queryBuilder.createPhraseQuery(field, queryText);
				if(query==null){
					System.out.println(attributeValue);
					System.out.println("Something is wrong...");
				}
				topDocs = indexSearcher.search(query, indexReader.numDocs());
				scoreDocs = topDocs.scoreDocs;
				for (ScoreDoc scoreDoc : scoreDocs) {
					document = indexReader.document(scoreDoc.doc);
					attributeClass = document.get("attributeClass");
					if (attributeClass.equals(String.format("document-%s", className))) {
						value = scoreDoc.score;
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
		
		result = String.format("Typer score for class %s", className);
		
		return result;
	}
}
