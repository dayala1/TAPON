package utils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import featuresCalculation.DatasetFeaturesCalculator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;

public class LuceneUtils {
	private static Map<String, Double> idfMap;
	
	public static void prepareVocabulary(String fieldName, String indexPath, String folderPath) throws IOException{
		assert fieldName != null;
		assert indexPath != null;
		assert folderPath != null;
		
		Path path;
		Directory directory;
		IndexReader indexReader;
		Analyzer analyzer;
		Terms terms;
		Double threshold;
		Double idf;
		List<String> line;
		String filePath;
		
		idfMap = Maps.newHashMap();
		filePath = String.format("%s/%s-vocabulary.csv", folderPath, fieldName);
		FileUtilsCust.createCSV(filePath);
		analyzer = new StandardAnalyzer();
		path = Paths.get(indexPath);
		directory = FSDirectory.open(path);
		indexReader = DirectoryReader.open(directory);
		threshold = ((double)indexReader.numDocs())*0.005;
		//threshold = 2.0;
		terms = MultiFields.getTerms(indexReader, fieldName);
		TermsEnum termEnum = terms.iterator();
        BytesRef bytesRef;
        TFIDFSimilarity tfidfSIM = new DefaultSimilarity();
        while ((bytesRef = termEnum.next()) != null) {
            if (termEnum.seekExact(bytesRef)){
            	String term = bytesRef.utf8ToString();
            	if(termEnum.docFreq()>=threshold){
            		line = Lists.newArrayList();
            		idf = (double) tfidfSIM.idf( termEnum.docFreq(), indexReader.numDocs() );
            		line.add(term);
            		line.add(idf.toString());
            		idfMap.put(term, idf);
            	}
            }
        }
        analyzer.close();
	}

	public static void storeIdfMap(String objectPath) throws IOException {
		FileOutputStream fileOutputStream;
		ObjectOutputStream objectOutputStream;

		fileOutputStream = new FileOutputStream(objectPath);
		objectOutputStream = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(idfMap);
		objectOutputStream.close();
	}

	public static void loadIdfMap(String objectPath) throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream;
		ObjectInputStream objectInputStream;

		fileInputStream = new FileInputStream(objectPath);
		objectInputStream = new ObjectInputStream(fileInputStream);
		idfMap = (Map<String, Double>) objectInputStream.readObject();
		objectInputStream.close();
	}
	
	public static double[] computeTFIDFVector(String content) throws IOException{
		assert idfMap != null;
		
		double[] res;
		Map<String, Double> tfMap;
		Double tf;
		Double idf;
		Double tfIdf;
		int index;
		Analyzer analyzer;
		
		tfMap = Maps.newHashMap();
		for (String key : idfMap.keySet()) {
			tfMap.put(key, 0.0);
		}
		analyzer = new StandardAnalyzer();
		
        for (String word : tokenizeString(analyzer, content)) {
			if(tfMap.containsKey(word)){
				tfMap.put(word, tfMap.get(word)+1);
			}
		}
        res = new double[tfMap.size()];
        index = 0;
        for (String word : tfMap.keySet()) {
			tf = tfMap.get(word);
			idf = idfMap.get(word);
			tfIdf = tf * idf;
			res[index] = tfIdf;
		}
        
        return res;
		
	}
	
	public static List<String> tokenizeString(Analyzer analyzer, String string) {
	    List<String> result = new ArrayList<String>();
	    try {
	      TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
	      stream.reset();
	      while (stream.incrementToken()) {
	        result.add(stream.getAttribute(CharTermAttribute.class).toString());
	      }
	    } catch (IOException e) {
	      // not thrown b/c we're using a string reader...
	      throw new RuntimeException(e);
	    }
	    return result;
	  }
}
