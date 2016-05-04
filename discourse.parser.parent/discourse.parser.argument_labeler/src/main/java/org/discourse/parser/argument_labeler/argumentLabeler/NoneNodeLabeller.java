package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getDependencyGraph;
import static ca.concordia.clac.ml.feature.FeatureExtractors.flatMap;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFunction;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeBiFunc;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.feature.GraphFeatureExtractors.getRoots;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getProductRule;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getTokenList;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.pickLeftMostToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.mallet.MalletCrfStringOutcomeDataWriter;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;
import org.jgrapht.DirectedGraph;

import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import ca.concordia.clac.util.graph.LabeledEdge;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class NoneNodeLabeller implements SequenceClassifierAlgorithmFactory<String, ArgumentTreeNode, Annotation>{

	Map<Annotation, Set<Token>> constituentToCoveredTokens = new HashMap<>();
	DirectedGraph<Token, LabeledEdge<Dependency>> dependencyGraph;

	JCas jcas = null;
	
	public Map<Annotation, Set<Token>> init(JCas jCas) {
		if (!jCas.equals(jcas)){
			constituentToCoveredTokens.clear();
			JCasUtil.indexCovered(jCas, Constituent.class, Token.class).forEach((cns, tokens) -> 
			constituentToCoveredTokens.put(cns, new HashSet<>(tokens)));
			for (Token token: JCasUtil.select(jCas, Token.class)){
				constituentToCoveredTokens.put(token, new HashSet<>(Collections.singletonList(token)));
			};
			
			dependencyGraph = getDependencyGraph(jCas);
		}
		return constituentToCoveredTokens;
	}

	@Override
	public Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> getSequenceExtractor(JCas jCas) {
		return (aJCas) -> JCasUtil.select(aJCas, ArgumentTreeNode.class);
	}

	@Override
	public Function<ArgumentTreeNode, List<Annotation>> getInstanceExtractor(JCas aJCas) {
		return (treeNode) -> {
			return getAllChilderen(treeNode.getTreeNode(), new ArrayList<>());
		};
	}
	
	public List<Annotation> getAllChilderen(Annotation ann, List<Annotation> allChilderen){
		if (ann instanceof Constituent){
			Constituent node = (Constituent) ann;
			for (int i = 0; i < node.getChildren().size(); i++){
				Annotation aChild = node.getChildren(i);
				allChilderen.add(aChild);
				getAllChilderen(aChild, allChilderen);
			}
			return allChilderen;
		}
		
		return Collections.emptyList();
	}

	@Override
	public BiFunction<List<Annotation>, ArgumentTreeNode, List<List<Feature>>> getFeatureExtractor(JCas jCas) {
		init(jCas);
		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> constituentFeatures = constituentBasedFeatures(jCas);
//		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> dcFeatures = 
//				(ann, treeNode) -> DiscourseVsNonDiscourseClassifier.getDiscourseConnectiveFeatures().apply(treeNode.getDiscourseArgument().getDiscouresRelation().getDiscourseConnective());

		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> dependencyFeatures = new DependencyFeatures(dependencyGraph, constituentToCoveredTokens);
		
		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> allFeatures = multiBiFuncMap(constituentFeatures, dependencyFeatures).andThen(flatMap(Feature.class)); 
		
		return mapOneByOneTo(allFeatures);
	}


	private BiFunction<Annotation, ArgumentTreeNode, List<Feature>> constituentBasedFeatures(JCas jCas) {
		Function<Annotation, Token> headFinder = getTokenList(constituentToCoveredTokens, List.class).andThen(getRoots(getDependencyGraph(jCas))).andThen(pickLeftMostToken());
		
		BiFunction<Annotation, ArgumentTreeNode, Feature> nodeHead = makeBiFunc(headFinder.andThen((h) -> h == null ? "null" : h.getCoveredText())
				.andThen(String::toLowerCase).andThen(makeFeature("nodeHead")));
		
		BiFunction<Annotation, ArgumentTreeNode, Feature> consType = makeBiFunc(getConstituentType().andThen(makeFeature("consType")));
		
		BiFunction<Annotation, ArgumentTreeNode, String> position = (ann, nodeInstance) -> {
			Annotation node = nodeInstance.getTreeNode();
			if (node.getBegin() == ann.getBegin())
				return "left";
			if (node.getEnd() == ann.getEnd())
				return "right";
			return "middle";
		};
		
		BiFunction<Annotation, ArgumentTreeNode, Feature> positionFeature = position.andThen(makeFeature("position"));
		
		BiFunction<Annotation, ArgumentTreeNode, Feature> parentPattern = makeBiFunc(
				getParent().andThen(getProductRule()).andThen(makeFeature("parentPattern")));

		BiFunction<Annotation, ArgumentTreeNode, Feature> grandParentPattern = makeBiFunc(
				getParent().andThen(getParent()).andThen(getProductRule()).andThen(makeFeature("grandParentPattern")));
		
		BiFunction<Annotation, ArgumentTreeNode, String> f = (ann, argTreeNode) -> argTreeNode.getDiscourseArgument().getArgumentType();
		BiFunction<Annotation, ArgumentTreeNode, Feature> argumentType = 
				getFunction(f).andThen(makeFeature("argumentType")); 
		

		BiFunction<Annotation, ArgumentTreeNode, Feature> leftSibling = makeBiFunc(getLeftSibling().andThen(getConstituentType()).andThen(makeFeature("leftSibling"))); 
		BiFunction<Annotation, ArgumentTreeNode, Feature> rightSibling = makeBiFunc(getRightSibling().andThen(getConstituentType()).andThen(makeFeature("rightSibling"))); 

		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> annotationFeatureExtractor = multiBiFuncMap(
				nodeHead, consType, positionFeature, parentPattern, grandParentPattern, argumentType,
				leftSibling, rightSibling);
		
		
		return annotationFeatureExtractor;
	}

	@Override
	public BiFunction<List<Annotation>, ArgumentTreeNode, List<String>> getLabelExtractor(JCas jCas) {
		init(jCas);
		BiFunction<Annotation, ArgumentTreeNode, String> getLabel = (ann, argInstance) -> {
			DiscourseRelation discourseRelation = argInstance.getDiscourseArgument().getDiscouresRelation();
			NodeArgType label = LabelExtractor.getNodeLabel(ann, discourseRelation, constituentToCoveredTokens, false);
			if (label == NodeArgType.None)
				return NodeArgType.None.toString();
			return argInstance.getDiscourseArgument().getArgumentType();
		};

		return mapOneByOneTo(getLabel);
	}

	@Override
	public SequenceClassifierConsumer<String, ArgumentTreeNode, Annotation> getLabeller(JCas jCas) {
		init(jCas);
		return new PurifyDiscourseRelations(constituentToCoveredTokens);
	}

	public static AnalysisEngineDescription getWriterDescription(String outputDirectory, boolean mallet) throws ResourceInitializationException {
		if (mallet)
			return StringSequenceClassifier.getWriterDescription(NoneNodeLabeller.class,
					MalletCrfStringOutcomeDataWriter.class, new File(outputDirectory));
		
		return StringSequenceClassifier.getViterbiWriterDescription(NoneNodeLabeller.class,
				WekaStringOutcomeDataWriter.class, new File(outputDirectory)); 
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String modelLocation, String goldView, String systemView) throws ResourceInitializationException {
		return StringSequenceClassifier.getClassifierDescription(goldView, systemView, NodeArgType.None.toString(), 
				NoneNodeLabeller.class, modelLocation);
	}
	

}
