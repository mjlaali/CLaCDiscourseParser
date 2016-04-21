package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getDependantDependency;
import static ca.concordia.clac.ml.feature.DependencyFeatureExtractor.getHead;
import static ca.concordia.clac.ml.feature.FeatureExtractors.getFunction;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeBiFunc;
import static ca.concordia.clac.ml.feature.FeatureExtractors.makeFeature;
import static ca.concordia.clac.ml.feature.FeatureExtractors.multiBiFuncMap;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getLeftSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getParent;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getProductRule;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getRightSibling;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getTokenList;
import static ca.concordia.clac.ml.scop.ScopeFeatureExtractor.mapOneByOneTo;

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
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;

import ca.concordia.clac.ml.classifier.GenericSequenceClassifier;
import ca.concordia.clac.ml.classifier.SequenceClassifierAlgorithmFactory;
import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import ca.concordia.clac.ml.classifier.StringSequenceClassifier;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class NoneNodeLabeller implements SequenceClassifierAlgorithmFactory<String, ArgumentTreeNode, Annotation>{

	Map<Constituent, Set<Token>> constituentToCoveredTokens = new HashMap<>();
	JCas jcas = null;
	
	public Map<Constituent, Set<Token>> initConstituentToCoveredTokens(JCas jCas) {
		if (!jCas.equals(jcas)){
			constituentToCoveredTokens.clear();
			JCasUtil.indexCovered(jCas, Constituent.class, Token.class).forEach((cns, tokens) -> 
			constituentToCoveredTokens.put(cns, new HashSet<>(tokens)));
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
		initConstituentToCoveredTokens(jCas);
		Map<Token, Dependency> dependencies = getDependantDependency().apply(jCas);
		Function<Annotation, Token> headFinder = getHead(dependencies, getTokenList(constituentToCoveredTokens, Set.class));
		
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
		BiFunction<Annotation, ArgumentTreeNode, Feature> rightSibling = makeBiFunc(getRightSibling().andThen(getConstituentType()).andThen(makeFeature("leftSibling"))); 

		BiFunction<Annotation, ArgumentTreeNode, List<Feature>> annotationFeatureExtractor = multiBiFuncMap(
				nodeHead, consType, positionFeature, parentPattern, grandParentPattern, argumentType,
				leftSibling, rightSibling);
		
		return mapOneByOneTo(annotationFeatureExtractor);
	}

	@Override
	public BiFunction<List<Annotation>, ArgumentTreeNode, List<String>> getLabelExtractor(JCas jCas) {
		initConstituentToCoveredTokens(jCas);
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
		initConstituentToCoveredTokens(jCas);
		return new PurifyDiscourseRelations(constituentToCoveredTokens);
	}

	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
//		return StringSequenceClassifier.getWriterDescription(NoneNodeLabeller.class,
//				MalletCrfStringOutcomeDataWriter.class, new File(outputDirectory)); 
		return StringSequenceClassifier.getViterbiWriterDescription(NoneNodeLabeller.class,
				WekaStringOutcomeDataWriter.class, new File(outputDirectory)); 
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String modelFileName) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
		        StringSequenceClassifier.class,
		        GenericSequenceClassifier.PARAM_ALGORITHM_FACTORY_CLASS_NAME,
		        NoneNodeLabeller.class.getName(),
		        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
		        modelFileName);
	}
	

}
