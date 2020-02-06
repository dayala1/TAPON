package featuresCalculation.features.attribute;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import featuresCalculation.ClassesConfiguration;
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

	private String indexPath;

	private ClassesConfiguration classesConfiguration;

	public ClassesConfiguration getClassesConfiguration() {
		return classesConfiguration;
	}

	public void setClassesConfiguration(ClassesConfiguration classesConfiguration) {
		this.classesConfiguration = classesConfiguration;
	}

	public void setIndexPath(String indexPath) throws IOException {
		assert indexPath != null;

		this.indexPath = indexPath;
		Path path = Paths.get(indexPath);
		Directory directory = FSDirectory.open(path);
		indexReader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(indexReader);
		analyzer = new UnicodeWhitespaceAnalyzer();
		queryBuilder = new SimpleQueryParser(analyzer, "value");
	}

	private DirectoryReader indexReader;
	private IndexSearcher indexSearcher;
	private Analyzer analyzer;
	private QueryBuilder queryBuilder;


	//Interface methods----------------------------------------------
	
	@Override
	public FeatureValue apply(Attribute element) {
		assert element != null;
		assert classesConfiguration != null;
		
		FeatureValue result;
		String attributeValue;
		TopDocs topDocs;
		ScoreDoc[] scoreDocs;
		String queryText;
		Query query;
		Document document;
		String attributeClass;
		Map<String, Float> value;
		
		value = new TreeMap<>();
		for(String className:classesConfiguration.getAttributeClasses()){
			value.put(className, 0.0f);
		}
		
		try {
			attributeValue = element.getValue().toLowerCase().replaceAll("and", " ").replaceAll("or", " ").replaceAll("\\+", "").replaceAll("\\-", "");
			if(!(attributeValue.equals("") || attributeValue.equals(" "))) {

				//analyzer = new StandardAnalyzer();
				queryText = attributeValue;
				query = queryBuilder.createPhraseQuery("value", queryText);
				if(query==null){
					System.out.println(attributeValue);
					System.out.println("Something is wrong...");
				}
				topDocs = indexSearcher.search(query, indexReader.numDocs());
				scoreDocs = topDocs.scoreDocs;
				for (ScoreDoc scoreDoc : scoreDocs) {
					document = indexReader.document(scoreDoc.doc);
					attributeClass = document.get("attributeClass");
					if(attributeClass.startsWith("document-")) {
						attributeClass = attributeClass.substring(9);
						value.put(attributeClass, scoreDoc.score);
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
		result = "";
		for(String className:classesConfiguration.getAttributeClasses()){
			result += String.format("|Dynamic-Typer score for class %s", className);
		}
		result = result.substring(1);
		return result;
	}
}
