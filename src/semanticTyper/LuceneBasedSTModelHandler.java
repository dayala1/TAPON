package semanticTyper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the API class for the semantic typing module, implementing the
 * combined approach of TF-IDF based cosine similarity and Kolmogorov-Smirnov
 * test approaches for textual and numeric respectively by 
 * Ramnandan.S.K and Amol Mittal.
 * 
 * @author ramnandan
 * 
 */

public class LuceneBasedSTModelHandler{
	
	static Logger logger = LoggerFactory
			.getLogger(LuceneBasedSTModelHandler.class.getSimpleName());
	private ArrayList<String> allowedCharacters;

	private boolean modelEnabled = false;
	private String contextId;
	private String TEXTUAL_DIRECTORY;
	/**
	 * NOTE: Currently, TF-IDF based approach is used for both textual and
	 * numeric data due to bug in KS test on Apache Commons Math.
	 * 
	 * TODO: Integrate KS test when this bug is resolved :
	 * https://issues.apache.org/jira/browse/MATH-1131
	 */
	
	public void setTextualDirectory(String textualDirectory) {
		assert textualDirectory != null;
		
		this.TEXTUAL_DIRECTORY = textualDirectory;
	}

	public LuceneBasedSTModelHandler(String contextId) {
		allowedCharacters = allowedCharacters();
		this.contextId = contextId;
		logger.debug("inside STModelHandler constructor");
	}

