package org.cleartk.corpus.conll2015.statistics;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;


public class DependenciesViewToDiscourseRelations extends JCasAnnotator_ImplBase{
//	private JCas aJCas;
//	private Set<String> relationType = new TreeSet<String>();
//	private Map<String, Integer> patternCnt = new TreeMap<String, Integer>();

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DependenciesViewToDiscourseRelations.class);
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
//		this.aJCas = aJCas;
//		if (!JCasUtil.exists(aJCas, Dependency.class))
//			return;
//		for (DiscourseRelation relation: JCasUtil.select(aJCas, DiscourseRelation.class)){
//			if (relation.getDiscourseConnective() == null)
//				continue;
//			
//			storeDependencyConnections(TokenListTools.convertToTokens(relation.getArguments(0)), 
//					TokenListTools.convertToTokens(relation.getArguments(1)));
//		}
//		setAllVerbDependency();
	}

//	private void storeDependencyConnections(List<Token> comp1, List<Token> comp2) {
//		getDirectConnection(comp1, comp2);
//		getDirectConnection(comp2, comp1);
//	}
//
//	private void getDirectConnection(List<Token> heads, List<Token> childeren) {
//		for (Token headToken: heads){
//			Dependency dependencyNode = JCasUtil.selectCovered(Dependency.class, headToken).get(0);
//			
//			for (int i = 0; i < dependencyNode.getChildRelations().size(); i++){
//				DependencyRelation relations = dependencyNode.getChildRelations(i);
//				List<Token> childTokens = JCasUtil.selectCovered(Token.class, relations.getHead());
//				if (childTokens.size() == 1){
//					if (childeren.contains(childTokens.get(0))){
//						String pattern = String.format("%s-%s-%s", relations.getRelation(), headToken.getPos(), childTokens.get(0).getPos());
//						DSManagment.incValue(patternCnt, pattern);
//					}
//				}
//			}
//		}
//	}
//
//	private void setAllVerbDependency() {
//		Collection<DependencyRelation> relations = JCasUtil.select(aJCas, DependencyRelation.class);
//		for (DependencyRelation relation: relations){
//			if (isVerb(relation.getHead()) && isVerb(relation.getChild())){
//				relationType.add(relation.getRelation());
//			}
//			
//		}
//	}
//	
//	@Override
//	public void collectionProcessComplete()
//			throws AnalysisEngineProcessException {
//		System.out.println(relationType);
//		DSPrinter.printMap("", patternCnt.entrySet(), System.out);
//		super.collectionProcessComplete();
//		
//	}
//	
//	private boolean isVerb(DependencyNode aNode){
//		List<Token> tokens = JCasUtil.selectCovered(Token.class, aNode);
//		if (tokens.size() == 1){
//			Token aToken = tokens.get(0);
//			return aToken.getPos().getstartsWith("VB");
//		}
//		
//		return false;
//	}
//	
//	public static void main(String[] args) throws UIMAException, IOException {
//		System.out.println("DependenciesViewToDiscourseRelations.main()");
//		DatasetPath dataset = new ConllDataset("train");
//		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset);
////		datasetStatistics.readDataset();
//		datasetStatistics.getStatistics(getDescription());
//	}

}
