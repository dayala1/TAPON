package featuresCalculation.features.attribute;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import dataset.Attribute;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import featuresCalculation.Feature;
import featuresCalculation.FeatureValue;
import utils.GloveEmbeddingsCalculator;
import utils.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GloveEmbeddingsCrawl300 extends Feature<Attribute> {
	
	//Properties-----------------------------------------------------
	
	private TokenizerFactory<Word> tf;
	private GloveEmbeddingsCalculator ec;

	public GloveEmbeddingsCrawl300() throws IOException {
		tf = PTBTokenizer.PTBTokenizerFactory.newTokenizerFactory();
		ec = GloveEmbeddingsCalculator.getInstance();
	}

	//Interface methods----------------------------------------------

	@Override
	public FeatureValue apply(Attribute element) {
		List<Word> rawWords = tf.getTokenizer(new StringReader(element.getValue())).tokenize();
		System.out.println(rawWords);
		Map<String, Double> value;
		FeatureValue result;

		value = new TreeMap<>();

		double ivalue;
		double[] avg = new double[300];
		for (int i = 0; i < avg.length; i++) {
			avg[i] = 0;
		}
		int j = 0;
		for (Word word:rawWords) {
			double[] embeddings = ec.getEmbeddings(word.word());
			if(embeddings != null) {
				j++;
				for (int i = 0; i < avg.length; i++) {
					avg[i] += embeddings[i];
				}
			}
		}
		if(j==0){
			j++;
		}

		for (int i = 0; i < avg.length; i++) {
			ivalue = avg[i] / j;
			value.put(Integer.toString(i), ivalue);
		}

		result = new FeatureValue();
		result.setFeature(this);
		result.setValue(value);
		result.setFeaturable(element);
		updateObservers(result);

		return result;
	}
	
	@Override
	public String toString() {
		String result;
		result = "";
		for(int i=0; i<300; i++){
			result += String.format("|GloveCrawl300-%d", i);
		}
		result = result.substring(1);
		return result;
	}

}
