package org.cleartk.discourse_parsing.module.argumentLabeler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DatasetStatistics;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse.type.TokenList;
import org.cleartk.discourse_parsing.DiscourseParser;
import org.cleartk.discourse_parsing.errorAnalysis.DiscourseRelationsExporter;
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
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Token;

class DiscourseRelationInfo{
	public DiscourseRelationInfo(DiscourseRelation selectedRelation, List<Token> relationTokens, List<Token> arg1Tokens, List<Token> arg2Tokens) {
		this.discourseRelation = selectedRelation;
		this.relationTokens = new HashSet<Token>(relationTokens);
		this.arg1Tokens = new HashSet<Token>(arg1Tokens);
		this.arg2Tokens = new HashSet<Token>(arg2Tokens);
	}

	Set<Token> relationTokens;
	Set<Token> arg1Tokens;
	Set<Token> arg2Tokens;
	DiscourseRelation discourseRelation;
}
class ScopeInstance{
	public ScopeInstance(TreebankNode aNode,
			DiscourseConnective discourseConnective,
			TreebankNode imediateDcParent) {
		this.imediateDcParent = imediateDcParent;
		this.discourseConnective = discourseConnective;
		this.treebankNode = aNode;
	}
	
	DiscourseConnective discourseConnective;
	TreebankNode treebankNode;
	TreebankNode imediateDcParent;
}

public class DCScopDetector extends ClassifierLabeler<String, ScopeInstance>{
	public static final String DEFAULT_PATTERN_FILE = "data/analysisResults/arg2NodePattern.txt";
	private static final String PARAM_ARG2_PATTERN_FILE = "arg2PatternFile";
	private FeatureExtractor1<TokenList> connectiveFeatureExtractor;
	private FeatureExtractor2<TreebankNode, TreebankNode> pathToCnnExtractor;
	private FeatureExtractor2<TreebankNode, TreebankNode> pathToRootExtractor;
	private FeatureExtractor1<TreebankNode> treebankNodeExtractor;
	private int todoCnt;
	private boolean checkingMode = false;
	
	private PrintStream out;
//	private Map<DiscourseConnective, TreebankNode> dcToArg2Node = new HashMap<DiscourseConnective, TreebankNode>();
	
//	private DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();

	@ConfigurationParameter(
			name = PARAM_ARG2_PATTERN_FILE,
			description = "Pattern of arg2 nodes",
			mandatory = false)
	private String arg2PatternFile;
	

