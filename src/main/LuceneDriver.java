package main;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.UnicodeWhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.simple.SimpleQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

public class LuceneDriver {

	public static void main(String[] args) throws IOException, ParseException {
		Path path;
		Directory directory;
		IndexReader indexReader;
		IndexSearcher indexSearcher;
		Analyzer analyzer;
		String field;
		String queryText;
		Query query;
		QueryBuilder queryBuilder;
		QueryParser queryParser;
		TopDocs topDocs;
		ScoreDoc[] scoreDocs;
		Document document;
		String attributeValue;
		
		path = Paths.get("C:/Users/Boss/Documents/NgongaTesting/classifiersAndTables/modelTables/2-domains/fold-1/index");
		directory = FSDirectory.open(path);
		indexReader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(indexReader);
		analyzer = new UnicodeWhitespaceAnalyzer();
		queryText = String.format("document-%s", "dunsNumber");
		queryBuilder = new SimpleQueryParser(analyzer, "attributeClass");
		query = queryBuilder.createBooleanQuery("attributeClass", queryText);
		//query.add(catQuery2, Occur.MUST);
		System.out.println(query);
		topDocs = indexSearcher.search(query, indexReader.numDocs());
		scoreDocs = topDocs.scoreDocs;
		for (ScoreDoc scoreDoc : scoreDocs) {
			document = indexReader.document(scoreDoc.doc);
			for (IndexableField indexableField : document.getFields()) {
				System.out.println(String.format("%s ---- %s", indexableField.name(), indexableField.stringValue()));
			}
		    System.out.println("==============");
		}
		/*for (int i=0; i<indexReader.maxDoc(); i++) {
		    Document doc = indexReader.document(i);
		    String docId = doc.get("docId");

		    for (IndexableField indexableField : doc.getFields()) {
				System.out.println(String.format("%s ---- %s", indexableField.name(), indexableField.stringValue()));
			}
		    System.out.println("==============");
		}*/
	  }

}
