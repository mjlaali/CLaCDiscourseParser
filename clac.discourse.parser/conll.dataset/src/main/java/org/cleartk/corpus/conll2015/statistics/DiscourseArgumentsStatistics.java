package org.cleartk.corpus.conll2015.statistics;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

public class DiscourseArgumentsStatistics extends JCasAnnotator_ImplBase{
	public static final String PARAM_ARG2_DIST_FILE = "arg2DistFile";

//	private Map<String, Integer> relationNodeDistribution = new TreeMap<String, Integer>();
//	private Map<String, Integer> arg2NodeDistribution = new TreeMap<String, Integer>();
//	private Map<String, Integer> coverinNodeOfDC = new TreeMap<String, Integer>();
//	private Map<String, Integer> arg2ParentChildPattern = new TreeMap<String, Integer>();
//	private Map<String, Integer> relationPattern = new TreeMap<String, Integer>();
//	private Map<Integer, Integer> relationPatternDepth = new TreeMap<Integer, Integer>();
//
//	private Set<String> targetTag = new TreeSet<String>(Arrays.asList(new String[]{"S", "SBAR"}));
	int valid, invalid;
	int exRelCnt = 0;
	//	private DiscourseRelation discourseRelation;

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DiscourseArgumentsStatistics.class);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
//		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
//		for (DiscourseRelation discourseRelation: discourseRelations){
//
//			if (discourseRelation.getRelationType().equals(RelationType.Explicit.toString())){
//				exRelCnt++;
//				DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
//				DiscourseArgument arg2 = discourseRelation.getArguments(1);
//				DiscourseArgument arg1 = discourseRelation.getArguments(0);
//				createDistributionOfFirstParent(relationNodeDistribution, getCoveringNodes(discourseConnective, arg1, arg2));
//				createDistributionOfFirstParent(arg2NodeDistribution, pruneNodes(getCoveringNodes(arg2, discourseConnective)));
//				analysisRelationTreebankNodes(discourseRelation);
//				storeRelationPattern(discourseRelation);
//				//				representTreeNodes(relationPattern, getParents(discourseConnective, arg1, arg2), discourseConnective, arg1, arg2);
//				if (isParentWellFormed(arg2, discourseConnective))
//					valid++;
//				else
//					invalid++;
//				getStatOfCoveringNodeOfDC(discourseConnective);
//			}
//		}
//		
	}
	
