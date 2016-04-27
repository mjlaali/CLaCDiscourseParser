package org.cleartk.corpus.conll2015;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse.type.TokenList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

class ConllGoldTokenList{

	public ConllGoldTokenList(TokenList tokenList, final Map<Token, TokenPosition> tokenPositions) {
		if (tokenList == null)
			return ;
		
		RawText = TokenListTools.getTokenListText(tokenList);
		List<int[]> spanLists = new ArrayList<>();
		List<int[]> tokenLists = new ArrayList<>();
		
		List<Token> tokens = TokenListTools.convertToTokens(tokenList);
		tokenLists = tokens.stream().map((token) -> {
			TokenPosition tokenPosition = tokenPositions.get(token);
			return new int[]{token.getBegin(), token.getEnd(), tokenPosition.getDocumentOffset(), tokenPosition.getSentenceOffset(), tokenPosition.getOffsetInSentence()};
		}).collect(Collectors.toList());
		

		int start = -1;
		int end = -1;
		for (Token token: tokens){
			if (start == -1){
				start = token.getBegin();
				end = token.getEnd();
			} else if (token.getBegin() <= end + 1){
				end = token.getEnd();
			} else {
				spanLists.add(new int[]{start, end});
				start = token.getBegin();
				end = token.getEnd();
			}
		}
		spanLists.add(new int[]{start, end});
		
		
		TokenList = tokenLists.toArray(new int[tokenLists.size()][]);
		CharacterSpanList = spanLists.toArray(new int[spanLists.size()][]);
		
	}

	int[][] CharacterSpanList = new int[0][];
	String RawText = "";
	int[][] TokenList = new int[0][];
	
}

class ConllGoldDiscourseRelation{
	String DocID;
	String Type;
	String[] Sense = new String[1];
	String discourseConnectiveText;
	ConllGoldTokenList Arg1;
	ConllGoldTokenList Arg2;
	ConllGoldTokenList Connective;
	
}

class TokenPosition{
	int documentOffset;
	int sentenceOffset;
	int offsetInSentence;
	
	public TokenPosition(int documentOffset) {
		super();
		this.documentOffset = documentOffset;
	}
	
	public void setOffsetInSentence(int indexInSentence) {
		this.offsetInSentence = indexInSentence;
	}
	
	public void setSentenceOffset(int sentenceOffset) {
		this.sentenceOffset = sentenceOffset;
	}
	
	public int getDocumentOffset() {
		return documentOffset;
	}
	
	public int getOffsetInSentence() {
		return offsetInSentence;
	}
	
	public int getSentenceOffset() {
		return sentenceOffset;
	}
}

public class ConllJSonGoldExporter extends JCasAnnotator_ImplBase{
	public static final String PARAM_JSON_OUT_FILE = "PARAM_JSON_OUT_FILE";
	public static final String PARAM_EXCLUDE_RELATION_TYPES = "excludeRelationType";
	

	@ConfigurationParameter(
			name = PARAM_JSON_OUT_FILE,
			description = "Specify the json output file.",
			mandatory = true)
	private String jsonOutFilePath;

	@ConfigurationParameter(
			name = PARAM_EXCLUDE_RELATION_TYPES,
			description = "Specify the relation types that are excluded from the output",
			mandatory = false)
	private String[] excludeRelationTypes;

	private PrintWriter jsonFile;
	private Set<String> toBeExcluded = new HashSet<>();

	private XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
	
	public static AnalysisEngineDescription getDescription(File jsonOuFilePath, String... excludeRelationTypes) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				ConllJSonGoldExporter.class,
				PARAM_JSON_OUT_FILE,
				jsonOuFilePath,
				PARAM_EXCLUDE_RELATION_TYPES,
				excludeRelationTypes);
	}

	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("", ConllGoldDiscourseRelation.class);
		try {
			File directory = new File(jsonOutFilePath).getParentFile();
			if (!directory.exists())
				directory.mkdirs();
			jsonFile = new PrintWriter(jsonOutFilePath);
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e); 
		}
		
		if (excludeRelationTypes != null)
			toBeExcluded.addAll(Arrays.asList(excludeRelationTypes));
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Map<Token, TokenPosition> tokenToDocumentOffset = getTokenOffsets(aJCas);
		
		for (DiscourseRelation discourseRelation: JCasUtil.select(aJCas, DiscourseRelation.class)){
			if (toBeExcluded.contains(discourseRelation.getRelationType()))
				continue;
			String line = convertToJSon(discourseRelation, tokenToDocumentOffset, aJCas);
			jsonFile.println(line);
			jsonFile.flush();
		}
	}
	
	private Map<Token, TokenPosition> getTokenOffsets(JCas aJCas) {
		Map<Token, TokenPosition> tokenPositions = new HashMap<>();
		
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		int docOffset = 0; 
		for (Token token: tokens){
			tokenPositions.put(token, new TokenPosition(docOffset++));
		}
		
		Map<Sentence, Collection<Token>> sentTokens = JCasUtil.indexCovered(aJCas, Sentence.class, Token.class);
		Collection<Sentence> sents = JCasUtil.select(aJCas, Sentence.class);
		
		int sentOffset = 0;
		for (Sentence sent: sents){
			int indexInSentence = 0;
			for (Token token: sentTokens.get(sent)){
				TokenPosition tokenPosition = tokenPositions.get(token);
				tokenPosition.setSentenceOffset(sentOffset);
				tokenPosition.setOffsetInSentence(indexInSentence++);
			}
			sentOffset++;
		}
		
		return tokenPositions;
	}

	private String convertToJSon(DiscourseRelation discourseRelation, Map<Token, TokenPosition> tokenPositions, JCas aJCas) throws AnalysisEngineProcessException {
		ConllGoldDiscourseRelation conllDiscourseRelation = new ConllGoldDiscourseRelation();
		conllDiscourseRelation.DocID = Tools.getDocName(aJCas);

		DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
		
		conllDiscourseRelation.Connective = new ConllGoldTokenList(discourseConnective, tokenPositions);

		DiscourseArgument arg1 = discourseRelation.getArguments(0);
		conllDiscourseRelation.Arg1 = new ConllGoldTokenList(arg1, tokenPositions);
		DiscourseArgument arg2 = discourseRelation.getArguments(1);
		conllDiscourseRelation.Arg2 = new ConllGoldTokenList(arg2, tokenPositions);

		conllDiscourseRelation.Sense[0] = discourseRelation.getSense();
		conllDiscourseRelation.Type = discourseRelation.getRelationType();

		String jsonVal = xstream.toXML(conllDiscourseRelation).replace("\n", "").replaceAll(" +", " ");
		jsonVal = jsonVal.substring("{\"\": ".length(), jsonVal.length() - 1);
		return jsonVal;
	}

	
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		jsonFile.close();
		System.out.println("ConllJSONExporter.collectionProcessComplete()");
	}
	
}
