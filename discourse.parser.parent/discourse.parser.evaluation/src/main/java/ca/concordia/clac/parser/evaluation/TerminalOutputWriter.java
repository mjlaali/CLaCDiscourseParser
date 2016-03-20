package ca.concordia.clac.parser.evaluation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.Feature;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.TokenList;

public class TerminalOutputWriter extends JCasAnnotator_ImplBase{
	PrintStream output = System.out;
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<TokenList> selected = JCasUtil.select(aJCas, TokenList.class);
		
		List<TokenList> sortedTokenList = new ArrayList<>(selected);
		
		Collections.sort(sortedTokenList, (a, b) -> {
			int diff = a.getBegin() - b.getBegin();
			if (diff == 0){
				diff = b.getEnd() - a.getEnd();
			}
				
			return diff;
		});
		
		String documentText = aJCas.getDocumentText();
		int currentTokenList = 0;
		int activeTokenList = 0;
		
		if (sortedTokenList.isEmpty()){
			System.out.println(documentText);
		} else {
			for (int i = 0; i < documentText.length(); i++){
				while (activeTokenList < sortedTokenList.size() && i == sortedTokenList.get(activeTokenList).getEnd()){
					output.printf("</%s>", sortedTokenList.get(activeTokenList).getType().getShortName());
					activeTokenList++;
				}
				
				while (currentTokenList < sortedTokenList.size() && i == sortedTokenList.get(currentTokenList).getBegin()){
					printTokenList(sortedTokenList, currentTokenList);
					currentTokenList++;
				}
				output.print(documentText.charAt(i));
			}
		}
		
	}

	private void printTokenList(List<TokenList> sortedTokenList, int activeTokenList) {
		TokenList toBePrinted = sortedTokenList.get(activeTokenList);
		output.printf("<%s", toBePrinted.getType().getShortName());
		for (Feature feature: toBePrinted.getType().getFeatures()){
			try {
				output.printf(" %s=\"%s\"", feature.getShortName(), toBePrinted.getFeatureValueAsString(feature));
			} catch (CASRuntimeException e) {
			}
		}
		output.print(">");
	}

	
}
