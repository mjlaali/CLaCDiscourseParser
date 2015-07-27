package org.cleartk.discourse_parsing.module.argumentLabeler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.statistics.DiscourseArgumentsStatistics;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse.type.TokenList;
import org.cleartk.discourse_parsing.DiscourseParser;
import org.cleartk.discourse_parsing.module.ClassifierLabeler;
import org.cleartk.feature.syntax.SiblingExtractor;
import org.cleartk.feature.syntax.SyntacticPathExtractor;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.AggregateExtractor1;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.CoveredTextExtractor;
import org.cleartk.ml.feature.extractor.FeatureExtractor1;
import org.cleartk.ml.feature.extractor.FeatureExtractor2;
import org.cleartk.ml.feature.extractor.TypePathExtractor;
import org.cleartk.ml.feature.function.CapitalTypeFeatureFunction;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor;
import org.cleartk.ml.feature.function.FeatureFunctionExtractor.BaseFeatures;
import org.cleartk.ml.feature.function.FeatureNameChanger2;
import org.cleartk.ml.feature.function.FeatureNameChangerFunc;
import org.cleartk.ml.feature.function.LowerCaseFeatureFunction;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

class Arg1Instance{
	public Arg1Instance(TreebankNode aCandid, DiscourseRelation discourseRelation, TreebankNode imidiateParent, TreebankNode imediateDcParent) {
		this.treebankNode = aCandid;
		this.discourseRelation = discourseRelation;
		this.imidiateParent = imidiateParent;
		this.imediateDcParent = imediateDcParent;
	}

	TreebankNode treebankNode;
	DiscourseRelation discourseRelation;
	TreebankNode imidiateParent;
	TreebankNode imediateDcParent;
}

public class Arg1Labeler extends ClassifierLabeler<String, Arg1Instance>{
//	private static final Set<String> ARG1CoveringNode = new TreeSet<String>(Arrays.asList(new String[]{"S", "SBAR"}));
	
	private FeatureExtractor1<TokenList> connectiveFeatureExtractor;
	private FeatureExtractor2<TreebankNode, TreebankNode> pathToCnnExtractor;
	private FeatureExtractor1<TreebankNode> treebankNodeExtractor;
	
	private Map<DiscourseRelation, Map<TreebankNode, NodeArgType>> labels; 
	