	public static AnalysisEngineDescription getClassifierDescription(String packageDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DCScopDetector.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(packageDirectory));
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DCScopDetector.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
			    true,
		        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
		        WekaStringOutcomeDataWriter.class.getName(),
		        DefaultDataWriterFactory.PARAM_DATA_WRITER_CONSTRUCTOR_INPUTS,
		        "arguments 10",
		        DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		        outputDirectory);
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

		pathToRootExtractor = new FeatureNameChanger2<TreebankNode, TreebankNode>(
				new SyntacticPathExtractor(tagExtractor), "COS-ROOT-Path", "SyntacticPath_Length");
		
		treebankNodeExtractor = new AggregateExtractor1<TreebankNode>("NT-Ctx", 
				new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType"), 
				new TypePathExtractor<TreebankNode>(TreebankNode.class, "parent/nodeType"),
				new SiblingExtractor(-1, new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType")),
				new SiblingExtractor(+1, new TypePathExtractor<TreebankNode>(TreebankNode.class, "nodeType")));
		
		try {
			File scopFile = new File("outputs/parser/debug/scop.txt");
			scopFile.getParentFile().mkdir();
			out = new PrintStream(scopFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		checkingMode = JCasUtil.exists(aJCas, DiscourseRelation.class);
		super.process(aJCas);
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
		TreebankNode root = instance;
		
		while (root.getParent() != null){
			root = root.getParent();
		}
		
		List<Feature> features = new ArrayList<Feature>();
		features.addAll(connectiveFeatureExtractor.extract(aJCas, discourseConnective));
		features.add(chiderenPattern(instance));
		features.add(getVpCountFeature(instance));
		features.addAll(treebankNodeExtractor.extract(aJCas, instance));
		features.addAll(pathToCnnExtractor.extract(aJCas, instance, imediateDcParent));
		features.addAll(pathToRootExtractor.extract(aJCas, instance, root));
		return features;
	}

	private Feature getVpCountFeature(TreebankNode instance) {
		return new Feature("Vp-Cnt", getVpCount(instance));
	}
	

	private int getVpCount(TreebankNode instance) {
		int vpCnt = 0;
		if (instance.getNodeType().equals("VP"))
			vpCnt = 1;
		else
			for (int i = 0; i < instance.getChildren().size(); i++){
				vpCnt += getVpCount(instance.getChildren(i));
			}
		
		return vpCnt;
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
	public void setLabel(JCas defView, ScopeInstance instance,
			String classifiedLabel) {
		String goldLabel = getLabel(instance);
		if (classifiedLabel.equals(goldLabel)){
			
		} else {
			DiscourseRelationInfo selectedDiscourseRelation = selectedDiscourseRelations.get(instance.discourseConnective);
			try {
				out.println(String.format("Gold Label = %s, Classifier Label = %s, Node(%s): <%s>", goldLabel, classifiedLabel, 
						instance.treebankNode.getNodeType(), instance.treebankNode.getCoveredText()));
				DiscourseRelationsExporter.printDiscourseRelation(out, aJCas, selectedDiscourseRelation.discourseRelation);
			} catch (AnalysisEngineProcessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String getLabel(ScopeInstance instance) {
		if (!checkingMode && !isTraining())
			return null;
		NodeArgType res;
		DiscourseRelationInfo selectedDiscourseRelation = selectedDiscourseRelations.get(instance.discourseConnective);
		List<Token> nodeTokens = JCasUtil.selectCovered(Token.class, instance.treebankNode);
		Map<NodeArgType, TreebankNode> setLabelsForDc = seenConnective.get(instance.discourseConnective);
		TreebankNode relationNode = null;
		TreebankNode arg2Node = null;
		if (setLabelsForDc != null){
			relationNode = setLabelsForDc.get(NodeArgType.Relation);
			arg2Node = setLabelsForDc.get(NodeArgType.Arg2);
		} else {
			setLabelsForDc = new TreeMap<NodeArgType, TreebankNode>();
			seenConnective.put(instance.discourseConnective, setLabelsForDc);
		}
		
		if (instance.treebankNode.equals(relationNode) || (
				relationNode == null && nodeTokens.containsAll(selectedDiscourseRelation.relationTokens))){
			res = NodeArgType.Relation;
			setLabelsForDc.put(res, instance.treebankNode);
//		} else if (arg2Node == null && nodeTokens.containsAll(selectedDiscourseRelation.arg2Tokens)){
//			res = NodeArgType.Arg2;
//			setLabelsForDc.put(res, instance.treebankNode);
		} else
			res = NodeArgType.Non;
		return res.toString();
	}

	@Override
	protected List<Feature> extractFeature(JCas aJCas,
			ScopeInstance instance) throws CleartkExtractorException {
		
		return createFeature(aJCas, instance.treebankNode, instance.discourseConnective, instance.imediateDcParent);
	}

	private Map<DiscourseConnective, DiscourseRelationInfo> selectedDiscourseRelations;
	@Override
	public List<ScopeInstance> getInstances(JCas aJCas) throws AnalysisEngineProcessException {
		List<ScopeInstance> instances = new ArrayList<ScopeInstance>();
		selectedDiscourseRelations = new HashMap<DiscourseConnective, DiscourseRelationInfo>();
		
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
				instances.add(new ScopeInstance(aNode, discourseConnective, imediateDcParent));
			};

			DiscourseRelation selectedRelation = null;
			if (JCasUtil.exists(aJCas, DiscourseRelation.class)){
				selectedRelation = selectDiscourseRelation(discourseConnective);
				if (selectedRelation == null){
					System.err.println("Arg2Labeler.process()" + Tools.getDocName(aJCas) + "\t" + discourseConnective.getCoveredText() + "\t" +discourseConnective.getBegin());
					selectDiscourseRelation(discourseConnective);
					continue;
				}
				selectedDiscourseRelations.put(discourseConnective, new DiscourseRelationInfo(selectedRelation, 
						TokenListTools.convertToTokens(selectedRelation), 
						TokenListTools.convertToTokens(selectedRelation.getArguments(0)), 
						TokenListTools.convertToTokens(selectedRelation.getArguments(1))));
			}

		}
		return instances;
	}
	
	@Override
	public void collectionProcessComplete()
			throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		out.close();
	}

	private Map<DiscourseConnective, Map<NodeArgType, TreebankNode>> seenConnective;
	@Override
	public void init() {
		seenConnective = new HashMap<DiscourseConnective, Map<NodeArgType, TreebankNode>>();
	}
	
	public static void main(String[] args) throws UIMAException, IOException {
		System.out.println("DCScopDetector.main()");
		DatasetPath dataset = new ConllDataset("dev");
		String modelDir = new ConllDataset("train").getModelDir();
		AnalysisEngineDescription scopDetector = DCScopDetector.getClassifierDescription(DiscourseParser.getScopeDetectorTrainDir(modelDir));
		
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset);
		datasetStatistics.getStatistics(scopDetector);

	}
	
}
