package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.pickLeftMostToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.ml.Feature;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;

import ca.concordia.clac.util.graph.LabeledEdge;
import ca.concordia.clac.util.graph.ShortestPathForMultiDestination;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class DependencyFeatures implements BiFunction<Annotation, ArgumentTreeNode, List<Feature>>{
	final Map<Annotation, Set<Token>> mapToTokenList;
	final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph;
	final UndirectedGraph<Token, LabeledEdge<Dependency>> dependencyUndirectedGraph;

	public DependencyFeatures(final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph, 
			Map<Annotation, Set<Token>> mapToTokenList) {
		this.dependencyGraph = dependencyGraph;
		this.dependencyUndirectedGraph = new AsUndirectedGraph<>(dependencyGraph);
		this.mapToTokenList = mapToTokenList;
	}

	@Override
	public List<Feature> apply(Annotation ann, ArgumentTreeNode treeNode) {
		Set<Token> nodeTokens = mapToTokenList.get(ann);
		
		DiscourseArgument nodeArgument = treeNode.getDiscourseArgument();
		DiscourseArgument otherArgument = null;
		for (int i = 0; i < 2; i++){
			DiscourseArgument anArg = treeNode.getDiscourseArgument().getDiscouresRelation().getArguments(i);
			if (!anArg.equals(nodeArgument))
				otherArgument = anArg;
		}
		
		Token nodeHead = getRoots(dependencyGraph).andThen(pickLeftMostToken()).apply(nodeTokens);
		
		List<Token> otherArgumentTokens = TokenListTools.convertToTokens(otherArgument);
		List<LabeledEdge<Dependency>> path = ShortestPathForMultiDestination.findPathBetween(dependencyUndirectedGraph, nodeHead, 
				new HashSet<>(otherArgumentTokens));
		if (path == null)
			path = Collections.emptyList();
		
		List<Feature> features = new ArrayList<>();
		features.add(new Feature("DepPathSize", path.size()));
		features.add(new Feature("DepPath", 
				path.stream()
				.map(LabeledEdge::getLabel)
				.map(Dependency::getDependencyType)
				.collect(Collectors.joining("-"))));
		
		return features;
	}
	
}