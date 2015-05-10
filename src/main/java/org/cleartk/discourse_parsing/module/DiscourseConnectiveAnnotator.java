package org.cleartk.discourse_parsing.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.corpus.conll2015.statistics.DiscourseConnectivesList;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.ml.weka.WekaStringOutcomeDataWriter;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Token;

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

public class DiscourseConnectiveAnnotator extends ClassifierLabeler<String, DcInstance>{
	private List<String> dcList;
	private TreeMap<Integer, Integer> dcIntervals;
	
//TODO: Update the name of features and how they are calculated
//	private FeatureExtractor1<TokenList> connectiveFeatureExtractor = new FeatureFunctionExtractor<TokenList>(new CoveredTextExtractor<TokenList>(), BaseFeatures.EXCLUDE, 
//			new FeatureNameChangerFunc(new CapitalTypeFeatureFunction(), "CON-CapitalType"), 
//			new FeatureNameChangerFunc(new LowerCaseFeatureFunction(), "CON-LStr"));


	public static AnalysisEngineDescription getClassifierDescription(String packageDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DiscourseConnectiveAnnotator.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
				false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
				JarClassifierBuilder.getModelJarFile(packageDirectory));
	}
	
	public static AnalysisEngineDescription getWriterDescription(String outputDirectory) throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(
				DiscourseConnectiveAnnotator.class,
				CleartkAnnotator.PARAM_IS_TRAINING,
			    true,
		        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
		        WekaStringOutcomeDataWriter.class.getName(),
		        DefaultDataWriterFactory.PARAM_DATA_WRITER_CONSTRUCTOR_INPUTS,
		        "arguments 10",
		        DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		        outputDirectory);
	}
	
	
	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);
		
		try {
			dcList = FileUtils.readLines(new File(DiscourseConnectivesList.DISCOURSE_CONNECTIVES_LIST_FILE));
			Collections.sort(dcList, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {
					return o2.length() - o1.length();
				}
			});
			String smallestDc = dcList.get(dcList.size() - 1);
			int smallestDcLength = smallestDc.length();
			if (smallestDcLength == 0 || smallestDcLength > 3){
				throw new RuntimeException("Are you sure the content of the discourse connective list is correct: " + smallestDc);
			}
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void init() {
		dcIntervals = new TreeMap<Integer, Integer>();
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
		features.addAll(lexicalFeature(instance.text));
		return features;
	}

	private List<Feature> lexicalFeature(String text) {
		List<Feature> lexicalFeatures = new ArrayList<Feature>();
		lexicalFeatures.add(new Feature("CON-LStr", text.toLowerCase()));
		lexicalFeatures.add(new Feature("connTxt", text.toLowerCase()));
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
		if (Tools.getDocName(aJCas).equals("wsj_2200"))
			System.out.println("DiscourseConnectiveAnnotator.getInstances()");
		String documentText = aJCas.getDocumentText().toLowerCase();
		
		List<DcInstance> dcInstances = new ArrayList<DcInstance>();
		for (String dc: dcList){
			int indexOfDC = documentText.indexOf(dc);
			int endOfDc = indexOfDC + dc.length();
			while (indexOfDC != -1){
				if (!containedInPrevCandid(indexOfDC, endOfDc) && checkBoundray(indexOfDC, endOfDc)){
					List<TreebankNode> poses = JCasUtil.selectCovered(aJCas, TreebankNode.class, indexOfDC, endOfDc);
					List<TreebankNode> parents = JCasUtil.selectCovering(aJCas, TreebankNode.class, indexOfDC, endOfDc);
					if (poses.size() == 0 || parents.size() == 0){
						System.err.println("DiscourseConnectiveAnnotator.getInstances(): can not cover or find poses of the DC <" + dc + ">");
						continue;
					}
					dcInstances.add(new DcInstance(dc, poses, parents, indexOfDC));
				}
				
				indexOfDC = documentText.indexOf(dc, endOfDc);
				endOfDc = indexOfDC + dc.length();
			}
		}
		return dcInstances;
	}

	
	private boolean checkBoundray(int indexOfDC, int endOfDc) throws AnalysisEngineProcessException {
		List<Token> selectCovered = JCasUtil.selectCovered(aJCas, Token.class, indexOfDC, endOfDc);
		if (selectCovered.size() > 0){
			Token token = selectCovered.get(0);
			boolean validBegin = token.getBegin() == indexOfDC;
			boolean validEnd = selectCovered.get(selectCovered.size() - 1).getEnd() == endOfDc;
			
			return validBegin && validEnd;
		}
		
		return false;
	}

	private boolean containedInPrevCandid(int indexOfDC, int endOfDc) {
		Integer floorKey = dcIntervals.floorKey(indexOfDC);
		if (floorKey == null || dcIntervals.get(floorKey) < indexOfDC){
			dcIntervals.put(indexOfDC, endOfDc);
			return false;
		}
		return true;
	}
	


}
