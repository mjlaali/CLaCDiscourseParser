package org.cleartk.discourse_parsing.module.dcAnnotator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse_parsing.DiscourseParser;
import org.cleartk.discourse_parsing.module.ClassifierLabeler;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

import ir.laali.tools.str.StringUtils;

class DcInstance{
	public DcInstance(String dc, List<TreebankNode> poses,
			List<TreebankNode> parents, int offset) {
		this.text = dc;
		this.poses = poses;
		this.parents = parents;
		this.offset = offset;
	}
	
	int offset;
	String text;
	List<TreebankNode> poses;
	List<TreebankNode> parents;
}

public class DCClassifierAnnotator extends ClassifierLabeler<String, DcInstance>{
	public static final String PARAM_DC_LIST_FILE = "PARAM_DC_LIST_FILE";

	private DCLookup dcLookup = new DCLookup();
	
	@ConfigurationParameter(
			name = PARAM_DC_LIST_FILE,
			description = "A file containg a list of discourse connectives",
			mandatory = true)
	private String dcFile;

	
//TODO: Update the name of features and how they are calculated
//	private FeatureExtractor1<TokenList> connectiveFeatureExtractor = new FeatureFunctionExtractor<TokenList>(new CoveredTextExtractor<TokenList>(), BaseFeatures.EXCLUDE, 
//			new FeatureNameChangerFunc(new CapitalTypeFeatureFunction(), "CON-CapitalType"), 
//			new FeatureNameChangerFunc(new LowerCaseFeatureFunction(), "CON-LStr"));


	public static AnalysisEngineDescription getClassifierDescription(String packageDirectory, String dcFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DCClassifierAnnotator.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(packageDirectory), 
				PARAM_DC_LIST_FILE,
				dcFile);
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory, String dcFile) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DCClassifierAnnotator.class,
				DiscourseParser.getMachineLearningParameters(outputDirectory,
						PARAM_DC_LIST_FILE,
						dcFile));
	}
	
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
			dcLookup.loadDC(dcFile);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void setLabel(JCas defView, DcInstance instance,
			String classifiedLabel) {
		
		if (classifiedLabel.equals("true")){
			DiscourseConnective discourseConnective = new DiscourseConnective(aJCas);
			List<Token> connectiveTokens = JCasUtil.selectCovered(aJCas, Token.class, instance.offset, instance.offset + instance.text.length());
			TokenListTools.initTokenList(aJCas, discourseConnective, connectiveTokens);
			discourseConnective.addToIndexes();
		}
	}

	@Override
	protected String getLabel(DcInstance instance) {
		List<DiscourseConnective> coveringDcs = JCasUtil.selectCovering(aJCas, DiscourseConnective.class, instance.offset, instance.offset + instance.text.length());
		return Boolean.toString(coveringDcs.size() > 0);
	}

	@Override
	protected List<Feature> extractFeature(JCas defView, DcInstance instance)
			throws CleartkExtractorException {
		List<Feature> features = new ArrayList<Feature>();
		features.addAll(syntacticFeature(instance.parents));
//		features.addAll(localContexFeatures(instance.offset, instance.offset + instance.text.length()));
		features.addAll(lexicalFeature(instance.text));
		return features;
	}

	private List<Feature> localContexFeatures(
			int start, int end) {
		Sentence sentences = JCasUtil.selectCovering(aJCas, Sentence.class, start, end).get(0);
		List<TreebankNode> constituents = JCasUtil.selectCovered(TreebankNode.class, sentences);
		List<TreebankNode> poses = new ArrayList<TreebankNode>();

		int dcIdx = -1;
		for (TreebankNode constituent: constituents){
			if (constituent.getChildren().size() == 0){
				poses.add(constituent);
				if (constituent.getBegin() == start){
					dcIdx = poses.size() - 1;
				}
			}
		}
		
		int windowSize = 2;
		List<Feature> features = new ArrayList<Feature>();
		
		for (int i = 0; i <= windowSize; i++){
			for (int coefficient: new int[]{1, -1}){
				int idx = dcIdx + coefficient * i;
				if (idx >= 0 && idx < poses.size()){
					String word = StringUtils.decompose(poses.get(idx).getCoveredText()).toLowerCase();
					String pos = poses.get(idx).getNodeType();
					
					features.add(new Feature("word_" + coefficient * i, word));
					features.add(new Feature("pos_" + coefficient * i, pos));
					features.add(new Feature("wordPos_" + coefficient * i, word + "_" + pos));
				}
				if (i == 0)
					break;
			}
		}
		return features;
	}

	private List<Feature> lexicalFeature(String text) {
		List<Feature> lexicalFeatures = new ArrayList<Feature>();
		lexicalFeatures.add(new Feature("CON-LStr", text.toLowerCase()));
		
		if (text.toLowerCase().equals(text)){
			lexicalFeatures.add(new Feature("CON-POS", "not-at-begining"));
		} else {
			lexicalFeatures.add(new Feature("CON-POS", "at-begining"));
		}
		
		return lexicalFeatures;
	}

	private List<Feature> syntacticFeature(List<TreebankNode> parents) {
		List<Feature> features = new ArrayList<Feature>();
		TreebankNode selfCat = parents.get(parents.size() - 1);
		
		features.add(new Feature("selfCat", selfCat.getNodeType()));
		
		TreebankNode selfCatParent = selfCat.getParent();
		features.add(addNodeType("selfCatParent", selfCatParent));
		
		TreebankNode leftSibling = null, rightSibling = null;
		if (selfCatParent != null) {
			int childSize = selfCatParent.getChildren().size();
			for (int i = 0; i < childSize; i++){
				TreebankNode current = selfCatParent.getChildren(i);
				if (current.equals(selfCat)){
					if (i + 1 < childSize){
						rightSibling = selfCatParent.getChildren(i + 1);
						break;
					}
				}
				leftSibling = current;
			}
		}
		
		features.add(addNodeType("selfCatLeftSibling", leftSibling));
		features.add(addNodeType("selfCatRightSibling", rightSibling));
		
		return features;
	}

	private Feature addNodeType(String featureName, TreebankNode selfCatParent) {
		String selfCatNodeType = selfCatParent == null ? "empty" : selfCatParent.getNodeType(); 
		return new Feature(featureName, selfCatNodeType);
	}

	@Override
	public List<DcInstance> getInstances(JCas defView)
			throws AnalysisEngineProcessException {
		String documentText = aJCas.getDocumentText();
		
		List<DcInstance> dcInstances = new ArrayList<DcInstance>();
		Map<Integer, Integer> occurrences = dcLookup.getOccurrence(documentText.toLowerCase(), DCLookup.coverToTokens(defView, Token.class));
		for (Entry<Integer, Integer> occurence: occurrences.entrySet()){
			int indexOfDC = occurence.getKey();
			int endOfDc = occurence.getValue();
			String dc = documentText.substring(occurence.getKey(), occurence.getValue());
			dc = StringUtils.decompose(dc);
			
			List<TreebankNode> poses = JCasUtil.selectCovered(aJCas, TreebankNode.class, indexOfDC, endOfDc);
			List<TreebankNode> parents = JCasUtil.selectCovering(aJCas, TreebankNode.class, indexOfDC, endOfDc);
			if (poses.size() == 0 || parents.size() == 0 || dc == null || dc.equals("null")){
				System.err.println("DiscourseConnectiveAnnotator.getInstances(): can not cover or find poses of the DC <" + dc + ">");
				continue;
			}
			dcInstances.add(new DcInstance(dc, poses, parents, indexOfDC));
		}
		return dcInstances;
	}

}
