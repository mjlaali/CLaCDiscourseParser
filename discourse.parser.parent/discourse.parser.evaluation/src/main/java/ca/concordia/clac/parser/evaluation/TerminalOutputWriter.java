package ca.concordia.clac.parser.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.discourse.type.TokenList;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class TerminalOutputWriter extends JCasAnnotator_ImplBase{
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = false)
	File outputFile;
	
	PrintStream output = System.out;
	
	Map<FeatureStructure, Integer> featureToId = new HashMap<>();
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		if (outputFile != null){
			try {
				output = new PrintStream(new FileOutputStream(outputFile));
			} catch (FileNotFoundException e) {
				throw new ResourceInitializationException(e);
			}
		}
	}
	
	public static AnalysisEngineDescription getDescription(File output) throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(TerminalOutputWriter.class, 
				PARAM_OUTPUT_FILE, output);
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		output.close();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		featureToId.clear();
		Collection<TokenList> selected = JCasUtil.select(aJCas, TokenList.class);
		
		List<TokenList> sortedByStart = new ArrayList<>(selected);
		List<TokenList> sortedByEnd = new ArrayList<>(selected);
		
		Collections.sort(sortedByStart, (a, b) -> {
			int diff = a.getBegin() - b.getBegin();
			if (diff == 0){
				diff = b.getEnd() - a.getEnd();
			}
				
			return diff;
		});

		Collections.sort(sortedByEnd, (a, b) -> {
			int diff = a.getEnd() - b.getEnd();
			if (diff == 0){
				diff = a.getBegin() - b.getBegin();
			}
				
			return diff;
		});

		String documentText = aJCas.getDocumentText();
		int currentTokenList = 0;
		int activeTokenList = 0;
		
		if (sortedByStart.isEmpty()){
			System.out.println(documentText);
		} else {
			for (int i = 0; i < documentText.length(); i++){
				while (activeTokenList < sortedByEnd.size() && i == sortedByEnd.get(activeTokenList).getEnd()){
					TokenList annotation = sortedByEnd.get(activeTokenList);
					output.printf("</%s id=\"%s\">", annotation.getType().getShortName(), getId(annotation));
					activeTokenList++;
				}
				
				while (currentTokenList < sortedByStart.size() && i == sortedByStart.get(currentTokenList).getBegin()){
					printTokenList(sortedByStart, currentTokenList);
					currentTokenList++;
				}
				output.print(documentText.charAt(i));
			}
		}
		
	}

	private void printTokenList(List<TokenList> sortedTokenList, int activeTokenList) {
		TokenList toBePrinted = sortedTokenList.get(activeTokenList);
		output.printf("<%s", toBePrinted.getType().getShortName());
		output.printf(" id=\"%s\"", getId(toBePrinted));
		for (Feature feature: toBePrinted.getType().getFeatures()){
			try {
				switch (feature.getShortName()) {
				case "begin":
				case "end":
				case "classLabel":
				case "sofa":
				case "tokens":
					break;
				default:
					String value = converToString(toBePrinted, feature);
					output.printf(" %s=\"%s\"", feature.getShortName(), value);
					break;
				}
			} catch (CASRuntimeException e) {
				output.printf(" %s=%d", feature.getShortName(), getId(toBePrinted.getFeatureValue(feature)));
			}
		}
		output.print(">");
	}

	private String converToString(TokenList toBePrinted, Feature feature) {
		if (feature.getRange().isArray()){
			FSArray array = (FSArray) toBePrinted.getFeatureValue(feature);
			List<String> res = new ArrayList<>();
			for (int i = 0; i < array.size(); i++){
				res.add(getId(array.get(i)));
			}
			return res.toString();
		}
		return safeToString(toBePrinted, feature);
	}

	private String safeToString(TokenList toBePrinted, Feature feature){
		try {
			return toBePrinted.getFeatureValueAsString(feature);
		} catch (CASRuntimeException e) {
			return getId(toBePrinted.getFeatureValue(feature));
		}
	}
	
	private String getId(FeatureStructure annotaiton) {
		Integer id = featureToId.get(annotaiton);
		if (id == null){
			id = featureToId.size();
			featureToId.put(annotaiton, featureToId.size());
		}
		return "<" + id + ">";
	}

	public static void main(String[] args) throws UIMAException, IOException {
		ConllDatasetPathFactory factor = new ConllDatasetPathFactory();
		ConllDatasetPath dataset = factor.makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.trial);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getDataJSonFile());
		
		AnalysisEngineDescription output = getDescription(new File("outputs/relations.txt"));
//		AnalysisEngineDescription output = getDescription(null);
		
		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllGoldJsonReader, output);
	}
	
}
