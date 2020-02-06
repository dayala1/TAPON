package main;

import com.google.common.base.Charsets;
import com.hubspot.jinjava.Jinjava;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.json.simple.JSONObject;
import org.spark_project.guava.io.Resources;
import utils.GloveEmbeddingsCalculator;
import utils.RandomForestParsing.RandomForestParser;
import utils.RandomForestParsing.Tree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class OthersDriver {

	public static void main(String[] args) throws IOException {
		TokenizerFactory<Word> factory = PTBTokenizer.PTBTokenizerFactory.newTokenizerFactory();
		List<Word> rawWords = factory.getTokenizer(new StringReader("Hola/MUNDO /cruel !qué bien! No-crees? 2.8mm 35mm")).tokenize();
		System.out.println(rawWords);

		/*GloveEmbeddingsCalculator embeddingsCalculator = GloveEmbeddingsCalculator.getInstance();
		System.out.println(embeddingsCalculator.getSimilarity("jew", "christian"));
		System.out.println(embeddingsCalculator.getSimilarity("jewish", "bank"));
		System.out.println(embeddingsCalculator.getSimilarity("jew", "obama"));
		System.out.println(embeddingsCalculator.getSimilarity("red", "blue"));
		System.out.println(embeddingsCalculator.getSimilarity("bag", "electric"));
		System.out.println("huehuehue " + embeddingsCalculator.getEmbeddings("huehuehue23d"));
		System.out.println("2x " + embeddingsCalculator.getEmbeddings("2x"));
		System.out.println("5x " + embeddingsCalculator.getEmbeddings("5x"));
		System.out.println("12315 " + embeddingsCalculator.getEmbeddings("12315"));
		System.out.println("2.8f " + embeddingsCalculator.getEmbeddings("2.8f"));
		System.out.println("35mm " + embeddingsCalculator.getEmbeddings("35mm"));*/


		/*
		List<Pair<String, List<String>>> treeStrings;
		treeStrings = RandomForestParser.separateTrees(new File("E:/model/classifiersAndTables/Awards/1/modelClassifiers/classifiersParsed/hintBased/attributes/email/email.txt"));
		List<Tree> trees = treeStrings.stream().map(t -> new Tree(t, null)).collect(Collectors.toList());
		File treesFolder = new File("treePages/");
		treesFolder.mkdirs();
		RandomForestParser.makeTreeViz(trees, treesFolder.getPath());*/
	}
}
