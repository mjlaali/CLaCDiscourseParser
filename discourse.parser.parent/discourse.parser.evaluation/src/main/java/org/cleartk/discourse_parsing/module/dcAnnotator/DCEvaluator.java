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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.eval.EvaluationConstants;
import org.cleartk.eval.util.ConfusionMatrix;

import com.lexicalscope.jewel.cli.Option;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DCEvaluator extends JCasAnnotator_ImplBase{
	private static final String SEPARATOR = "-";
	private static final String PARAM_REPORT_FILE = "reportFile";
	private static final String PARAM_DC_HEAD_FILE = "dcHeadsFile";
	private static final String PARAM_INCLUDE_SENSE = "includeSense";
	
	public static final String NO_CLASS = "null";
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
	
	@ConfigurationParameter(
			name = PARAM_INCLUDE_SENSE,
			description = "include sense in the evaluation.",
			mandatory = false)
	private boolean includeSense;
	private PrintStream output;
	private ConfusionMatrix<String> confusionMatrix;
	private Map<String, String> dcToHead;

	public static AnalysisEngineDescription getDescription(File outputFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DCEvaluator.class, 
				PARAM_REPORT_FILE, outputFile
				);
	}
	
	public static AnalysisEngineDescription getDescription(String outputFile, String headFile) throws ResourceInitializationException {
		return getDescription(new File(outputFile), new File(headFile), false);
	}
	
	public static AnalysisEngineDescription getDescription(File outputFile, File headFile, boolean includeSense) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DCEvaluator.class, 
				PARAM_REPORT_FILE, outputFile, 
				PARAM_DC_HEAD_FILE, headFile,
				PARAM_INCLUDE_SENSE, includeSense
				);
	}

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		try {
			File f = new File(reportFile);
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			output = new PrintStream(new FileOutputStream(reportFile), true, StandardCharsets.UTF_8.name());
			confusionMatrix = new ConfusionMatrix<String>();
			if (dcHeadsFile != null){
				dcToHead = new TreeMap<String, String>();
				List<String> lines = FileUtils.readLines(new File(dcHeadsFile), StandardCharsets.UTF_8);
				for (String line: lines){
					String[] columns = line.split(":");
					if (columns.length == 2)
						dcToHead.put(columns[0].trim().toLowerCase(), columns[1].trim().toLowerCase());
					else
						dcToHead.put(columns[0].trim().toLowerCase(), columns[0].trim().toLowerCase());
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
		
		printConfusionMatrix(confusionMatrix);
		
		if (includeSense){
			ConfusionMatrix<String> withoutSense = new ConfusionMatrix<>();
			for (String actual: confusionMatrix.getClasses())
				for (String predicted: confusionMatrix.getClasses()){
					String predictedTrimed = trimSense(predicted);
					int count = confusionMatrix.getCount(actual, predicted);
					if (!predicted.equals("null") && !actual.equals(predicted)) {
						predictedTrimed += "-false";
						if (count > 0)
							System.out.println(actual + ", " + predicted + "=" + count );
					}
					withoutSense.add(trimSense(actual), predictedTrimed, count);
				}
			output.println();
			output.println("==========WITHOUT SENSE==============");
			printConfusionMatrix(withoutSense);
		}
		output.close();
	}

	private void printConfusionMatrix(ConfusionMatrix<String> confusionMatrix) {
		if (confusionMatrix.getClasses().size() == 0)
			return;
		
		int sumTotal = 0, sumSystem = 0, sumCorrect = 0;
		Set<String> classes = new TreeSet<>(confusionMatrix.getClasses());
		
		if (dcToHead != null)
			classes.addAll(dcToHead.values());
		for (String cls: classes){
			if (cls.equals(NO_CLASS) || cls.contains("-false")){	//NO_CLASS is a custom class that was defined and is only added to it when either gold or system contain an annotation but not the other one.
				continue;
			}
			
			int total = confusionMatrix.getActualTotal(cls);
			int system = confusionMatrix.getPredictedTotal(cls) + confusionMatrix.getPredictedTotal(cls + "-false");
			int correct = confusionMatrix.getCount(cls, cls);
			
			printToOutput(cls, total, system, correct);
			sumSystem += system;
			sumTotal += total;
			sumCorrect += correct;
		}
		
		output.println();
		output.println();
		printToOutput("Average", sumTotal, sumSystem, sumCorrect);
	}

	boolean isFirst = true;
	private void printToOutput(String key, int sumTotal, int sumSystem, int sumCorrect) {
		if (isFirst)
			output.println("P\tR\tF\tGold\tSystem\tIntersect\tdc");
		output.println(convertToString(key, sumTotal, sumSystem, sumCorrect));
		isFirst = false;
	}
	
	public static String convertToString(String key, int sumTotal, int sumSystem, int sumCorrect) {
		double precision;
		double recall;
		if (sumCorrect == 0){
			precision = sumSystem == 0 ? 1 : 0;
			recall = sumTotal == 0 ? 1 : 0;
		} else {
			precision = (double) sumCorrect / sumSystem;
			recall = (double) sumCorrect / sumTotal;
		}
		double f = 0;
		if (precision != 0 || recall != 0)
			f = 2 * precision * recall / (precision + recall);
		return String.format("%.3f\t%.3f\t%.3f\t%5d\t%5d\t%5d\t%s", precision, recall, f, sumTotal, sumSystem, sumCorrect, key);
	}
	
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			Map<Integer, DiscourseConnective> goldConnectives = getAnnotations(aJCas
					, EvaluationConstants.GOLD_VIEW, DiscourseConnective.class);
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
			
			if (dcToHead != null && dcToHead.containsKey(dcString)){
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
			
			if (includeSense){
				dcString += SEPARATOR + anEntry.getValue().getSense();
			}
			
			heads.put(startPosition, dcString);
		}
		
		return heads;
	}
	
	private String trimSense(String dcWithSense){
		int endOfDc = dcWithSense.indexOf(SEPARATOR);
		if (endOfDc == -1)
			return dcWithSense;
		return dcWithSense.substring(0, endOfDc);
	}

	private void fillConfusionMatrix(
			Map<Integer, String> goldConnectives,
			Map<Integer, String> systemConnectives) {
		
		for (Entry<Integer, String> aSystemConnective: systemConnectives.entrySet()){
			String goldConnectiveText = goldConnectives.remove(aSystemConnective.getKey());
			if (goldConnectiveText == null)
				goldConnectiveText = NO_CLASS;
			
			confusionMatrix.add(goldConnectiveText.toLowerCase()
					, aSystemConnective.getValue().toLowerCase());
		}
		
		for (Entry<Integer, String> aGoldConnective: goldConnectives.entrySet()){
			confusionMatrix.add(aGoldConnective.getValue().toLowerCase(), 
					NO_CLASS);
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

	interface Options{
		@Option(
				shortName = "m",
				longName = "model", 
				description = "model dir")
		public File getModelFile();
		
		@Option(
				shortName = "o",
				longName = "output", 
				description = "output directory"
				)
		public File getOutputDir();

	}
	
	public static void main(String[] args) throws Exception{
	}
	
}
