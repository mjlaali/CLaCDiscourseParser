package org.cleartk.discourse_parsing.module.argumentLabeler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.Tools;
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


public class KongEtAl2014ArgumentLabeler extends ClassifierLabeler<String, Arg2Instance>{
	public static final String DEFAULT_PATTERN_FILE = "data/analysisResults/arg2NodePattern.txt";
	private FeatureExtractor1<TokenList> connectiveFeatureExtractor;
	private FeatureExtractor2<TreebankNode, TreebankNode> pathToCnnExtractor;
	private FeatureExtractor1<TreebankNode> treebankNodeExtractor;
	private static int todoCnt;
	private boolean checkingMode = false;
	
	private Map<DiscourseConnective, Map<TreebankNode, NodeArgType>> labels; 
	
	private DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();

	public static AnalysisEngineDescription getClassifierDescription(String packageDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				KongEtAl2014ArgumentLabeler.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(packageDirectory));
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				KongEtAl2014ArgumentLabeler.class,
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


	private List<TreebankNode> createCandidates(
			DiscourseConnective discourseConnective,
			List<TreebankNode> coveringNodes, TreebankNode imediateDcParent) {
		List<TreebankNode> instances = new ArrayList<TreebankNode>();
		TreebankNode node = null;
		if (imediateDcParent.getCoveredText().equals(TokenListTools.getTokenListText(discourseConnective)))
			node = imediateDcParent.getParent();
		else
			node = imediateDcParent;
		
		while(node != null){
			for (int i = 0; i < node.getChildren().size(); i++){
				instances.add(node.getChildren(i));
			}
			node = node.getParent();
		}
		
		instances.removeAll(coveringNodes);
		
		return instances;
	}
	
	public static DiscourseRelation selectDiscourseRelation(DiscourseConnective discourseConnective) {
		List<DiscourseRelation> discourseRelations = JCasUtil.selectCovering(DiscourseRelation.class, discourseConnective);
		List<Token> connectiveTokenList = TokenListTools.convertToTokens(discourseConnective);
		DiscourseRelation selectedRelation = null;
		for (DiscourseRelation discourseRelation: discourseRelations){
			DiscourseConnective relationConnective = discourseRelation.getDiscourseConnective();
			if (relationConnective == null)	//it is an implicit relation
				continue;
			List<Token> relationConnectiveTokenList = TokenListTools.convertToTokens(relationConnective);
			if (TokenListTools.isEqualTokenList(connectiveTokenList, relationConnectiveTokenList)){
				selectedRelation = discourseRelation;
				break;
			}
		}
		
		return selectedRelation;
	}
	
	private List<Feature> createFeature(JCas aJCas, TreebankNode instance,
			DiscourseConnective discourseConnective, TreebankNode imediateDcParent) throws CleartkExtractorException {
		List<Feature> features = new ArrayList<Feature>();
		features.addAll(connectiveFeatureExtractor.extract(aJCas, discourseConnective));
		features.add(positionFeature(instance, discourseConnective));
		features.addAll(treebankNodeExtractor.extract(aJCas, instance));
		features.addAll(pathToCnnExtractor.extract(aJCas, instance, imediateDcParent));
		return features;
	}

	public static Feature positionFeature(TreebankNode instance,
			DiscourseConnective discourseConnective) {
		Position position = null;
		if (instance.getEnd() <= discourseConnective.getBegin())
			position = Position.Left;
		else if (instance.getBegin() >= discourseConnective.getEnd())
			position = Position.Right;
		else if (instance.getEnd() >= discourseConnective.getEnd() &&
				instance.getBegin() <= discourseConnective.getBegin())
			position = Position.Up;
		else 
			position = Position.Non;
		
		if (position == Position.Non)
			System.out.println("KongEtAl2014ArgumentLabeler.positionFeature(): TODO [" + (todoCnt++) + "] <" + discourseConnective.getBegin() + ", " + 
					discourseConnective.getEnd() + ">:" + discourseConnective.getCoveredText() +", <" 
					+ instance.getBegin() + ", " + instance.getEnd() + ">: " + instance.getCoveredText());
		
		return new Feature("CON-NT-Position", position.toString());
	}

