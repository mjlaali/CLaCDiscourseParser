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
				addSyntaxInfo(jsonSentences.getJSONObject(i), aJCas);

		} catch (JSONException | CASException e) {
			throw new AnalysisEngineProcessException(e);
		}

	}

	private void addSyntaxInfo(JSONObject jsonSent, JCas aJCas) throws JSONException, AnalysisEngineProcessException, CASException {
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
//		JSONArray dependencies = jsonSent.getJSONArray("dependencies");

//		ArrayListMultimap<DependencyNode, DependencyRelation> headRelations = ArrayListMultimap.create();
//		ArrayListMultimap<DependencyNode, DependencyRelation> childRelations = ArrayListMultimap.create();
//		List<DependencyNode> nodes = new ArrayList<DependencyNode>();
//
//		nodes.add(new TopDependencyNode(aJCas, sentBegin, sentEnd));
//		for (int i = 0; i < sentTokens.size(); i++){
//			DependencyNode aNode = new DependencyNode(aJCas, sentTokens.get(i).getBegin(), sentTokens.get(i).getEnd());
//			nodes.add(aNode);
//		}
//		
//		for (int i = 0; i < dependencies.length(); i++){
//			JSONArray aJsonDepRel = dependencies.getJSONArray(i);
//			String relationType = aJsonDepRel.getString(0);
//			String govern = aJsonDepRel.getString(1);
//			String dep = aJsonDepRel.getString(2);
//
//			int governIdx = Integer.parseInt(govern.substring(govern.lastIndexOf('-') + 1));
//			int depIdx = Integer.parseInt(dep.substring(dep.lastIndexOf('-') + 1));
//			
//			DependencyNode head = nodes.get(governIdx);
//			DependencyNode child = nodes.get(depIdx);
//			
//			if (governIdx != 0 && (!head.getCoveredText().equals(govern.substring(0, govern.lastIndexOf('-'))) ||
//					!child.getCoveredText().equals(dep.substring(0, dep.lastIndexOf('-')))))
//				System.err.println("ConllSyntaxGoldAnnotator.addDependencies()" + 
//					String.format("out of sync: %s <> %s, %s <> %s", head.getCoveredText(), govern, child.getCoveredText(), dep));
//			
//			DependencyRelation relation = new DependencyRelation(aJCas);
//
//			relation.setHead(head);
//			relation.setChild(child);
//			relation.setRelation(relationType);
//			relation.addToIndexes();
//			headRelations.put(child, relation);
//			childRelations.put(head, relation);
//		}
//
//		// set the relations for each node annotation
//		for (DependencyNode node : nodes) {
//			List<DependencyRelation> heads = headRelations.get(node);
//			node.setHeadRelations(new FSArray(aJCas, heads == null ? 0 : heads.size()));
//			if (heads != null) {
//				FSCollectionFactory.fillArrayFS(node.getHeadRelations(), heads);
//			}
//			List<DependencyRelation> children = childRelations.get(node);
//			node.setChildRelations(new FSArray(aJCas, children == null ? 0 : children.size()));
//			if (children != null) {
//				FSCollectionFactory.fillArrayFS(node.getChildRelations(), children);
//			}
//			node.addToIndexes();
//		}
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
