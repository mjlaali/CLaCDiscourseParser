package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.pickLeftMostToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;

import ca.concordia.clac.util.graph.LabeledEdge;
import ca.concordia.clac.util.graph.ShortestPathForMultiDestination;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ConstituentArg2Arg1FeatureFactory implements Function<Annotation, List<Feature>>{
	final Map<Annotation, Set<Token>> mapToTokenList;
	final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph;
	final UndirectedGraph<Token, LabeledEdge<Dependency>> dependencyUndirectedGraph;
	Set<Token> arg1Tokens;
	Set<Token> arg2Tokens;
	List<Set<Token>> argsTokens;
	

	public ConstituentArg2Arg1FeatureFactory(final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph, 
			final Map<Annotation, Set<Token>> mapToTokenList) {
		this.dependencyGraph = dependencyGraph;
		this.dependencyUndirectedGraph = new AsUndirectedGraph<>(dependencyGraph);
		this.mapToTokenList = mapToTokenList;
	}

	private void setCoveringTokens(final Set<Token> coveringArg1Tokens, final Set<Token> coveringArg2Tokens) {
		this.arg1Tokens = coveringArg1Tokens;
		this.arg2Tokens = coveringArg2Tokens;
		this.argsTokens = Arrays.asList(coveringArg1Tokens, coveringArg2Tokens);
	}

	@Override
	public List<Feature> apply(Annotation ann) {
		Set<Token> nodeTokens = mapToTokenList.get(ann);
		
		Token nodeHead = getRoots(dependencyGraph).andThen(pickLeftMostToken()).apply(nodeTokens);
		
		List<Feature> features = new ArrayList<>();
		int argIdx = 1;
		for (Set<Token> argTokens: argsTokens){
			
			List<LabeledEdge<Dependency>> path = ShortestPathForMultiDestination.findPathBetween(dependencyUndirectedGraph, nodeHead, 
					new HashSet<>(argTokens));
			if (path == null)
				path = Collections.emptyList();

			features.add(new Feature("ArgsDepPathSize_" + argIdx, path.size()));
			features.add(new Feature("ArgsDepPath_" + argIdx, 
					path.stream()
					.map(LabeledEdge::getLabel)
					.map(Dependency::getDependencyType)
					.collect(Collectors.joining("-"))));

			
			features.add(new Feature("ArgsHeadInArg_" + argIdx, Boolean.toString(argTokens.contains(nodeHead))));
			features.add(new Feature("ArgsContainAllInArg_" + argIdx, Boolean.toString(argTokens.containsAll(nodeTokens))));
			++argIdx;
		}
		
		
		return features;
	}

	public Function<Annotation, List<Feature>> getInstance(final Set<Token> coveringArg1Tokens, final Set<Token> coveringArg2Tokens){
		setCoveringTokens(coveringArg1Tokens, coveringArg2Tokens);
		return this::apply;
	}
}