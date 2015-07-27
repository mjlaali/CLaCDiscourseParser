package org.cleartk.discourse_parsing.module.dcAnnotator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.token.type.Token;

public class DictionaryBasedDCAnnotator extends JCasAnnotator_ImplBase{
	private static final String PARAM_DICTIONARY_FILE = "PARAM_DICTIONARY_FILE";
	
	@ConfigurationParameter(
			name = PARAM_DICTIONARY_FILE,
			description = "A file containing discourse connectives, each in one line",
			mandatory = true)
	private String dcDictionaryFile;
	private DCLookup dcLookup = new DCLookup();

	public static AnalysisEngineDescription getDescription(String dictionaryFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DictionaryBasedDCAnnotator.class,
				PARAM_DICTIONARY_FILE,
				dictionaryFile);
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			dcLookup.loadDC(dcDictionaryFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		String documentText = aJCas.getDocumentText().toLowerCase();

		Map<Integer, Integer> occurrences = dcLookup.getOccurrence(documentText, DCLookup.coverToTokens(aJCas, Token.class));
		for (Entry<Integer, Integer> occurence: occurrences.entrySet()){
			int indexOfDC = occurence.getKey();
			int endOfDc = occurence.getValue();
			DiscourseConnective discourseConnective = new DiscourseConnective(aJCas);
			List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, indexOfDC, endOfDc);
			TokenListTools.initTokenList(aJCas, discourseConnective, tokens);
			discourseConnective.addToIndexes();
		}
	}

}
