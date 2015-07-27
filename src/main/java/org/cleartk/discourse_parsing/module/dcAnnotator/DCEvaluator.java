package org.cleartk.discourse_parsing.module.dcAnnotator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.eval.util.ConfusionMatrix;
import org.cleartk.token.type.Token;

public class DCEvaluator extends JCasAnnotator_ImplBase{
	private static final String PARAM_REPORT_FILE = "PARAM_REPORT_FILE";
	private static final String PARAM_DC_HEAD_FILE = "PARAM_DC_HEAD_FILE";
	@ConfigurationParameter(
			name = PARAM_REPORT_FILE,
			description = "the report file",
			mandatory = true)
	private String reportFile;

	@ConfigurationParameter(
			name = PARAM_DC_HEAD_FILE,
			description = "the report file",
			mandatory = false)
	private String dcHeadsFile;
	
	private PrintStream output;
	private ConfusionMatrix<String> confusionMatrix;
	private Map<String, String> dcToHead;

	public static AnalysisEngineDescription getDescription(String outputFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DCEvaluator.class, 
				PARAM_REPORT_FILE, outputFile
				);
	}
	
	public static AnalysisEngineDescription getDescription(String outputFile, String headFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DCEvaluator.class 
				, PARAM_REPORT_FILE, outputFile
				, PARAM_DC_HEAD_FILE, headFile
				);
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			output = new PrintStream(new FileOutputStream(reportFile), true, StandardCharsets.UTF_8.name());
			confusionMatrix = new ConfusionMatrix<String>();
			if (dcHeadsFile != null){
				dcToHead = new TreeMap<String, String>();
				List<String> lines = FileUtils.readLines(new File(dcHeadsFile), StandardCharsets.UTF_8);
				for (String line: lines){
					String[] columns = line.split(":");
					dcToHead.put(columns[0].trim().toLowerCase(), columns[1].trim().toLowerCase());
				}
					
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		int sumTotal = 0, sumSystem = 0, sumCorrect = 0;
		for (String cls: confusionMatrix.getClasses()){
			int total = confusionMatrix.getActualTotal(cls);
			int system = confusionMatrix.getPredictedTotal(cls);
			int correct = confusionMatrix.getCount(cls, cls);
			
			if (!cls.equals("null")){
				printToOutput(cls, total, system, correct);
				sumTotal += total;
			}
			
			sumSystem += system;
			sumCorrect += correct;
		}
		
		output.println();
		output.println();
		printToOutput("Average", sumTotal, sumSystem, sumCorrect);
		output.close();
	}

	boolean isFirst = true;
	private void printToOutput(String key, int sumTotal, int sumSystem, int sumCorrect) {
		double precision = (double) sumCorrect / sumSystem;
		double recall = (double) sumCorrect / sumTotal;
		double f = 2 * precision * recall / (precision + recall);
		if (isFirst)
			output.println("dc\tP\tR\tF\tAC\tSC");
		output.printf("%s\t%.3f\t%.3f\t%.3f\t%5d\t%5d\n", key, precision, recall, f, sumTotal, sumSystem);
		isFirst = false;
	}
	
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			Map<Integer, DiscourseConnective> goldConnectives = getAnnotations(aJCas
					, ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW, DiscourseConnective.class);
			Map<Integer, DiscourseConnective> systemConnectives = getAnnotations(aJCas
					, CAS.NAME_DEFAULT_SOFA, DiscourseConnective.class);
			
			fillConfusionMatrix(getHeadOfDcs(goldConnectives), getHeadOfDcs(systemConnectives));
			
		} catch (CASException e) {
			e.printStackTrace();
		}

	}
	
	public Map<Integer, String> getHeadOfDcs(Map<Integer, DiscourseConnective> toConvert){
		Map<Integer, String> heads = new TreeMap<Integer, String>();
		for (Entry<Integer, DiscourseConnective> anEntry: toConvert.entrySet()){
			String dcString = TokenListTools.getTokenListText(anEntry.getValue()).toLowerCase();
			int startPosition = anEntry.getKey();
			
			if (dcToHead != null){
				dcString = dcToHead.get(dcString);
				List<Token> tokens = TokenListTools.convertToTokens(anEntry.getValue());
				for (Token token: tokens){
					String tokenStr = token.getCoveredText();
					if (dcToHead.containsKey(tokenStr)){		//normalize the dc if there are multiple spelling for the dc
						tokenStr = dcToHead.get(tokenStr);
					}
					if (dcString.startsWith(tokenStr)){
						startPosition = token.getBegin();
						break;
					}
				}
			}
			
			heads.put(startPosition, dcString);
		}
		
		return heads;
	}

	private void fillConfusionMatrix(
			Map<Integer, String> goldConnectives,
			Map<Integer, String> systemConnectives) {
		
		for (Entry<Integer, String> aSystemConnective: systemConnectives.entrySet()){
			String goldConnectiveText = goldConnectives.remove(aSystemConnective.getKey());
			if (goldConnectiveText == null)
				goldConnectiveText = "" + null;
			
			confusionMatrix.add(goldConnectiveText.toLowerCase()
					, aSystemConnective.getValue().toLowerCase());
		}
		
		for (Entry<Integer, String> aGoldConnective: goldConnectives.entrySet()){
			confusionMatrix.add(aGoldConnective.getValue().toLowerCase(), 
					"" + null);
		}
	}

	private <T extends Annotation> Map<Integer, T> getAnnotations(JCas aJCas, String view, Class<T> clazz) throws CASException{
		JCas aView = aJCas.getView(view);
		Collection<T> annotations = JCasUtil.select(aView, clazz);
		Map<Integer, T> indexedAnnotaitons = new TreeMap<Integer, T>();
		for (T annotation: annotations){
			indexedAnnotaitons.put(annotation.getBegin(), annotation);
		}
		return indexedAnnotaitons;
	}
	
}