//	private Map<String, Map<String, Integer>> distNodeType = new TreeMap<String, Map<String,Integer>>();
//	private Map<Integer, Integer> relationSize = new TreeMap<Integer, Integer>();
//	
//	private void analysisRelationTreebankNodes(DiscourseRelation discourseRelation) {
//		Set<Sentence> coveredSents = new HashSet<Sentence>();
//		coveredSents.addAll(JCasUtil.selectCovered(Sentence.class, discourseRelation));
//		
//		for (Token token: TokenListTools.convertToTokens(discourseRelation)){
//			coveredSents.addAll(JCasUtil.selectCovering(Sentence.class, token));
//		}
//		
//		DSManagment.incValue(relationSize, coveredSents.size());
//		if (coveredSents.size() > 2){
//			return;
//		}
//		List<TreebankNode> coveredNode = JCasUtil.selectCovered(TreebankNode.class, discourseRelation);
//		
//		Map<TreebankNode, String> nodeToType = new HashMap<TreebankNode, String>();
//		
//		Map<String, List<Token>> tokensGroup = new HashMap<String, List<Token>>(); 
//		tokensGroup.put("dc", TokenListTools.convertToTokens(discourseRelation.getDiscourseConnective()));
//		tokensGroup.put("arg1", TokenListTools.convertToTokens(discourseRelation.getArguments(0)));
//		tokensGroup.put("arg2", TokenListTools.convertToTokens(discourseRelation.getArguments(1)));
//		
//		for (TreebankNode aNode: coveredNode){
//			List<Token> aNodeTokens = new ArrayList<Token>(JCasUtil.selectCovered(Token.class, aNode));
//			String type = null;
//			
//			for (Entry<String, List<Token>> aTokenGroup: tokensGroup.entrySet()){
//				if (aTokenGroup.getValue().containsAll(aNodeTokens)){
//					type = aTokenGroup.getKey();
//					if (aTokenGroup.getValue().size() == aNodeTokens.size()){
//						type = type + "/eq";
//					} else
//						type = type + "/sub";
//					aNodeTokens = Collections.emptyList();
//					break;
//				}
//			}
//			if (type == null){
//				for (Entry<String, List<Token>> aTokenGroup: tokensGroup.entrySet()){
//					//int beforeDelSize = aNodeTokens.size();
//					if (aNodeTokens.removeAll(aTokenGroup.getValue())){
//						if (type != null){
//							type = type + "-";
//						} else
//							type = "";
//						type = type + aTokenGroup.getKey();
//						//if (beforeDelSize - aNodeTokens.size() == aTokenGroup.getValue().size()){
//							type = type + "/sup";
//						//} else
//						//	type = type + "/part";
//					}
//				}
//			}
//			
//			if (type == null){
//				if (aNode.getNodeType().equals("TOP"))
//					System.out.println();
//				type = "none";
//			} else if (aNodeTokens.size() != 0){
//				type = type + "-none";
//			}
//
//			nodeToType.put(aNode, type);
//		}
//		
//		Map<TreebankNode, String> bigNodeType = new HashMap<TreebankNode, String>();
//		for (TreebankNode aNode: coveredNode){
//			if (nodeToType.get(aNode) != nodeToType.get(aNode.getParent())){
//				bigNodeType.put(aNode, nodeToType.get(aNode));
//			}
//		}
//		
//		for (Entry<TreebankNode, String> aBigNodeType: bigNodeType.entrySet()){
//			DSManagment.incValue(distNodeType, aBigNodeType.getValue(), aBigNodeType.getKey().getNodeType());
//		}
//	}
//
//	int depth = 0, maxDepth = 0;
//	private void storeRelationPattern(DiscourseRelation discourseRelation){
//		DiscourseConnective discourseConnective = discourseRelation.getDiscourseConnective();
//		DiscourseArgument arg1 = discourseRelation.getArguments(0);
//		DiscourseArgument arg2 = discourseRelation.getArguments(1);
//		
//		List<TreebankNode> relationSuperNode = getCoveringNodes(discourseConnective, arg1, arg2);
//		List<Set<Token>> tokenListTokens = new ArrayList<Set<Token>>();
//		tokenListTokens.add(new HashSet<Token>(TokenListTools.convertToTokens(arg1)));
//		Set<Token> arg2Tokens = new HashSet<Token>(TokenListTools.convertToTokens(arg2));
//		arg2Tokens.addAll(TokenListTools.convertToTokens(discourseConnective));
//		tokenListTokens.add(arg2Tokens);
//
//		String representaion;
//		if (relationSuperNode.size() == 0){
//			Sentence sentDc = JCasUtil.selectCovering(Sentence.class, discourseConnective).iterator().next();
//			List<Sentence> arg1Sents = getSents(arg1);
//			List<Sentence> arg2Sents = getSents(arg2);
//			if (arg1Sents.size() == 0 || arg2Sents.size() == 0)
//				representaion = "none-" + (arg1Sents.size() == 0);
//			else {
//				Sentence sentArg1 = arg1Sents.iterator().next();
//				Sentence sentArg2 = arg2Sents.iterator().next();
//				if (arg1Sents.size() == 1 && arg2Sents.size() == 1){
//					List<TreebankNode> sentsCovering = JCasUtil.selectCovering(TreebankNode.class, sentArg2);
//					TreebankNode selected = null;
//					for (TreebankNode sentCovering: sentsCovering){
//						if (!sentCovering.getNodeType().equals("TOP")){
//							selected = sentCovering;
//							break;
//						}
//					}
//					if (selected == null || !selected.getCoveredText().equals(sentArg2.getCoveredText()))
//						representaion = "error";
//					else
//						representaion = relativePosition(sentArg1, sentArg2) + 
//						representTreeNodes(selected, new String[]{"Arg2"}, Collections.singletonList(arg2Tokens));
//				} else
//					representaion = (arg1Sents.size() > 1) + "-" + (arg2Sents.size() > 1) + "-" +sentArg1.equals(sentArg2) + "-" + sentDc.equals(sentArg2) ;
//			}
//		} else {
//			
//			TreebankNode aNode = null;
//			for (int i = relationSuperNode.size() - 1; i >=0; i--){
//				aNode = relationSuperNode.get(i);
//				if (!aNode.getNodeType().equals("TOP"))
//					break;
//			}
//			depth = 0; maxDepth = 0;
//			representaion = representTreeNodes(aNode, 
//					new String[]{"Arg1", "Arg2"}, 
//					tokenListTokens);
//			DSManagment.incValue(relationPatternDepth, maxDepth);
//		}
//		
//		DSManagment.incValue(relationPattern, representaion);
//	}
//
//	private String relativePosition(Sentence sentArg1, Sentence sentArg2) {
//		String rep;
//		if (sentArg1.getBegin() < sentArg2.getBegin()){
//			rep = "next-";
//			List<Sentence> selectFollowing = JCasUtil.selectFollowing(Sentence.class, sentArg1, 1);
//			rep += selectFollowing.get(0).equals(sentArg2);
//		} else {
//			rep = "prev-";
//			List<Sentence> selectFollowing = JCasUtil.selectFollowing(Sentence.class, sentArg2, 1);
//			rep += selectFollowing.get(0).equals(sentArg1);
//		}
//			
//		return rep;
//	}
//
//	private List<Sentence> getSents(DiscourseArgument arg1) {
//		List<Sentence> selectSent = JCasUtil.selectCovering(Sentence.class, arg1);
//		if (selectSent.size() == 0){
//			selectSent = JCasUtil.selectCovered(Sentence.class, arg1);
//		}
//		return selectSent;
//	}
//
//	private String representTreeNodes(TreebankNode aNode, String[] labels,List<Set<Token>> tokenListTokens) {
//		depth++;
//		if (depth > maxDepth)
//			maxDepth = depth;
//		StringBuilder sb = new StringBuilder();
//
//		int size = aNode.getChildren().size();
//		for (int i = 0; i < size; i++){
//			TreebankNode child = aNode.getChildren(i);
//			if (sb.length() != 0)
//				sb.append(" ");
//			Set<Token> childToken = new HashSet<Token>(JCasUtil.selectCovered(Token.class, child));
//
//			String nodeRepresentation = String.format("[%s %s]", child.getNodeType(), "none");
//			int tokenListIdx = 0;
//			for (Set<Token> tokenListToken: tokenListTokens){
//				boolean containsAll = tokenListToken.containsAll(childToken);
//				if (containsAll){
//					nodeRepresentation = String.format("[%s %s]", child.getNodeType(), labels[tokenListIdx]);
//					break;
//				} else if (childToken.removeAll(tokenListToken)){
//					nodeRepresentation = representTreeNodes(child, labels, tokenListTokens);
//					break;
//				} else {
//					
//				}
//				tokenListIdx++;
//			}
//
//			sb.append(nodeRepresentation);
//		}
//		
//		String format = String.format("[%s %s]", aNode.getNodeType(), sb.toString());
//		depth--;
//		return format;
//	}
//	//http://ironcreek.net/phpsyntaxtree/?PHPSESSID=d3qk5vltbi4pf9lt13lli1em85
//
//	private boolean isParentWellFormed(DiscourseArgument arg2, DiscourseConnective discourseConnective) {
//		List<TreebankNode> allValidParent = pruneNodes(getCoveringNodes(arg2, discourseConnective));
//
//		boolean valid = true;
//		StringBuilder sb = new StringBuilder();
//		if (allValidParent.size() > 0){
//			TreebankNode smallestValidParent = allValidParent.get(allValidParent.size() - 1);
//			Set<Token> argTokens = new HashSet<Token>(TokenListTools.convertToTokens(arg2));
//			//			Set<Token> dcTokens = new HashSet<Token>(TokenListContext.convertToTokens(discourseConnective));
//
//			int size = smallestValidParent.getChildren().size();
//			for (int i = 0; i < size && valid; i++){
//				TreebankNode child = smallestValidParent.getChildren(i);
//				if (sb.length() != 0)
//					sb.append("-");
//				Set<Token> childToken = new HashSet<Token>(JCasUtil.selectCovered(Token.class, child));
//				sb.append(child.getNodeType() + "/" + argTokens.containsAll(childToken));
//				if (!argTokens.containsAll(childToken) && childToken.removeAll(argTokens) == true){
//					valid = false;
//				}
//			}
//		} else {
//			valid = false;
//		}
//
//		if (valid)
//			DSManagment.incValue(arg2ParentChildPattern, sb.toString());
//
//		return valid;
//	}
//
//	private void getStatOfCoveringNodeOfDC(DiscourseConnective discourseConnective) {
//		List<TreebankNode> coveringNodes = JCasUtil.selectCovering(TreebankNode.class, discourseConnective);
//
//		for (TreebankNode treebankNode: coveringNodes){
//			if (targetTag.contains(treebankNode.getNodeType())){
//				DSManagment.incValue(coverinNodeOfDC, treebankNode.getNodeType());
//			}
//		}
//	}
//
//	public void createDistributionOfFirstParent(Map<String, Integer> dist, List<TreebankNode> nodes){
//		String parentType = "none";
//		if (nodes.size() > 0)
//			parentType = nodes.get(nodes.size() - 1).getNodeType();
//
//		DSManagment.incValue(dist, parentType);
//	}
//
//	private List<TreebankNode> pruneNodes(List<TreebankNode> toBePruned){
//		List<TreebankNode> res = new ArrayList<TreebankNode>();
//		for (TreebankNode aNode: toBePruned){
//			if (targetTag.contains(aNode.getNodeType()))
//				res.add(aNode);
//		}
//		return res;
//	}
//
//	public static List<TreebankNode> getCoveringNodes(TokenList... tokenLists) {
//		List<TreebankNode> coveringAll = null;
//		for (TokenList tokenList: tokenLists){
//			if (coveringAll == null)
//				coveringAll = new ArrayList<TreebankNode>(JCasUtil.selectCovering(TreebankNode.class, tokenList));
//			else{
//				List<TreebankNode> coveringThis = JCasUtil.selectCovering(TreebankNode.class, tokenList);
//				Set<TreebankNode> notCoveredThis = new HashSet<TreebankNode>(coveringAll);
//				notCoveredThis.removeAll(coveringThis);
//				coveringAll.removeAll(notCoveredThis);
//			}
//
//		}
//
//		return coveringAll;
//	}
//
//
//	@Override
//	public void collectionProcessComplete()
//			throws AnalysisEngineProcessException {
//		PrintStream output;
//		try {
//			output = new PrintStream(new FileOutputStream("outputs/statistics/arg2node-dist.txt"));
//			output.println(String.format("Valid = %d, Invalid = %d", valid, invalid));
//			DSPrinter.printMap("Arg2Node-Dist", arg2NodeDistribution.entrySet(), output);
//			output.close();
//
//			output = new PrintStream(new FileOutputStream("outputs/statistics/relationNode-dist.txt"));
//			DSPrinter.printMap("Arg1Node-Dist", relationNodeDistribution.entrySet(), output);
//			output.close();
//
//			output = new PrintStream(new FileOutputStream("outputs/statistics/dcNode-dist.txt"));
//			DSPrinter.printMap("DCNode-Dist", coverinNodeOfDC.entrySet(), output);
//			output.close();
//
//			output = new PrintStream(new FileOutputStream("outputs/statistics/arg2ParentChildPattern.txt"));
//			DSPrinter.printMap("DCNode-Dist", arg2ParentChildPattern.entrySet(), output);
//			output.close();
//
//			output = new PrintStream(new FileOutputStream("outputs/statistics/relationPattern.txt"));
//			DSPrinter.printMap("Relation Patterns", relationPattern.entrySet(), output);
//			output.close();
//
//			output = new PrintStream(new FileOutputStream("outputs/statistics/depthPattern.txt"));
//			DSPrinter.printMap("Relation Depth", relationPatternDepth.entrySet(), output);
//			output.println();
//			output.println();
//			DSPrinter.printMap("Relation Sent Size", relationSize.entrySet(), output);
//			output.close();
//
//			output = new PrintStream(new FileOutputStream("outputs/statistics/distNodeType.txt"));
//			DSPrinter.printTable("Dist Node Type", distNodeType.entrySet(), output);
//			output.close();
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//
//
//	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
//		System.out.println("DiscourseArgumentsStatistics.main()");
//
//		DatasetPath dataset = new ConllDataset("train");
//		DatasetStatistics datasetStatistics = new DatasetStatistics(dataset, dataset.getXmiOutDir());
////		datasetStatistics.readDataset();
//		datasetStatistics.getStatistics(getDescription());
//
//		System.out.println("DiscourseArgumentsStatistics.main(): Done!");
//
//	}
}
