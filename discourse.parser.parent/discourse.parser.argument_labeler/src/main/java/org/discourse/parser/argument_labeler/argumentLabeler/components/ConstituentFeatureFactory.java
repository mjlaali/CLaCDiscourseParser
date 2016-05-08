package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.dummyFunc;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiMapList;
import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.collect;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.pickLeftMostToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.jgrapht.DirectedGraph;

import ca.concordia.clac.ml.feature.TreeFeatureExtractor;
import ca.concordia.clac.util.graph.LabeledEdge;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ConstituentFeatureFactory {
	final Map<Annotation, Set<Token>> mapToTokenList;
	final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph;
	final Map<Annotation, List<Token>> constituentToCoveredTokens;
	
	/**
	 * 
	 * @param constituentToCoveredTokens add both constituents and tokens to this list
	 */
	public ConstituentFeatureFactory(Map<Annotation, Set<Token>> mapToTokenList, 
			DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph,
			Map<Annotation, List<Token>> constituentToCoveredTokens){
		this.mapToTokenList = mapToTokenList;
		this.dependencyGraph = dependencyGraph;
		this.constituentToCoveredTokens = constituentToCoveredTokens;
	}
	
	public Function<Annotation, List<Feature>> getInstance(){
		List<Function<Annotation, Feature>> features = new ArrayList<>();
		
		Function<Annotation, Feature> childPatterns =dummyFunc(Annotation.class)
				.andThen(TreeFeatureExtractor.getChilderen())
				.andThen(mapOneByOneTo(TreeFeatureExtractor.getConstituentType()))
				.andThen(collect(Collectors.joining("-")))
				.andThen(makeFeature("ConstituentChildPattern"));
		features.add(childPatterns);
		
		Function<Annotation, Feature> ntCtx = dummyFunc(Annotation.class)
				.andThen(multiMap(
						getConstituentType(), 
						getParent().andThen(getConstituentType()), 
						getLeftSibling().andThen(getConstituentType()),
						getRightSibling().andThen(getConstituentType())
						))
				.andThen(collect(Collectors.joining("-")))
				.andThen(makeFeature("ConstituentContext"));
		features.add(ntCtx);
		
		//setup sugar functions for calculating features
		Function<? super Annotation, ? extends List<Token>> getTokens = (cns) -> constituentToCoveredTokens.get(cns);
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
		//end sugar functions
		
		
		Function<Annotation, Feature> constituentFirstToken = dummyFunc(Annotation.class)
				.andThen(getTokens)
				.andThen((childeren) -> childeren.get(0))
				.andThen(Token::getCoveredText)
				.andThen(String::toLowerCase)
				.andThen(makeFeature("ConstituentFirstToken"));
		features.add(constituentFirstToken);

		
		Function<Annotation, Feature> tokenBeforeFirstToken = dummyFunc(Annotation.class)
				.andThen(getTokens)
				.andThen((childeren) -> childeren.get(0))
				.andThen(getPrevToken)
				.andThen((opt) -> opt.map(Token::getCoveredText).orElse("null"))
				.andThen(String::toLowerCase)
				.andThen(makeFeature("ConstituentPrevToken"));
		features.add(tokenBeforeFirstToken);

		Function<Annotation, Feature> constituentLastToken = dummyFunc(Annotation.class)
				.andThen(getTokens)
				.andThen((childeren) -> childeren.get(childeren.size() - 1))
				.andThen(Token::getCoveredText)
				.andThen(String::toLowerCase)
				.andThen(makeFeature("ConstituentLastToken"));
		features.add(constituentLastToken);

		Function<Annotation, Feature> tokenAfterLastToken = dummyFunc(Annotation.class)
				.andThen(getTokens)
				.andThen((childeren) -> childeren.get(childeren.size() - 1))
				.andThen(getNextToken)
				.andThen((opt) -> opt.map(Token::getCoveredText).orElse("null"))
				.andThen(String::toLowerCase)
				.andThen(makeFeature("ConstituentNextToken"));
		features.add(tokenAfterLastToken);
		
		Function<Annotation, Feature> constituentHead = dummyFunc(Annotation.class)
				.andThen(getTokens)
				.andThen(getRoots(dependencyGraph))
				.andThen(pickLeftMostToken())
				.andThen(makeFeature("ConstituentHead"));
		features.add(constituentHead);
				
		return multiMapList(features);
	}
}