	@Override
	public void setLabel(JCas defView, Arg2Instance instance,
			String classifiedLabel) {
		Map<TreebankNode, NodeArgType> map = labels.get(instance.discourseConnective);
		if (map == null){
			map = new HashMap<TreebankNode, NodeArgType>();
			labels.put(instance.discourseConnective, map);
		}
		
		map.put(instance.treebankNode, NodeArgType.valueOf(classifiedLabel));
	}

	
	@Override
	protected String getLabel(Arg2Instance instance) {
		if (!checkingMode && !isTraining())
			return null;
		
		NodeArgType res;
		SelectedDiscourseRelation selectedDiscourseRelation = selectedDiscourseRelations.get(instance.discourseConnective);
		List<Token> nodeTokens = JCasUtil.selectCovered(Token.class, instance.treebankNode);
		if (selectedDiscourseRelation.arg2Tokens.containsAll(nodeTokens)){
			res = NodeArgType.Arg2;
		} else if (selectedDiscourseRelation.arg1Tokens.containsAll(nodeTokens)) {
			res = NodeArgType.Arg1;
		} else {
			res = NodeArgType.Non;
		}
			
		return res.toString();
	}

	@Override
	protected List<Feature> extractFeature(JCas aJCas,
			Arg2Instance instance) throws CleartkExtractorException {
		return createFeature(aJCas, instance.treebankNode, instance.discourseConnective, instance.imediateDcParent);
	}

	private Map<DiscourseConnective, SelectedDiscourseRelation> selectedDiscourseRelations;
	@Override
	public List<Arg2Instance> getInstances(JCas aJCas) throws AnalysisEngineProcessException {
		List<Arg2Instance> instances = new ArrayList<Arg2Instance>();
		selectedDiscourseRelations = new HashMap<DiscourseConnective, SelectedDiscourseRelation>();
		
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			List<TreebankNode> coveringNodes = new ArrayList<TreebankNode>(JCasUtil.selectCovering(TreebankNode.class, discourseConnective));
			TreebankNode imediateDcParent = coveringNodes.size() == 0 ? null : coveringNodes.get(coveringNodes.size() - 1);
			if (imediateDcParent == null){
				System.out.println("Arg2Labeler.process(): TODO [" + (todoCnt++)
						+ "]\t<" + TokenListTools.getTokenListText(discourseConnective) +
						">\t:" + discourseConnective.getCoveredText()); 
				continue;
			}
			for (TreebankNode aNode: createCandidates(discourseConnective,
					coveringNodes, imediateDcParent)){
				instances.add(new Arg2Instance(aNode, discourseConnective, imediateDcParent));
			};

			DiscourseRelation selectedRelation = null;
			if (JCasUtil.exists(aJCas, DiscourseRelation.class)){
				selectedRelation = selectDiscourseRelation(discourseConnective);
				if (selectedRelation == null){
					System.err.println("Arg2Labeler.process()" + Tools.getDocName(aJCas) + "\t" + discourseConnective.getCoveredText() + "\t" +discourseConnective.getBegin());
					selectDiscourseRelation(discourseConnective);
					continue;
				}
				selectedDiscourseRelations.put(discourseConnective, new SelectedDiscourseRelation(selectedRelation, 
						TokenListTools.convertToTokens(selectedRelation.getArguments(0)), TokenListTools.convertToTokens(selectedRelation.getArguments(1))));
			}

		}
		return instances;
	}

	@Override
	public void init() {
		checkingMode = JCasUtil.exists(aJCas, DiscourseRelation.class);
		labels = new HashMap<DiscourseConnective, Map<TreebankNode, NodeArgType>>();
	}
	
	@Override
	protected void wrapUp() {
		if (checkingMode)
			return;
		for (Entry<DiscourseConnective, Map<TreebankNode, NodeArgType>> label: labels.entrySet()){
			Map<NodeArgType, List<Token>> tokensType = new TreeMap<NodeArgType, List<Token>>();
			for (NodeArgType nodeArgType: NodeArgType.values()){
				tokensType.put(nodeArgType, new ArrayList<Token>());
			}
			for (Entry<TreebankNode, NodeArgType> aNodeType: label.getValue().entrySet()){
				List<Token> tokens = JCasUtil.selectCovered(Token.class, aNodeType.getKey());
				List<Token> list = tokensType.get(aNodeType.getValue());
				list.addAll(tokens);
			}
			List<Token> arg1 = tokensType.get(NodeArgType.Arg1);
			if (arg1.size() == 0){
				List<Sentence> prevSents = JCasUtil.selectPreceding(Sentence.class, label.getKey(), 1);
				if (prevSents.size() == 1){
					arg1.addAll(JCasUtil.selectCovered(Token.class, prevSents.get(0)));
					int last = arg1.size() - 1;
					if (arg1.get(last).getCoveredText().equals("."))
						arg1.remove(last);
				}
			}
			
			discourseRelationFactory.makeAnExplicitRelation(aJCas, null, label.getKey(), 
					arg1, tokensType.get(NodeArgType.Arg2)).addToIndexes();
		}
	}
}
