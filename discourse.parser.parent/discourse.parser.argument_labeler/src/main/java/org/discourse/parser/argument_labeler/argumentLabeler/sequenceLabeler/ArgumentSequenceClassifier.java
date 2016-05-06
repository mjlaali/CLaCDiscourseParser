package org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler;

import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.collect;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;
import org.discourse.parser.argument_labeler.argumentLabeler.LabelExtractor;
import org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler.copy.DCTreeNodeArgInstance;

import ca.concordia.clac.discourse.parser.dc.disambiguation.DiscourseVsNonDiscourseClassifier;
import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import ca.concordia.clac.ml.feature.TreeFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class ArgumentSequenceClassifier implements SequenceClassifierAlgorithmFactory<String, DiscourseConnective, DCTreeNodeArgInstance>{
	Map<Constituent, List<Token>> constituentToCoveredTokens = new HashMap<>();
	JCas jcas = null;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<Constituent, List<Token>> initConstituentToCoveredTokens(JCas jCas) {
		if (!jCas.equals(jcas)){
			constituentToCoveredTokens.clear();
			JCasUtil.indexCovered(jCas, Constituent.class, Token.class).forEach((cns, tokens) -> 
			constituentToCoveredTokens.put(cns, (List)tokens));
		}
		return constituentToCoveredTokens;
	}

	@Override
	public Function<JCas, ? extends Collection<? extends DiscourseConnective>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, DiscourseConnective.class);
	}

	@Override
	public Function<DiscourseConnective, List<DCTreeNodeArgInstance>> getInstanceExtractor(JCas aJCas) {
		return new ArgumentInstanceExtractor(aJCas);
	}
	
	public BiFunction<DCTreeNodeArgInstance, DiscourseConnective, List<Feature>> getArgumentFeatureExtractor(){
		BiFunction<DCTreeNodeArgInstance, DiscourseConnective, List<Feature>> dcFeatures = 
				(ins, dc) -> DiscourseVsNonDiscourseClassifier.getDiscourseConnectiveFeatures().apply(dc);
			
		BiFunction<DCTreeNodeArgInstance, DiscourseConnective, List<Feature>> constituentFeatures = getConstituentFeatures();
				
		BiFunction<DCTreeNodeArgInstance, DiscourseConnective, Feature> posFeature = (inst, dc) -> {
			boolean left = inst.getNode().getBegin() < dc.getBegin();
			return makeFeature("CON-NT-Position").apply(Boolean.toString(left));
		};
		
		return multiBiFuncMap(dcFeatures, multiBiFuncMap(posFeature), constituentFeatures).andThen(flatMap(Feature.class));
	}

	private BiFunction<DCTreeNodeArgInstance, DiscourseConnective, List<Feature>> getConstituentFeatures() {
		Function<DCTreeNodeArgInstance, Annotation> convertToConstituent = DCTreeNodeArgInstance::getNode;
		Function<DCTreeNodeArgInstance, Feature> childPatterns =
				convertToConstituent.andThen(
						TreeFeatureExtractor.getChilderen()).andThen(
								mapOneByOneTo(TreeFeatureExtractor.getConstituentType())).andThen(
										collect(Collectors.joining("-"))).andThen(
												makeFeature("ChildPat"));
		Function<DCTreeNodeArgInstance, Feature> ntCtx = convertToConstituent
				.andThen(multiMap(
						getConstituentType(), 
						getParent().andThen(getConstituentType()), 
						getLeftSibling().andThen(getConstituentType()),
						getRightSibling().andThen(getConstituentType())
						))
				.andThen(collect(Collectors.joining("-")))
				.andThen(makeFeature("NT-Ctx"));
		
		Function<DCTreeNodeArgInstance, List<Annotation>> pathExtractor = 
				(inst) -> getPath().apply(inst.getImediateDcParent(), inst.getNode()); 
		Function<DCTreeNodeArgInstance, Feature> path = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(collect(Collectors.joining("-")))
				.andThen(makeFeature("CON-NT-Path"));

		Function<DCTreeNodeArgInstance, Feature> pathSize = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(collect(Collectors.counting()))
				.andThen(makeFeature("CON-NT-Path-Size"));

		Function<? super Annotation, ? extends List<Token>> getTokens = (cns) -> {
			List<Token> results = null;
			if (cns instanceof Constituent)
				results = constituentToCoveredTokens.get(cns);
			else if (cns instanceof Token)
				results = Collections.singletonList((Token) cns);
			
			return results;
		};
		
		Function<Token, Optional<Token>> getPrevToken = (token) -> {
			Token result = null;
			List<Token> precedings = JCasUtil.selectPreceding(Token.class, token, 1);
			if (precedings.size() > 0)
				result = precedings.get(0);
			
			return Optional.ofNullable(result);
		};
		
		Function<Token, Optional<Token>> getNextToken = (token) -> {
			Token result = null;
			List<Token> nexts = JCasUtil.selectFollowing(Token.class, token, 1);
			if (nexts.size() > 0)
				result = nexts.get(0);
			
			return Optional.ofNullable(result);
		};
		
		Function<DCTreeNodeArgInstance, Feature> constituentFirstToken = convertToConstituent.andThen(getTokens)
				.andThen((childeren) -> childeren.get(0))
				.andThen(Token::getCoveredText).andThen(String::toLowerCase).andThen(makeFeature("firstToken"));
		
		Function<DCTreeNodeArgInstance, Feature> tokenBeforeFirstToken = convertToConstituent.andThen(getTokens)
				.andThen((childeren) -> childeren.get(0)).andThen(getPrevToken)
				.andThen((opt) -> opt.map(Token::getCoveredText).orElse("null")).andThen(String::toLowerCase)
				.andThen(makeFeature("tokenBeforeFirst"));

		Function<DCTreeNodeArgInstance, Feature> constituentLastToken = convertToConstituent.andThen(getTokens)
				.andThen((childeren) -> childeren.get(childeren.size() - 1))
				.andThen(Token::getCoveredText).andThen(String::toLowerCase).andThen(makeFeature("lastToken"));

		Function<DCTreeNodeArgInstance, Feature> tokenAfterLastToken = convertToConstituent.andThen(getTokens)
				.andThen((childeren) -> childeren.get(childeren.size() - 1)).andThen(getNextToken)
				.andThen((opt) -> opt.map(Token::getCoveredText).orElse("null")).andThen(String::toLowerCase)
				.andThen(makeFeature("tokenAfterLast"));

		Predicate<Token> isVerb = (token) -> {
			if (token.getPos().getPosValue().startsWith("V"))
				return true;
			return false;
		};
		
		
		
		Function<List<Token>, Optional<Token>> getMainVerb = (tokens) -> {
			List<Token> verbs = tokens.stream().filter(isVerb).collect(Collectors.toList());
			Token last = null;
			if (verbs.size() > 0)
				last = verbs.get(verbs.size() - 1);
			return Optional.ofNullable(last);
		};
		
		Function<DCTreeNodeArgInstance, Feature> mainVerb = convertToConstituent.andThen(getTokens)
				.andThen(getMainVerb).andThen((verb) -> verb.map(Token::getCoveredText).orElse("null"))
				.andThen(makeFeature("mainVerb"));

		
		BiFunction<DCTreeNodeArgInstance, DiscourseConnective, List<Feature>> constituentFeatures =
				(inst, dc) -> multiMap(childPatterns, ntCtx, path, pathSize
						, constituentFirstToken, constituentLastToken,
						tokenBeforeFirstToken, tokenAfterLastToken, mainVerb
						).apply(inst);
				
				
		return constituentFeatures;
	}
	

	@Override
	public BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		initConstituentToCoveredTokens(jCas);
		BiFunction<DCTreeNodeArgInstance, DiscourseConnective, List<Feature>> biFunc = 
				getArgumentFeatureExtractor();
		return mapOneByOneTo(biFunc);
	}

	@Override
	public BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<String>> getLabelExtractor(JCas jCas) {
		initConstituentToCoveredTokens(jCas);
		return mapOneByOneTo(new LabelExtractor(false, constituentToCoveredTokens));
	}

	@Override
	public SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance> getLabeller(JCas jCas) {
		initConstituentToCoveredTokens(jCas);
		return new ArgumentConstructor(jCas, constituentToCoveredTokens);
	}


	public static AnalysisEngineDescription getWriterDescription(String outputDirectory, boolean mallet) throws ResourceInitializationException {
		if (mallet)
			return StringSequenceClassifier.getWriterDescription(ArgumentSequenceClassifier.class,
					MalletCrfStringOutcomeDataWriter.class, new File(outputDirectory));
		
		return StringSequenceClassifier.getViterbiWriterDescription(ArgumentSequenceClassifier.class,
				WekaStringOutcomeDataWriter.class, new File(outputDirectory)); 
		
		
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String modelLocation) throws ResourceInitializationException {
		return getClassifierDescription(modelLocation, null, null);
	}
	public static AnalysisEngineDescription getClassifierDescription(String modelLocation, String goldView, String systemView) throws ResourceInitializationException {
		return StringSequenceClassifier.getClassifierDescription(goldView, systemView, "Arg2", 
				ArgumentSequenceClassifier.class, modelLocation);
	}
}
