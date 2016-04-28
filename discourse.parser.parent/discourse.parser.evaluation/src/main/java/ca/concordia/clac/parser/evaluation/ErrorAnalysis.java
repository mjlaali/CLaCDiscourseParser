package ca.concordia.clac.parser.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.component.ViewTextCopierAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse.type.TokenList;

import ca.concordia.clac.uima.engines.ViewAnnotationCopier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ErrorAnalysis extends JCasAnnotator_ImplBase {
	public static enum ErrorType{
		IncorrectSense, DCNotIdentified, ParallelDC, ConnectiveIsNotDC, Arg1, Arg2, TotalRelationCnt, Correct, TotalIdentified
	}
	
	public static final String GOLD_VIEW = "goldView";
	public static final String SYSTEM_VIEW = "systemView";
	
	public static final String PARAM_OUTPUT_DIR = "outputDir";
	
	@ConfigurationParameter(name = PARAM_OUTPUT_DIR)
	private File outputDir;

	private Map<ErrorType, Integer> errorCount = new HashMap<>();
	private Map<String, Integer> parallelDCCoung = new HashMap<>();
	private Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();
	
	public static AnalysisEngineDescription getDescription(File outputDir) throws ResourceInitializationException{
		return AnalysisEngineFactory.createEngineDescription(ErrorAnalysis.class, PARAM_OUTPUT_DIR, outputDir);
	}
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		if (outputDir.exists())
			try {
				FileUtils.deleteDirectory(outputDir);
			} catch (IOException e) {
				throw new ResourceInitializationException(e);
			}
		
		for (ErrorType errorType: ErrorType.values()){
			errorCount.put(errorType, 0);
		}

		outputDir.mkdirs();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		try {
			
			JCas goldView = aJCas.getView(GOLD_VIEW);
			
			JCas systemView = aJCas.getView(SYSTEM_VIEW);
			evaluateExplicitRelations(goldView, systemView);
		} catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void evaluateExplicitRelations(JCas goldView, JCas systemView) throws AnalysisEngineProcessException {
		Set<DiscourseConnective> goldConnectives = new HashSet<>(JCasUtil.select(goldView, DiscourseConnective.class));
		Set<DiscourseConnective> systemConnectives = new HashSet<>(JCasUtil.select(systemView, DiscourseConnective.class));
		
		errorCount.put(ErrorType.TotalRelationCnt, errorCount.get(ErrorType.TotalRelationCnt) + goldConnectives.size());
		errorCount.put(ErrorType.TotalIdentified, errorCount.get(ErrorType.TotalIdentified) + systemConnectives.size());
		
		List<Pair<DiscourseConnective, DiscourseConnective>> incorrectRelations = new ArrayList<>();
		List<DiscourseConnective> notDetected = new ArrayList<>();
		List<DiscourseConnective> invalidConnectives = new ArrayList<>();
		
		for (DiscourseConnective aGoldConnective: goldConnectives){
			List<DiscourseConnective> alignedSystemConnectives = findMatchedSystemDC(aGoldConnective, systemView);
			if (alignedSystemConnectives.isEmpty()){
				addError(ErrorType.DCNotIdentified);
				notDetected.add(aGoldConnective);
			}
			
			systemConnectives.removeAll(alignedSystemConnectives);
			
			for (DiscourseConnective aSystemConnective: alignedSystemConnectives){
				if (!hasSameRelation(aGoldConnective, aSystemConnective)){
					incorrectRelations.add(new Pair<DiscourseConnective, DiscourseConnective>(aGoldConnective, aSystemConnective));
				} else
					addError(ErrorType.Correct);
			}
		}
		
		invalidConnectives.addAll(systemConnectives);
		errorCount.put(ErrorType.ConnectiveIsNotDC, errorCount.get(ErrorType.ConnectiveIsNotDC) + systemConnectives.size());
		
		try {
			report(Tools.getDocName(systemView), incorrectRelations, notDetected, invalidConnectives);
		} catch (FileNotFoundException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	

	private void report(String docName, List<Pair<DiscourseConnective, DiscourseConnective>> incorrectRelations,
			List<DiscourseConnective> notDetected, List<DiscourseConnective> invalidConnectives) throws FileNotFoundException {
		PrintStream dcErrors = new PrintStream(new File(outputDir, docName + "-dc.txt"));
		
		dcErrors.println("Not Detected:\n=========================\n");
		notDetected.stream().map(ErrorAnalysis::relationToString).forEach(dcErrors::println);
		dcErrors.println("\n\nInvalid Connectives:\n=========================\n");
		invalidConnectives.stream().map(TokenListTools::getTokenListText).forEach(dcErrors::println);
		
		dcErrors.close();
		
		PrintStream relationErrors = new PrintStream(new File(outputDir, docName + "-relations.txt"));
		incorrectRelations.stream().map(ErrorAnalysis::pairRelationToString).forEach(relationErrors::println);
		relationErrors.close();
	}
	
	private static String pairRelationToString(Pair<DiscourseConnective, DiscourseConnective> pair){
		return relationToString(pair.getFirst()) + "\n\n" + relationToString(pair.getSecond()) + "\n\n===================\n\n";
	}

	private static String relationToString(DiscourseConnective connective){
		DiscourseRelation relation = connective.getDiscourseRelation();
		return String.format("Text: %s\nArg1: %s\nArg2: %s\nSense: %s\nConnective: %s\n",
				relation.getCoveredText(),
				TokenListTools.getTokenListText(relation.getArguments(0)),
				TokenListTools.getTokenListText(relation.getArguments(1)),
				relation.getSense(),
				TokenListTools.getTokenListText(relation.getDiscourseConnective()));
	}
	
	private List<DiscourseConnective> findMatchedSystemDC(DiscourseConnective aGoldConnective,
			JCas systemView) {
		int coverdToken = JCasUtil.selectCovered(systemView, Token.class, aGoldConnective.getBegin(), aGoldConnective.getEnd()).size();
		
		if (coverdToken != aGoldConnective.getTokens().size()){	//this is a parallel discourse connective
			addError(ErrorType.ParallelDC);
			String parallelDc = TokenListTools.getTokenListText(aGoldConnective);
//			parallelDc += "<>" + aGoldConnective.getCoveredText();
//			parallelDc += "[" + coverdToken + "<>" + aGoldConnective.getTokens().size() + "]";
			Integer cnt = parallelDCCoung.get(parallelDc);
			if (cnt == null){
				parallelDCCoung.put(parallelDc, 1);
			} else
				parallelDCCoung.put(parallelDc, cnt + 1);
			return Collections.emptyList();
		}
		
		List<DiscourseConnective> covering = JCasUtil.selectCovering(systemView, DiscourseConnective.class, aGoldConnective.getBegin(), aGoldConnective.getEnd());
		return covering;
	}

	private void addError(ErrorType errorType){
		errorCount.put(errorType, errorCount.get(errorType) + 1);
	}
	
	private boolean hasSameRelation(DiscourseConnective aGoldConnective, DiscourseConnective aSystemConnective) {
		DiscourseRelation goldRelation = aGoldConnective.getDiscourseRelation();
		DiscourseRelation systemRelation = aSystemConnective.getDiscourseRelation();
		
		if (!isEqualTokenList(goldRelation.getArguments(0), systemRelation.getArguments(0))){
			addError(ErrorType.Arg1);
			return false;
		}
		if (!isEqualTokenList(goldRelation.getArguments(1), systemRelation.getArguments(1))){
			addError(ErrorType.Arg2);
			return false;
		}
		
		addToConfusionMatrix(goldRelation.getSense(), systemRelation.getSense());
		if (!goldRelation.getSense().equals(systemRelation.getSense())){
			addError(ErrorType.IncorrectSense);
			return false;
		}
				
		return true;
	}
	
	private void addToConfusionMatrix(String gold, String system) {
		Map<String, Integer> outputs = confusionMatrix.get(gold);
		if (outputs == null){
			outputs = new HashMap<>();
			confusionMatrix.put(gold, outputs);
		}
		
		Integer cnt = outputs.get(system);
		if (cnt == null)
			outputs.put(system, 1);
		else
			outputs.put(system, cnt + 1);
	}

	private static boolean isEqualTokenList(TokenList first, TokenList second){
		List<Token> firstTokens = TokenListTools.convertToTokens(first);
		List<Token> secondTokens = TokenListTools.convertToTokens(second);
		
		HashSet<Integer> firstSet = firstTokens.stream().map(Token::getBegin).collect(Collectors.toCollection(HashSet::new));
		HashSet<Integer> secondSet = secondTokens.stream().map(Token::getBegin).collect(Collectors.toCollection(HashSet::new));
		return firstSet.containsAll(secondSet) && secondSet.containsAll(firstSet);
	}
	
	public static AnalysisEngineDescription getGoldPipeline(ConllDatasetPath dataset) throws ResourceInitializationException{
		AggregateBuilder builder = new AggregateBuilder();
		
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getDataJSonFile());
		
		builder.add(conllSyntaxJsonReader);
		builder.add(conllGoldJsonReader);
		
		return builder.createAggregateDescription();
	}

	private static AnalysisEngineDescription getSystemPipeline(ConllDatasetPath dataset) throws ResourceInitializationException, MalformedURLException, URISyntaxException {
		AggregateBuilder builder = new AggregateBuilder();

		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = new CLaCParser().getParser();

		builder.add(conllSyntaxJsonReader);
		builder.add(conllGoldJsonReader);
		
		return builder.createAggregateDescription();
	}
	private static void addAView(String aNewView, AggregateBuilder builder) throws ResourceInitializationException {
		AnalysisEngineDescription createView = AnalysisEngineFactory.createEngineDescription(ViewCreatorAnnotator.class, 
				ViewCreatorAnnotator.PARAM_VIEW_NAME, aNewView);
		AnalysisEngineDescription viewTextCopier = AnalysisEngineFactory.createEngineDescription(ViewTextCopierAnnotator.class,
				ViewTextCopierAnnotator.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA,
				ViewTextCopierAnnotator.PARAM_DESTINATION_VIEW_NAME, aNewView);
		AnalysisEngineDescription viewAnnotationCopier = AnalysisEngineFactory.createEngineDescription(ViewAnnotationCopier.class,
				ViewAnnotationCopier.PARAM_SOURCE_VIEW_NAME, CAS.NAME_DEFAULT_SOFA,
				ViewAnnotationCopier.PARAM_TARGET_VIEW_NAME, aNewView);
		
		builder.add(createView);
		builder.add(viewTextCopier);
		builder.add(viewAnnotationCopier);
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		try {
			PrintStream summary = new PrintStream(new File(outputDir, "summary.txt"));
			errorCount.forEach((type, cnt) -> summary.println(type.toString() + ":\t" + cnt));
			
			summary.println("====Parallel DC=====");
			parallelDCCoung.forEach((type, cnt) -> summary.println(type + ":\t" + cnt));
			
			summary.println("====Confusion Matrix=====");
			confusionMatrix.forEach((gold, row) -> row.forEach((system, cnt) -> summary.println(gold + "\t" + system + "\t" + cnt)));
			
			summary.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) throws URISyntaxException, UIMAException, IOException {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.dev);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		
		AggregateBuilder builder = new AggregateBuilder();
		
		addAView(GOLD_VIEW, builder);
		addAView(SYSTEM_VIEW, builder);

		
		
		builder.add(getGoldPipeline(dataset), CAS.NAME_DEFAULT_SOFA, GOLD_VIEW);
		builder.add(getSystemPipeline(dataset), CAS.NAME_DEFAULT_SOFA, SYSTEM_VIEW);
		
		File outputDir = new File("outputs/errorAnalysis");
		builder.add(getDescription(outputDir));
		
		SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
	}

}
