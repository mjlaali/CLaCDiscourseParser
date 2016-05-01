package ca.concordia.clac.penn.analysis;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import ir.laali.tools.ds.DSManagment;
import ir.laali.tools.ds.DSPrinter;

public class ArgConstituentTypes extends JCasAnnotator_ImplBase{
	public static final String PARAM_OUTPUT_FILE = "outputFile"; 

	@ConfigurationParameter(name=PARAM_OUTPUT_FILE)
	private File outputFile;

	Map<String, Integer> patternCnt = new HashMap<>();
	Map<String, Integer> nonPatternCnt = new HashMap<>();
	Map<String, Integer> smallestConstituents = new TreeMap<>();
	
	private List<Token> dcTokens;

	public static AnalysisEngineDescription	getDescription(File outputFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(ArgConstituentTypes.class, PARAM_OUTPUT_FILE, outputFile);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Collection<DiscourseArgument> arguments = JCasUtil.select(aJCas, DiscourseArgument.class);

		Map<DiscourseArgument, Collection<Constituent>> coveringConstituents = JCasUtil.indexCovering(aJCas, DiscourseArgument.class, Constituent.class);
		Map<Constituent, Collection<Token>> constituentToTokens = JCasUtil.indexCovered(aJCas, Constituent.class, Token.class);
		

		for (DiscourseArgument argument: arguments){
			Collection<Constituent> constituents = coveringConstituents.get(argument);
			DiscourseConnective discourseConnective = argument.getDiscouresRelation().getDiscourseConnective();
			if (discourseConnective != null)
				dcTokens = TokenListTools.convertToTokens(discourseConnective);
			else
				dcTokens = Collections.emptyList();
			Constituent constituent = pickTheSmallest(constituents);
			
			if (constituent != null){
				DSManagment.incValue(smallestConstituents, constituent.getConstituentType());
				List<Annotation> pattern = new ArrayList<>();
				List<Annotation> nonPattern = new ArrayList<>();
				
				saveThePattern(constituent, new HashSet<>(TokenListTools.convertToTokens(argument)), constituentToTokens, pattern);
				saveThePatternNon(constituent, new HashSet<>(TokenListTools.convertToTokens(argument)), constituentToTokens, nonPattern);

				String strPattern = pattern.stream().map(getConstituentType()).collect(Collectors.joining("\t"));
				String strPatternNon = nonPattern.stream().map(getConstituentType()).collect(Collectors.joining("\t"));

				add(strPattern, patternCnt);
				add(strPatternNon, nonPatternCnt);
			}
		}
	}

	private void add(String strPattern, Map<String, Integer> patternCnt) {
		Integer cnt = patternCnt.get(strPattern);
		if (cnt == null)
			cnt = 0;
		patternCnt.put(strPattern, cnt + 1);
	}
	
	private boolean containAny(Set<Token> tokens, Collection<Token> toCheck){
		for (Token token: toCheck){
			if (tokens.contains(token))
				return true;
		}
		
		return false;
	}
	
	private void saveThePatternNon(Annotation ann, Set<Token> argument, Map<Constituent, Collection<Token>> constituentToTokens, List<Annotation> pattern) {
		if (ann instanceof Token){
			if (!argument.contains(ann))
				pattern.add(ann);
			
		} else if (ann instanceof Constituent){
			Constituent constituent = (Constituent)ann;
			if (!containAny(argument, getConstituentTokens(constituentToTokens, constituent))){
				pattern.add(constituent);
			} else {
				for (int i = 0; i < constituent.getChildren().size(); i++){
					saveThePattern(constituent.getChildren(i), argument, constituentToTokens, pattern);
				}
			}
		} else
			throw new RuntimeException(ann.getType().getName());
	}

	private Collection<Token> getConstituentTokens(Map<Constituent, Collection<Token>> constituentToTokens,
			Constituent constituent) {
		Set<Token> tokens = new HashSet<>(constituentToTokens.get(constituent));
		tokens.removeAll(dcTokens);
		return tokens;
	}

	private void saveThePattern(Annotation ann, Set<Token> argument, Map<Constituent, Collection<Token>> constituentToTokens, List<Annotation> pattern) {
		if (ann instanceof Token){
			if (argument.contains(ann))
				pattern.add(ann);
			
		} else if (ann instanceof Constituent){
			Constituent constituent = (Constituent)ann;
			if (argument.containsAll(getConstituentTokens(constituentToTokens, constituent))){
				pattern.add(constituent);
			} else {
				for (int i = 0; i < constituent.getChildren().size(); i++){
					saveThePattern(constituent.getChildren(i), argument, constituentToTokens, pattern);
				}
			}
		} else
			throw new RuntimeException(ann.getType().getName());
	}

	public static Constituent pickTheSmallest(Collection<Constituent> constituents) {
		if (constituents == null || constituents.size() == 0)
			return null;
		Constituent smallest = null;
		int smallestLen = Integer.MAX_VALUE;
		for (Constituent constituent: constituents){
			int len = constituent.getEnd() - constituent.getBegin();
			if (len < smallestLen){
				smallest = constituent;
				smallestLen = len;
			}

		}
		return smallest;
	}

	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		try {
			outputFile.mkdirs();
			PrintStream output = new PrintStream(new File(outputFile, "pattern.txt"));
			patternCnt.forEach((k, v) -> output.println(v + "\t" + k));
			output.close();
			
			PrintStream outputNon = new PrintStream(new File(outputFile, "nonPattern.txt"));
			nonPatternCnt.forEach((k, v) -> outputNon.println(v + "\t" + k));
			outputNon.close();
			
			PrintStream summary = new PrintStream(new File(outputFile, "summary.txt"));
			DSPrinter.printMap("smallest constituent", smallestConstituents.entrySet(), summary);
			summary.close();
		} catch (FileNotFoundException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	public static void main(String[] args) throws UIMAException, IOException {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());

		File outputFile = new File("outputs/analysis/argPatterns");
		AnalysisEngineDescription patterExporter = ArgConstituentTypes.getDescription(outputFile);

		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllGoldJsonReader, patterExporter);

	}
}
