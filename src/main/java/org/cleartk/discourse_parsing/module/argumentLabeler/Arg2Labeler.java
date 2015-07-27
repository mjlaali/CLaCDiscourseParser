package org.cleartk.discourse_parsing.module.argumentLabeler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.RelationType;
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
import org.cleartk.token.type.Token;


class Arg2Instance{
	public Arg2Instance(TreebankNode aNode,	DiscourseConnective discourseConnective, TreebankNode imediateDcParent) {
		this.treebankNode = aNode;
		this.discourseConnective = discourseConnective;
		this.imediateDcParent = imediateDcParent;
	}
	TreebankNode treebankNode;
	TreebankNode imediateDcParent;
	DiscourseConnective discourseConnective;
}

class SelectedDiscourseRelation{
	public SelectedDiscourseRelation(DiscourseRelation selectedRelation, List<Token> arg1Token, List<Token> arg2Token) {
		this.discourseRelation = selectedRelation;
		this.arg1Tokens = new HashSet<Token>(arg1Token);
		this.arg2Tokens = new HashSet<Token>(arg2Token);

	}

	Set<Token> arg1Tokens = null, arg2Tokens = null;
	DiscourseRelation discourseRelation;
}

public class Arg2Labeler extends ClassifierLabeler<String, Arg2Instance>{
	public static final String DEFAULT_PATTERN_FILE = "data/analysisResults/arg2NodePattern.txt";
	private static final String PARAM_ARG2_PATTERN_FILE = "arg2PatternFile";
	private FeatureExtractor1<TokenList> connectiveFeatureExtractor;
	private FeatureExtractor2<TreebankNode, TreebankNode> pathToCnnExtractor;
	private FeatureExtractor1<TreebankNode> treebankNodeExtractor;
	private int todoCnt;
	private boolean checkingMode = false;
	
	private Map<String, List<Boolean>> arg2Patterns = new TreeMap<String, List<Boolean>>();
	private Map<DiscourseConnective, TreebankNode> dcToArg2Node = new HashMap<DiscourseConnective, TreebankNode>();
	
	private DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();

	@ConfigurationParameter(
			name = PARAM_ARG2_PATTERN_FILE,
			description = "Pattern of arg2 nodes",
			mandatory = false)
	private String arg2PatternFile;
	

	public static AnalysisEngineDescription getClassifierDescription(String packageDirectory, String arg2PatternFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				Arg2Labeler.class,
				PARAM_ARG2_PATTERN_FILE, 
				arg2PatternFile,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(packageDirectory));
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				Arg2Labeler.class,
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
		
		try {
			if (!isTraining())
				readPatterns();
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e);
		}
		
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		checkingMode = JCasUtil.exists(aJCas, DiscourseRelation.class);
		super.process(aJCas);
	}

	private void readPatterns() throws FileNotFoundException {
		Scanner scanner = new Scanner(new File(arg2PatternFile));
		while (scanner.hasNext()){
			String line = scanner.nextLine();
			String pattern = line.replaceAll("/[a-z]+", "");
			String[] strTags = line.replaceAll("[^/-]+/", "").split("-");
			List<Boolean> tags = new ArrayList<Boolean>();
			for (int i = 0; i < strTags.length; i++){
				tags.add(Boolean.parseBoolean(strTags[i]));
			}
			if (!arg2Patterns.containsKey(pattern)){
				arg2Patterns.put(pattern, tags);
			}
		}
		
		scanner.close();
		
	}


	private List<TreebankNode> createCandidates(
			DiscourseConnective discourseConnective,
			List<TreebankNode> coveringNodes, TreebankNode imediateDcParent) {
		List<TreebankNode> instances = new ArrayList<TreebankNode>();
		final Set<String> targetTag = new TreeSet<String>(Arrays.asList(new String[]{"S", "SBAR"}));
		for (TreebankNode node: coveringNodes){
			String nodeType = node.getNodeType();
			if (targetTag.contains(nodeType))
				instances.add(node);
		}
		Collections.reverse(instances);
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
//		features.add(positionFeature(instance, discourseConnective));
		features.add(chiderenPattern(instance));
		features.addAll(treebankNodeExtractor.extract(aJCas, instance));
		features.addAll(pathToCnnExtractor.extract(aJCas, instance, imediateDcParent));
		return features;
	}

	private Feature chiderenPattern(TreebankNode instance) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < instance.getChildren().size(); i++){
			if (sb.length() != 0)
				sb.append("-");
			sb.append(instance.getChildren(i).getNodeType());
		}

		return new Feature("ChildPat", sb.toString());
	}

	@Override
	public void setLabel(JCas defView, Arg2Instance instance,
			String classifiedLabel) {
		if (classifiedLabel.equals(NodeArgType.Arg2.toString()) && !dcToArg2Node.containsKey(instance.discourseConnective)){
			TreebankNode node = instance.treebankNode;
			StringBuilder sb = new StringBuilder();
			int childSize = node.getChildren().size();
			for (int i = 0 ; i < childSize; i++){
				if (sb.length() != 0)
					sb.append("-");
				sb.append(node.getChildren(i).getNodeType());
			}
			List<Boolean> tags = arg2Patterns.get(sb.toString());
			if (tags != null){
				dcToArg2Node.put(instance.discourseConnective, node);
				List<Token> arg2Tokens = new ArrayList<Token>();
				for (int i = 0 ; i < childSize; i++){
					if (tags.get(i)){
						arg2Tokens.addAll(JCasUtil.selectCovered(Token.class, node.getChildren(i)));
					}
				}
				
				List<Token> dcTokens = TokenListTools.convertToTokens(instance.discourseConnective);
				arg2Tokens.removeAll(dcTokens);//for sure these token should not be in the list.
				List<Token> arg1Tokens = Collections.emptyList();
				discourseRelationFactory.makeDiscourseRelation(aJCas, RelationType.Explicit, null, 
						TokenListTools.getTokenListText(instance.discourseConnective), dcTokens, arg1Tokens, arg2Tokens).addToIndexes();
			}
		}
	}

	@Override
	protected String getLabel(Arg2Instance instance) {
		if (!checkingMode && !isTraining())
			return null;
		NodeArgType res;
		if (seenConnective.containsKey(instance.discourseConnective))
			if (seenConnective.get(instance.discourseConnective).equals(instance.treebankNode))
				res = NodeArgType.Arg2;
			else
				res = NodeArgType.Non;
		else {
			SelectedDiscourseRelation selectedDiscourseRelation = selectedDiscourseRelations.get(instance.discourseConnective);
			List<Token> nodeTokens = JCasUtil.selectCovered(Token.class, instance.treebankNode);
			if (nodeTokens.containsAll(selectedDiscourseRelation.arg2Tokens)){
				res = NodeArgType.Arg2;
				seenConnective.put(instance.discourseConnective, instance.treebankNode);
			} else
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

	private Map<DiscourseConnective, TreebankNode> seenConnective;
	@Override
	public void init() {
		seenConnective = new HashMap<DiscourseConnective, TreebankNode>();
	}
	
}
