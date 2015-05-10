package org.cleartk.discourse_parsing.errorAnalysis;

import ir.laali.tools.ds.DSManagment;
import ir.laali.tools.ds.DSPrinter;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.token.type.Token;

public class AnalysisParserOutput extends JCasAnnotator_ImplBase{
	public static final String PARAM_REPORT_FILE = "reportFile";

	private Map<String, DiscourseRelation> goldRelations;
	private Map<String, DiscourseRelation> parserRelations;
	
	int correct = 0;
	int totalRel = 0;
	int totalOutput = 0;
	Map<String, Integer> arg1Status = new TreeMap<String, Integer>();
	Map<String, Integer> arg2Status = new TreeMap<String, Integer>();
	Map<String, Integer> arg12Status = new TreeMap<String, Integer>();


	@ConfigurationParameter(
			name = PARAM_REPORT_FILE,
			description = "the report file",
			mandatory = true)
	private String reportFile;

	public static AnalysisEngineDescription getDescription(String reportFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				AnalysisParserOutput.class,
				PARAM_REPORT_FILE,
				reportFile);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		goldRelations = new TreeMap<String, DiscourseRelation>();
		parserRelations = new TreeMap<String, DiscourseRelation>();
		
		try {
			JCas goldView = aJCas.getView(ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW);
			JCas parserView = aJCas.getView(CAS.NAME_DEFAULT_SOFA);
			storeRelations(goldRelations, goldView);
			storeRelations(parserRelations, parserView);
			
			compareRelations();
			
		} catch (CASException e) {
			e.printStackTrace();
		}
		
	}

	
	private void compareRelations() {
		totalOutput += parserRelations.size();
		totalRel += goldRelations.size();
		
		for (Entry<String, DiscourseRelation> keyGoldRelation: goldRelations.entrySet()){
			String key = keyGoldRelation.getKey();
			DiscourseRelation aGoldRelation = keyGoldRelation.getValue();
			DiscourseRelation aParserRelation = parserRelations.remove(key);
			if (aParserRelation != null){
				correct++;
				String arg1stat = getStatusTokenList(TokenListTools.convertToTokens(aGoldRelation.getArguments(0)), TokenListTools.convertToTokens(aParserRelation.getArguments(0)));
				DSManagment.incValue(arg1Status, arg1stat);
				String arg2stat = getStatusTokenList(TokenListTools.convertToTokens(aGoldRelation.getArguments(1)), TokenListTools.convertToTokens(aParserRelation.getArguments(1)));
				DSManagment.incValue(arg2Status, arg2stat);
				DSManagment.incValue(arg12Status, arg1stat + "-" + arg2stat);
			}
			
		}
	}

	private String getStatusTokenList(List<Token> goldTokens, List<Token> systemTokens) {
		return getStatus(converToStr(goldTokens), converToStr(systemTokens));
		
	}
	
	private List<String> converToStr(List<Token> goldTokens) {
		List<String> converted = new ArrayList<String>();
		for (Token token: goldTokens){
			converted.add(token.getCoveredText() + "-" + ((ConllToken)token).getDocumentOffset());
		}
		return converted;
	}

	private String getStatus(List<String> goldTokens, List<String> systemTokens) {
		
		if (goldTokens.equals(systemTokens))
			return "Eq";
		if (goldTokens.containsAll(systemTokens))
			return "Sub";
		if (systemTokens.containsAll(goldTokens))
			return "Sup";
		if (goldTokens.removeAll(systemTokens))
			return "Int";
		
		return "non";
	}

	private void storeRelations(Map<String, DiscourseRelation> relations,
			JCas aView) {
		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aView, DiscourseRelation.class);
		for (DiscourseRelation discourseRelation: discourseRelations){
			DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
			if (discourseConnective == null)
				continue;
			int startIdx = ((ConllToken) TokenListTools.convertToTokens(discourseConnective).get(0)).getDocumentOffset();
			String key = TokenListTools.getTokenListText(discourseConnective) + "-" + startIdx;
			relations.put(key, discourseRelation);
		}
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		try {
			PrintStream output = new PrintStream(reportFile);
			output.println(String.format("Precision = %.2f, Recall = %.2f, Total Rel = %d, Total out = %d", (double)correct / totalOutput, (double)correct / totalRel, totalRel, totalOutput));
			output.println();
			DSPrinter.printMap("Arg1", arg1Status.entrySet(), output);
			output.println();
			DSPrinter.printMap("Arg2", arg2Status.entrySet(), output);
			output.println();
			DSPrinter.printMap("Arg 1 & 2", arg12Status.entrySet(), output);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
