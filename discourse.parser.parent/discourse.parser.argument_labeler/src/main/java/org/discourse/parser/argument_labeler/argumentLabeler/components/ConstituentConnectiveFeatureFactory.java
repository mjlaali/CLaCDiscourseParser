package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static ca.concordia.clac.ml.feature.FeatureExtractors.dummyFunc;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getPath;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.collect;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.pickLeftMostToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;

import ca.concordia.clac.util.graph.LabeledEdge;
import ca.concordia.clac.util.graph.ShortestPathForMultiDestination;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class ConstituentConnectiveFeatureFactory {
	final Map<Annotation, Set<Token>> mapToTokenList;
	final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph;
	final UndirectedGraph<Token, LabeledEdge<Dependency>> dependencyUndirectedGraph;
	
	public ConstituentConnectiveFeatureFactory(final DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph, 
			final Map<Annotation, Set<Token>> mapToTokenList) {
		this.dependencyGraph = dependencyGraph;
		this.dependencyUndirectedGraph = new AsUndirectedGraph<>(dependencyGraph);
		this.mapToTokenList = mapToTokenList;
	}


	public BiFunction<Annotation, DiscourseConnective, List<Feature>> getInstance(){
		BiFunction<Annotation, DiscourseConnective, List<Annotation>> pathExtractor = 
				(cons, dc) -> getPath().apply(cons, dc); 
		BiFunction<Annotation, DiscourseConnective, Feature> path = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(collect(Collectors.joining("-")))
				.andThen(makeFeature("ConstituentDCPath"));

		BiFunction<Annotation, DiscourseConnective, Feature> pathSize = pathExtractor
				.andThen(mapOneByOneTo(getConstituentType()))
				.andThen(collect(Collectors.counting()))
				.andThen((l) -> "" + l)
				.andThen(makeFeature("ConstituentDCPathSize"));
		
		Function<Annotation, Token> annotationHead = dummyFunc(Annotation.class)
				.andThen(mapToTokenList::get)
				.andThen(getRoots(dependencyGraph))
				.andThen(pickLeftMostToken());
		
		BiFunction<Annotation, DiscourseConnective, Feature> depPathFeature = (ann, dc) -> {
			Token nodeHead = annotationHead.apply(ann);
			List<LabeledEdge<Dependency>> depPath = ShortestPathForMultiDestination
					.findPathBetween(dependencyUndirectedGraph, nodeHead, 
					new HashSet<>(TokenListTools.convertToTokens(dc)));
			
			if (depPath == null){
				depPath = Collections.emptyList();
			}
			
			String strPath = depPath.stream()
					.map(LabeledEdge::getLabel)
					.map(Dependency::getDependencyType)
					.collect(Collectors.joining("-"));
			return makeFeature("ConstituentDCDependencyPath").apply(strPath);
		};
		
		return multiBiFuncMap(path, pathSize, depPathFeature);
	}
}