	public static AnalysisEngineDescription getClassifierDescription(String packageDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				Arg1Labeler.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(packageDirectory));
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				Arg1Labeler.class,
			    DiscourseParser.getMachineLearningParameters(outputDirectory));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		connectiveFeatureExtractor = new FeatureFunctionExtractor<TokenList>(new CoveredTextExtractor<TokenList>(), BaseFeatures.EXCLUDE, 
				new FeatureNameChangerFunc(new CapitalTypeFeatureFunction(), "CON-CapitalType"), 
				new FeatureNameChangerFunc(new LowerCaseFeatureFunction(), "CON-LStr"));

		TypePathExtractor<TreebankNode> tagExtractor = new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType");
		pathToCnnExtractor = new FeatureNameChanger2<TreebankNode, TreebankNode>(
				new SyntacticPathExtractor(tagExtractor), "CON-NT-Path", "SyntacticPath_Length");
		
		treebankNodeExtractor = new AggregateExtractor1<TreebankNode>("NT-Ctx", 
				new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType"), 
				new TypePathExtractor<TreebankNode>(TreebankNode.class, "parent/nodeType"),
				new SiblingExtractor(-1, new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType")),
				new SiblingExtractor(+1, new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType")));
	}

	@Override
	public void init() {
		labels = new HashMap<DiscourseRelation, Map<TreebankNode,NodeArgType>>();
	}

	@Override
	public void setLabel(JCas defView, Arg1Instance instance,
			String classifiedLabel) {
		Map<TreebankNode, NodeArgType> map = labels.get(instance.discourseRelation);
		if (map == null){
			map = new HashMap<TreebankNode, NodeArgType>();
			labels.put(instance.discourseRelation, map);
		}
		
		map.put(instance.treebankNode, NodeArgType.valueOf(classifiedLabel));
	}

	private Map<DiscourseArgument, List<Token>> arg1sToken = new HashMap<DiscourseArgument, List<Token>>();
	
	private List<Token> getTokens(DiscourseArgument discourseArgument){
		List<Token> tokens = arg1sToken.get(discourseArgument);
		if (tokens == null){
			tokens = TokenListTools.convertToTokens(discourseArgument);
			arg1sToken.put(discourseArgument, tokens);
		}
		return tokens;
	}
	
	@Override
	protected String getLabel(Arg1Instance instance) {
		List<Token> arg1Tokens = getTokens(instance.discourseRelation.getArguments(0));
		List<Token> nodeTokens = JCasUtil.selectCovered(Token.class, instance.treebankNode);
		if (arg1Tokens.containsAll(nodeTokens))
			return NodeArgType.Arg1.toString();
		return NodeArgType.Non.toString();
	}

	@Override
	protected List<Feature> extractFeature(JCas defView, Arg1Instance instance)
			throws CleartkExtractorException {
		
		DiscourseConnective discourseConnective = instance.discourseRelation.getDiscourseConnective();
		TreebankNode treebankNode = instance.treebankNode;
		TreebankNode imidiateParent = instance.imediateDcParent;

		List<Feature> features = new ArrayList<Feature>();
		features.addAll(connectiveFeatureExtractor.extract(aJCas, discourseConnective));
		features.add(KongEtAl2014ArgumentLabeler.positionFeature(treebankNode, discourseConnective));
		features.addAll(treebankNodeExtractor.extract(aJCas, treebankNode));
		features.addAll(pathToCnnExtractor.extract(aJCas, treebankNode, imidiateParent));
		return features;
	}

	@Override
	public List<Arg1Instance> getInstances(JCas aJCas)
			throws AnalysisEngineProcessException {
		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
		
		List<Arg1Instance> instances = new ArrayList<Arg1Instance>();
		for (DiscourseRelation discourseRelation: discourseRelations){
			if (!discourseRelation.getRelationType().equals(RelationType.Explicit.toString()))
				continue;
			
			DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
			List<TreebankNode> dcCoveringNodes = new ArrayList<TreebankNode>(JCasUtil.selectCovering(TreebankNode.class, discourseConnective));
			TreebankNode imediateDcParent = dcCoveringNodes.size() == 0 ? null : dcCoveringNodes.get(dcCoveringNodes.size() - 1);
			if (imediateDcParent == null){
				System.out.println("Arg2Labeler.process(): TODO [" // + (todoCnt++)
						+ "]\t<" + TokenListTools.getTokenListText(discourseConnective) +
						">\t:" + discourseConnective.getCoveredText()); 
				continue;
			}

			
			DiscourseArgument arg2 = discourseRelation.getArguments(1);
			
			List<TreebankNode> coveringNodes = DiscourseArgumentsStatistics.getCoveringNodes(arg2, discourseRelation.getDiscourseConnective());
			if (coveringNodes.size() == 0){
				System.err.println("Arg1Labeler.getInstances(): TODO- There is no coering node for discourse connective!");
				continue;
			}
			
			TreebankNode arg2Parent = coveringNodes.get(coveringNodes.size() - 1);
			
			List<TreebankNode> treebankNodeCandids = getChildCandidates(arg2Parent, arg2, discourseRelation.getDiscourseConnective());
			treebankNodeCandids.addAll(getParentCandidates(arg2Parent));
			for (TreebankNode aCandid: treebankNodeCandids){
				instances.add(new Arg1Instance(aCandid, discourseRelation, arg2Parent, imediateDcParent));
			}
		}
		
		return instances;
	}


	private List<TreebankNode> getChildCandidates(TreebankNode arg2Parent, DiscourseArgument arg2, DiscourseConnective dc) {
		List<Token> arg2Tokens = TokenListTools.convertToTokens(arg2);
		List<Token> dcTokens = TokenListTools.convertToTokens(dc);
		List<TreebankNode> instances = new ArrayList<TreebankNode>();
		
		for (int i = 0; i < arg2Parent.getChildren().size(); i++){
			TreebankNode child = arg2Parent.getChildren(i);
			List<Token> childTokens = JCasUtil.selectCovered(Token.class, child);
			if (!arg2Tokens.containsAll(childTokens) && !dcTokens.containsAll(childTokens))
				instances.add(child);
		}

		return instances;
	}
	
	private List<TreebankNode> getParentCandidates(TreebankNode arg2Parent) {
		List<TreebankNode> instances = new ArrayList<TreebankNode>();
		List<TreebankNode> traversedNode = new ArrayList<TreebankNode>();
		
		traversedNode.add(arg2Parent);
		TreebankNode node = arg2Parent.getParent();
		while (node != null){
			traversedNode.add(node);
			for (int i = 0; i < node.getChildren().size(); i++){
				TreebankNode child = node.getChildren(i);
				instances.add(child);
			}

//			if (ARG1CoveringNode.contains(node.getNodeType()))
//				break;
			node = node.getParent();
		}
		
		instances.removeAll(traversedNode);
		return instances;
		
	}


	@Override
	protected void wrapUp() {
		for (Entry<DiscourseRelation, Map<TreebankNode, NodeArgType>> aRelationLabels: labels.entrySet()){
			List<Token> arg1Tokens = new ArrayList<Token>();
			for (Entry<TreebankNode, NodeArgType> aNodeLabel: aRelationLabels.getValue().entrySet()){
				if (aNodeLabel.getValue().equals(NodeArgType.Arg1)){
					arg1Tokens.addAll(JCasUtil.selectCovered(Token.class, aNodeLabel.getKey()));
				}
			}
			
			if (arg1Tokens.size() == 0){
				List<Sentence> prevSents = JCasUtil.selectPreceding(Sentence.class, aRelationLabels.getKey().getArguments(1), 1);
				if (prevSents.size() == 1){
					Sentence prevSent = prevSents.get(0);
					arg1Tokens = JCasUtil.selectCovered(Token.class, prevSent);
				}
			}
			
			DiscourseArgument arg1 = aRelationLabels.getKey().getArguments(0);
			TokenListTools.initTokenList(aJCas, arg1, arg1Tokens);
			List<Token> relTokens = TokenListTools.convertToTokens(aRelationLabels.getKey());
			relTokens.addAll(arg1Tokens);
			TokenListTools.initTokenList(aJCas, aRelationLabels.getKey(), relTokens);
		}
	}

}
