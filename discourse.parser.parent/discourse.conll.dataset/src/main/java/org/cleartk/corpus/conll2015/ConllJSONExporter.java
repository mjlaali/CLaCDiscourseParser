package org.cleartk.corpus.conll2015;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

class ConllTokenList{
	public ConllTokenList(int[] tokens) {
		this.TokenList = tokens;
	}

	int[] TokenList;
}

class ConllDiscourseRelation{
	String DocID;
	Integer ID;
	String Type;
	String[] Sense = new String[1];
	String discourseConnectiveText;
	ConllTokenList Arg1;
	ConllTokenList Arg2;
	ConllTokenList Connective;
	
}

public class ConllJSONExporter extends JCasAnnotator_ImplBase{
	public static final String PARAM_JSON_OUT_FILE = "jsonOutFilePath";
	public static final String PARAM_EXPORT_NULL_SENSES = "exportNullSenses";
	public static final String JSON_OUT_FILE_DESCRIPTION = "Specify the json output file.";

	@ConfigurationParameter(
			name = PARAM_JSON_OUT_FILE,
			description = JSON_OUT_FILE_DESCRIPTION,
			mandatory = true)
	private String jsonOutFilePath;

	@ConfigurationParameter(
			name = PARAM_EXPORT_NULL_SENSES,
			mandatory = true)
	private boolean exportNullSenses;

	
	private PrintWriter jsonFile;

	private XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
	public static AnalysisEngineDescription getDescription(String jsonOuFilePath) throws ResourceInitializationException {
		return getDescription(jsonOuFilePath, true);
	}
	
	public static AnalysisEngineDescription getDescription(String jsonOuFilePath, boolean exportNullSenses) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				ConllJSONExporter.class,
				PARAM_JSON_OUT_FILE, jsonOuFilePath,
				PARAM_EXPORT_NULL_SENSES, exportNullSenses);
	}

	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("", ConllDiscourseRelation.class);
		try {
			File directory = new File(jsonOutFilePath).getParentFile();
			if (!directory.exists())
				directory.mkdirs();
			jsonFile = new PrintWriter(jsonOutFilePath);
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e); 
		}
	}
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		for (DiscourseRelation discourseRelation: JCasUtil.select(aJCas, DiscourseRelation.class)){
			if (discourseRelation.getSense() == null){
				if (exportNullSenses)
					discourseRelation.setSense("Expansion.Conjunction");
				else 
					continue;
			}
			
			String line = convertToJSon(discourseRelation, aJCas);
			jsonFile.println(line);
			jsonFile.flush();
//			try {
//				JSONObject jsonObject = getJSONObject(discourseRelation, aJCas);
//				jsonObject.write(jsonFile);
//				jsonFile.println();
//				jsonFile.flush();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	private String convertToJSon(DiscourseRelation discourseRelation, JCas aJCas) throws AnalysisEngineProcessException {
		ConllDiscourseRelation conllDiscourseRelation = new ConllDiscourseRelation();
		conllDiscourseRelation.DocID = Tools.getDocName(aJCas);

		DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
		List<Token> connectiveTokens = Collections.emptyList();
		if (discourseConnective != null){
			connectiveTokens = TokenListTools.convertToTokens(discourseConnective);
		}
		
		conllDiscourseRelation.Connective = ids(connectiveTokens);

		DiscourseArgument arg1 = discourseRelation.getArguments(0);
		conllDiscourseRelation.Arg1 = ids(TokenListTools.convertToTokens(arg1));
		DiscourseArgument arg2 = discourseRelation.getArguments(1);
		conllDiscourseRelation.Arg2 = ids(TokenListTools.convertToTokens(arg2));

		conllDiscourseRelation.Sense[0] = discourseRelation.getSense();
		conllDiscourseRelation.Type = discourseRelation.getRelationType();
		conllDiscourseRelation.ID = discourseRelation.getRelationId();

		String jsonVal = xstream.toXML(conllDiscourseRelation).replace("\n", "").replaceAll(" +", " ");
		jsonVal = jsonVal.substring("{\"\": ".length(), jsonVal.length() - 1);
		return jsonVal;
	}

	
	private ConllTokenList ids(List<Token> tokens) {
		int[] ides = new int[tokens.size()];
		for (int i = 0; i < tokens.size(); i++){
			ConllToken token = (ConllToken) tokens.get(i);
			ides[i] = token.getDocumentOffset();
		}
		
		return new ConllTokenList(ides);
	}

	@SuppressWarnings("unused")
	private JSONObject getJSONObject(DiscourseRelation discourseRelation, JCas aJCas) throws AnalysisEngineProcessException, JSONException {
		JSONObject jsonRel = new JSONObject();
		jsonRel.put("DocID", Tools.getDocName(aJCas));

		JSONObject jsonTokens = null;
		DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
		
		List<Token> connectiveTokens = Collections.emptyList();
		if (discourseConnective != null)
			connectiveTokens = TokenListTools.convertToTokens(discourseConnective);
		
		jsonTokens = getJSONObject(connectiveTokens);
		jsonRel.put(ConllJSON.CONNECTIVE, jsonTokens);

		for (int i = 0; i < discourseRelation.getArguments().size(); i++){
			DiscourseArgument argument = discourseRelation.getArguments(i);
			jsonTokens = getJSONObject(TokenListTools.convertToTokens(argument));
			jsonRel.put(argument.getArgumentType(), jsonTokens);
		}

		JSONArray jsonArray = new JSONArray();
		String sense = discourseRelation.getSense();
		jsonArray.put(sense);

		jsonRel.put(ConllJSON.RELATION_SENSE, jsonArray);
		jsonRel.put(ConllJSON.RELATION_TYPE, discourseRelation.getRelationType());

		return jsonRel;
	}

	private JSONObject getJSONObject(List<Token> tokens) throws JSONException {
		JSONObject jsonObject = new JSONObject();

		List<Integer> wordIds = new ArrayList<Integer>();
		for (int i = 0; i < tokens.size(); i++){
			ConllToken token = (ConllToken) tokens.get(i);
			wordIds.add(token.getDocumentOffset());
		}
		JSONArray jsonArray = new JSONArray(wordIds);
		
		jsonObject.put("TokenList", jsonArray);
		return jsonObject;
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		jsonFile.close();
	}
	
}
