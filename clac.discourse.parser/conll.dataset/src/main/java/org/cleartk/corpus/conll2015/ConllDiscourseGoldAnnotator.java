package org.cleartk.corpus.conll2015;

import ir.laali.tools.ds.DSManagment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.json.JSONComplexAnnotation;
import org.cleartk.corpus.conll2015.json.JSONRelation;
import org.cleartk.corpus.conll2015.json.JSONToken;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.cleartk.discourse.type.DiscourseRelation;
import org.json.JSONException;
import org.json.JSONObject;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


public class ConllDiscourseGoldAnnotator extends JCasAnnotator_ImplBase{
	public static final String PARAM_DISCOURSE_JSON_FILE = "PARAM_DISCOURSE_JSON_FILE";
	public static final String DISCOURSE_JSON_FILE_DESCRIPTION = "Specify the discourse json file.";

	public static final String PARAM_ADD_MULTIPLE_SENSES = "PARAM_ADD_MULTIPLE_SENSES";
	public static final String ADD_MULTIPLE_SENSES_DESCRIPTION = "Indicate if multiple senses of a discourse relations addes as separate relations or just one of them.";
	public static final String GOLD_DISCOURSE_VIEW = "";

	@ConfigurationParameter(
			name = PARAM_DISCOURSE_JSON_FILE,
			description = DISCOURSE_JSON_FILE_DESCRIPTION,
			mandatory = true)
	private String discourseJsonFilePath;
	
	@ConfigurationParameter(
			name = PARAM_ADD_MULTIPLE_SENSES,
			description = ADD_MULTIPLE_SENSES_DESCRIPTION,
			mandatory = true)
	private boolean addMultipleSenses;
	
	
	private Map<String, RelationType> textToRelation = new TreeMap<String, RelationType>();
	private DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
	
	public static AnalysisEngineDescription getDescription(String discourseFilePath) throws ResourceInitializationException {
		return getDescription(discourseFilePath, true);
	}
	
	public static AnalysisEngineDescription getDescription(String discourseFilePath, boolean addMultipleSenses) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				ConllDiscourseGoldAnnotator.class,
				PARAM_DISCOURSE_JSON_FILE,
				discourseFilePath, 
				PARAM_ADD_MULTIPLE_SENSES, 
				addMultipleSenses);
	}

	private Map<String, List<JSONObject>> docJSonRelations = new TreeMap<String, List<JSONObject>>();
	private JCas aJCas;
	private ArrayList<Token> docTokens;
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		File source = new File(discourseJsonFilePath);
		try {
			Scanner scanner = new Scanner(source);
			while (scanner.hasNext()){
				String line = scanner.nextLine();
				JSONObject aDiscourseAnnotation = new JSONObject(line);
				String jsonDocId = aDiscourseAnnotation.getString(ConllJSON.DOC_ID);
				DSManagment.addToList(docJSonRelations, jsonDocId, aDiscourseAnnotation);
			}
			scanner.close();
		} catch (IOException | JSONException e) {
			throw new ResourceInitializationException(e);
		}
		
		for (RelationType relationType: RelationType.values()){
			textToRelation.put(relationType.toString().toLowerCase(), relationType);
		}
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		if (!docJSonRelations.containsKey(Tools.getDocName(aJCas))){
			if (JCasUtil.exists(aJCas, ConllToken.class)){
				System.err.println("ConllDiscourseGoldAnnotator.process(): No discourse relation for file <" +
						Tools.getDocName(aJCas) + "> while there is syntactic informaiton for the file. <" +
						docJSonRelations.size() + ">");
			}
			return;
		}
		
		List<JSONObject> jsonRelations = docJSonRelations.remove(Tools.getDocName(aJCas));
		if (jsonRelations == null)
			return;
		
		this.aJCas = aJCas;
		docTokens = new ArrayList<>(JCasUtil.select(aJCas, Token.class));
		for (JSONObject aDiscourseAnnotation: jsonRelations){
			JSONRelation discourseRelaiton;
				try {
					discourseRelaiton = new JSONRelation();
					discourseRelaiton.init(aDiscourseAnnotation);
				} catch (JSONException e) {
					throw new RuntimeException(e); 
				}
			addDiscourseRelation(discourseRelaiton);
		}
		
	}
	
	private void addDiscourseRelation(JSONRelation jsonDiscourseRelation) {
		@SuppressWarnings("unchecked")
		List<String> senses = (List<String>)jsonDiscourseRelation.getFeatures().get(ConllJSON.RELATION_SENSE);
		for (String sense: senses){
			RelationType type = textToRelation.get(jsonDiscourseRelation.getFeatures().get(ConllJSON.RELATION_TYPE).toString().toLowerCase());
			Map<String, JSONComplexAnnotation> annotaions = jsonDiscourseRelation.getAnnotaions();
			JSONComplexAnnotation jsonDiscourseConnective = annotaions.get(ConllJSON.CONNECTIVE);
			String discourseConnectiveText = jsonDiscourseConnective.getRawText();
			List<Token> discourseConnectiveTokenList = convertToTokens(jsonDiscourseConnective.getTokenList().getTokenList());
			List<Token> arg1Tokens = convertToTokens(annotaions.get(ArgType.Arg1.toString()).getTokenList().getTokenList());
			List<Token> arg2Tokens = convertToTokens(annotaions.get(ArgType.Arg2.toString()).getTokenList().getTokenList());

			
			DiscourseRelation discourseRelation = discourseRelationFactory.makeDiscourseRelation(aJCas,
					type, sense, discourseConnectiveText, discourseConnectiveTokenList, arg1Tokens, arg2Tokens);
			addToIndex(discourseRelation);
			if (!addMultipleSenses)
				break;
		}
	}

	protected void addToIndex(DiscourseRelation discourseRelation) {
		discourseRelation.addToIndexes();
		if (discourseRelation.getRelationType().equals(RelationType.Explicit.toString()))
			discourseRelation.getDiscourseConnective().addToIndexes();
		discourseRelation.getArguments(0).addToIndexes();
		discourseRelation.getArguments(1).addToIndexes();
	}

	private List<Token> convertToTokens(List<JSONToken> jsonTokenList) {
		List<Token> tokens = new ArrayList<>();
		
		for (JSONToken jsonToken: jsonTokenList){
			if (docTokens.size() <= jsonToken.getDocOffset())
				System.out.println("ConllDiscourseGoldAnnotator.convertToTokens(): " + aJCas.getDocumentText());
			Token token = docTokens.get(jsonToken.getDocOffset());
			tokens.add(token);
		}
		
		return tokens;
	}
	
	
	
}