	/**
	 * Adds the passed list of examples for training
	 * 
	 * @param label
	 *            True label for the list of example.
	 * @param examples
	 *            List of example strings.
	 * @return True if success, else False
	 */
	public synchronized boolean addType(String label, List<String> examples) {
		boolean savingSuccessful = false;

		// running basic sanity checks in the input arguments
		if (label == null || label.trim().length() == 0 || examples.size() == 0) {
			logger.warn("@label argument cannot be null or an empty string and the @examples list cannot be empty.");
			return false;
		}
		
		label = label.trim();
		ArrayList<String> cleanedExamples = new ArrayList<String>();
		cleanedExamplesList(examples, cleanedExamples);
		
		// making sure that the condition where the examples list is not empty
		// but contains junk only is not accepted
		if (cleanedExamples.size() == 0) {
			logger.warn("@examples list contains forbidden characters only. The allowed characters are "
					+ allowedCharacters);
			return false;
		}


		// if the column is textual
		try {
			savingSuccessful = indexTrainingColumn(label, cleanedExamples);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return savingSuccessful;
	}

	/**
	 * Indexes the given training column for a specific label
	 * 
	 * @param label
	 * @param selectedExamples
	 * @return
	 * @throws IOException
	 */
	
	
	private boolean indexTrainingColumn(String label,
			ArrayList<String> selectedExamples) throws IOException {
		/**
		 * @patch applied
		 * @author pranav and aditi
		 * @date 12th June 2015
		 * 
		 * 
		 */
		
		// treat content of column as single document
		StringBuilder sb = new StringBuilder();
		for (String ex : selectedExamples) {
			sb.append(ex);
			sb.append(" ");
		}

		// check if semantic label already exists
		Document labelDoc = null; // document corresponding to existing semantic label if exists
		if (indexDirectoryExists()) {
			try {
				// check if semantic label already exists in index
				Searcher searcher = new Searcher(getIndexDirectory(),
						Indexer.LABEL_FIELD_NAME);
				try {
					labelDoc = searcher.getDocumentForLabel(label);
				} finally {
					searcher.close();
				}
			} catch (Exception e) {
				// Ignore, the searcher might not work if index is empty.
			}
		}

		// index the document
		Indexer indexer = new Indexer(getIndexDirectory());
		try {
			indexer.open();
			if (labelDoc != null) {
				IndexableField[] existingContent = labelDoc.getFields(Indexer.CONTENT_FIELD_NAME);
				indexer.updateDocument(existingContent, sb.toString(), label);
			} else {
				indexer.addDocument(sb.toString(), label);
			}
			indexer.commit();
		} finally {
			indexer.close();
		}

		return true;
	}

	/**
	 * Check if index directory exists and contains files
	 * 
	 * @return
	 */
	private boolean indexDirectoryExists() {
		File dir = new File(TEXTUAL_DIRECTORY);

		if (dir.exists() && dir.listFiles().length > 0) {
			String[] files = dir.list();
			for (String file : files) {
				if (file.startsWith("segments"))
					return true;
			}
		}
		return false;
	}

	/**
	 * @param examples
	 *            - list of examples of an unknown type
	 * @param numPredictions
	 *            - required number of predictions in descending order
	 * @param predictedLabels
	 *            - the argument in which the ordered list of labels is
	 *            returned. the size of this list could be smaller than
	 *            numPredictions if there aren't that many labels in the model
	 *            already
	 * @param confidenceScores
	 *            - the probability of the examples belonging to the labels
	 *            returned.
	 * @param exampleProbabilities
	 *            - the size() == examples.size(). It contains, for each
	 *            example, in the same order, a double array that contains the
	 *            probability of belonging to the labels returned in
	 *            predictedLabels.
	 * @param columnFeatures
	 *            - this Map supplies ColumnFeatures such as ColumnName, etc.
	 * @return True, if successful, else False
	 */
	
	public List<SemanticTypeLabel> predictType(List<String> examples,
			int numPredictions) {

		if (!this.modelEnabled) {
			logger.warn("Semantic Type Modeling is not enabled");
			return null;
		}

		// Sanity checks for arguments
		if (examples == null || examples.size() == 0 || numPredictions <= 0) {
			logger.warn("Invalid arguments. Possible problems: examples list size is zero, numPredictions is non-positive");
			return null;
		}

		logger.debug("Predic Type for " + examples.toArray().toString());
		// get top-k suggestions
		if (indexDirectoryExists()) {
			// construct single text for test column
			StringBuilder sb = new StringBuilder();
			for (String ex : examples) {
				sb.append(ex);
				sb.append(" ");
			}
			
			try {
				Searcher predictor = new Searcher(getIndexDirectory(),
						Indexer.CONTENT_FIELD_NAME);
				try {
					List<SemanticTypeLabel> result = predictor.getTopK(numPredictions, sb.toString());
					logger.debug("Got " + result.size() + " predictions");
					return result;
				} finally {
					predictor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * @return True if successfully cleared the model. False, otherwise. This
	 *         method removes all labels from the model.
	 * 
	 *         Currently, when only TF-IDF is used, equivalent to deleting all
	 *         documents
	 */
	
	public boolean removeAllLabels() {
		try {
			Indexer indexer = new Indexer(getIndexDirectory());
			try {
				indexer.open();
				indexer.deleteAllDocuments();
				indexer.commit();
			} finally {
				indexer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public String getIndexDirectory()
	{
		return TEXTUAL_DIRECTORY;
	}
	/**
	 * @param uncleanList
	 *            List of all examples
	 * @param cleanedList
	 *            List with examples that don't have unallowed chars and others
	 *            such as nulls or empty strings This method cleans the examples
	 *            list passed to it. Generally, it is used by other methods to
	 *            sanitize lists passed from outside.
	 */
	private void cleanedExamplesList(List<String> uncleanList,
			List<String> cleanedList) {
		cleanedList.clear();
		for (String example : uncleanList) {
			if (example != null) {
				String trimmedExample;
				trimmedExample = getSanitizedString(example);
				if (trimmedExample.length() != 0) {
					cleanedList.add(trimmedExample);
				}
			}
		}
	}

	/**
	 * @param unsanitizedString
	 *            String to be sanitized
	 * @return sanitizedString
	 */
	private String getSanitizedString(String unsanitizedString) {
		String sanitizedString;
		sanitizedString = "";
		for (int i = 0; i < unsanitizedString.length(); i++) {
			String charAtIndex;
			charAtIndex = unsanitizedString.substring(i, i + 1);
			if (allowedCharacters.contains(charAtIndex)) {
				sanitizedString += charAtIndex;
			}
		}
		return sanitizedString;
	}

	/**
	 * @return Returns list of allowed Characters
	 */
	private ArrayList<String> allowedCharacters() {
		ArrayList<String> allowed = new ArrayList<String>();
		// Adding A-Z
		for (int c = 65; c <= 90; c++) {
			allowed.add(new Character((char) c).toString());
		}
		// Adding a-z
		for (int c = 97; c <= 122; c++) {
			allowed.add(new Character((char) c).toString());
		}
		// Adding 0-9
		for (int c = 48; c <= 57; c++) {
			allowed.add(new Character((char) c).toString());
		}
		allowed.add(" "); // adding space
		allowed.add("."); // adding dot
		allowed.add("%");
		allowed.add("@");
		allowed.add("_");
		allowed.add("-");
		allowed.add("*");
		allowed.add("(");
		allowed.add(")");
		allowed.add("[");
		allowed.add("]");
		allowed.add("+");
		allowed.add("/");
		allowed.add("&");
		allowed.add(":");
		allowed.add(",");
		allowed.add(";");
		allowed.add("?");
		return allowed;
	}

	public void setModelHandlerEnabled(boolean enabled) {
		this.modelEnabled = enabled;

	}

}
