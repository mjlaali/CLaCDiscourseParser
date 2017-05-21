package org.cleartk.corpus.conll2015;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.cleartk.corpus.conll2015.type.SentenceWithSyntax;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.concordia.clac.uima.engines.Tools;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

@TypeCapability(outputs = {"org.cleartk.token.type.Sentence", "org.cleartk.corpus.conll2015.type.ConllToken"})
public class ConllSyntaxGoldAnnotator extends JCasAnnotator_ImplBase{
	public static final String PARAM_SYNTAX_JSON_FILE = "PARAM_SYNTAX_JSON_FILE";
	public static final String SYNTAX_JSON_FILE_DESCRIPTION = "Specify the syntax json file.";
	public static final String NO_PARSE = "(())";

	@ConfigurationParameter(
			name = PARAM_SYNTAX_JSON_FILE,
			description = SYNTAX_JSON_FILE_DESCRIPTION,
			mandatory = true)
	private String syntaxFilePath;

	private JSONObject root;
	private int tokenIdx;
	private SyntaxReader syntaxReader = new SyntaxReader();

	public static AnalysisEngineDescription getDescription(File syntaxFilePath) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				ConllSyntaxGoldAnnotator.class,
				PARAM_SYNTAX_JSON_FILE,
				syntaxFilePath);
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		File source = new File(syntaxFilePath);
		try {
			root = new JSONObject(FileUtils.readFileToString(source));
		} catch (JSONException | IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String docId = Tools.getDocName(aJCas);

		try {
			tokenIdx = 0;

			if (!root.has(docId)){
				return;
			}
			JSONObject jsonDoc = root.getJSONObject(docId);

			JSONArray jsonSentences = jsonDoc.getJSONArray("sentences");
			for (int i = 0; i < jsonSentences.length(); i++)
				addSyntaxInfo(jsonSentences.getJSONObject(i), aJCas, i);

		} catch (JSONException | CASException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private void addSyntaxInfo(JSONObject jsonSent, JCas aJCas, int sentenceOffset) throws JSONException, AnalysisEngineProcessException, CASException {
		JSONArray jsonWords = jsonSent.getJSONArray("words");
		int sentBegin = Integer.MAX_VALUE;
		int sentEnd = -1;
		List<Token> sentTokens = new ArrayList<>();

		for (int i = 0; i < jsonWords.length(); i++){
			JSONArray jsonWord = jsonWords.getJSONArray(i);
			JSONObject jsonWordInfo = jsonWord.getJSONObject(1);
			int wordBegin = jsonWordInfo.getInt("CharacterOffsetBegin");
			int wordEnd = jsonWordInfo.getInt("CharacterOffsetEnd");
			ConllToken conllToken = new ConllToken(aJCas, wordBegin, wordEnd);
			POS pos = new POS(aJCas, wordBegin, wordEnd);
			pos.setPosValue(jsonWordInfo.getString("PartOfSpeech"));
			conllToken.setPos(pos);
			conllToken.setDocumentOffset(tokenIdx++);
			conllToken.setSentenceOffset(sentenceOffset);
			conllToken.setOffsetInSentence(i);
			conllToken.addToIndexes();
			if (wordBegin < sentBegin)
				sentBegin = wordBegin;
			if (wordEnd > sentEnd)
				sentEnd = wordEnd;
			sentTokens.add(conllToken);
		}
		
		addDependencies(jsonSent, sentTokens, aJCas, sentBegin, sentEnd);
		String parseTree = jsonSent.getString("parsetree");
		SentenceWithSyntax sentence = new SentenceWithSyntax(aJCas, sentBegin, sentEnd);
		sentence.setSyntaxTree(parseTree);
		sentence.addToIndexes();

		syntaxReader.addSyntacticConstituents(sentence, parseTree);
	}

	private void addDependencies(JSONObject jsonSent, List<Token> sentTokens, JCas aJCas, int sentBegin, int sentEnd) throws JSONException, AnalysisEngineProcessException {
		JSONArray dependencies = jsonSent.getJSONArray("dependencies");
		List<List<String>> dependeciesValues = DiscourseRelationExample.jSonToList(dependencies);
		syntaxReader.addDependency(sentTokens, aJCas, dependeciesValues);
	}

	
	
	public static void main(String[] args) throws UIMAException, IOException {
		String base = "/Users/majid/Documents/git/french-connective-disambiguation/connective-disambiguation/data/pdtb/";
		String rawDir = base + "raw";
		String parseTreeFile = base + "pdtb-parses.json";
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, rawDir, 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(new File(parseTreeFile));

		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader);
	}
}
