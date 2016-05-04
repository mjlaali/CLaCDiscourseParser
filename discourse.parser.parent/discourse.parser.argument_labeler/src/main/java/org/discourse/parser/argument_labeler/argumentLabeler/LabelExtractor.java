package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class LabelExtractor implements BiFunction<DCTreeNodeArgInstance, DiscourseConnective, String>{
	private boolean errorAnalysis; 
//	private Set<String> uniqWords = new HashSet<>();
	private Map<Constituent, Set<Token>> constituentsToTokens = new HashMap<>();
//	private PrintStream output = null;
	
	public LabelExtractor(boolean errorAnalysis, Map<Constituent, List<Token>> constituentsToTokens) {
		this.errorAnalysis = errorAnalysis;
		constituentsToTokens.forEach((k, v) -> this.constituentsToTokens.put(k, new HashSet<Token>(v)));
		
	}

	@Override
	public String apply(DCTreeNodeArgInstance instance, DiscourseConnective dc) {
//		try {
//			output = new PrintStream(new FileOutputStream("outputs/patterns.txt", true));
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(e);
//		}
		

		DiscourseRelation discourseRelation = dc.getDiscourseRelation();
		if (discourseRelation == null)
			return null;

		NodeArgType res = getNodeLabel(instance.getNode(), discourseRelation, constituentsToTokens, errorAnalysis);
		
		if (res == NodeArgType.Arg1 || res == NodeArgType.Arg2){
			createArgTreeNode(instance, dc, res);
		}
		
//		output.close();
		return res.toString();
	}

	public static NodeArgType getNodeLabel(Annotation ann, DiscourseRelation discourseRelation, 
			Map<? extends Annotation, Set<Token>> constituentsToTokens, boolean errorAnalysis) {
		Set<Token> arg1Tokens = new HashSet<Token>(TokenListTools.convertToTokens(discourseRelation.getArguments(0)));
		Set<Token> arg2Tokens = new HashSet<Token>(TokenListTools.convertToTokens(discourseRelation.getArguments(1)));
		Set<Token> dcTokens = new HashSet<Token>(TokenListTools.convertToTokens(discourseRelation.getDiscourseConnective()));

		Set<Token> nodeTokens = getCoveredToken(ann, constituentsToTokens);
		
		Set<Token> noneTokens = new HashSet<>(nodeTokens);
		noneTokens.removeAll(arg1Tokens);
		noneTokens.removeAll(arg2Tokens);
		noneTokens.removeAll(dcTokens);
		
		//divide node's tokens into different sets
		arg1Tokens.retainAll(nodeTokens);
		arg2Tokens.retainAll(nodeTokens);
		dcTokens.retainAll(nodeTokens);
		noneTokens.retainAll(nodeTokens);

		int[] cnts = {arg1Tokens.size(), arg2Tokens.size(), dcTokens.size(), noneTokens.size()};
		NodeArgType[] labels = {NodeArgType.Arg1, NodeArgType.Arg2, NodeArgType.DC, NodeArgType.None};
		
		NodeArgType res = NodeArgType.None;
		int max = 0;
		for (int i = 0; i < cnts.length; i++){
			if (cnts[i] > max){
				max = cnts[i];
				res = labels[i];
			}
		}
		
		if (errorAnalysis && nodeTokens.size() > max){
			System.out.println("LabelExtractor.apply()");
			System.out.println(Arrays.toString(cnts));
			Function<Set<Token>, String> convertToStr = (lst) -> lst.stream().map(Token::getCoveredText).collect(Collectors.joining(" "));
			System.out.printf("<%s> <%s> <%s> <%s>\n", convertToStr.apply(arg1Tokens), 
					convertToStr.apply(arg2Tokens), 
					convertToStr.apply(dcTokens), 
					convertToStr.apply(noneTokens));
		}
		return res;
	}

	public static void createArgTreeNode(DCTreeNodeArgInstance instance, DiscourseConnective dc, NodeArgType res) {
		ArgumentTreeNode argTreeNode = instance.getArgTreeNode(); 
		switch (res) {
		case Arg1:
			argTreeNode.setDiscourseArgument(dc.getDiscourseRelation().getArguments(0));
			break;
		case Arg2:
			argTreeNode.setDiscourseArgument(dc.getDiscourseRelation().getArguments(1));
			break;

		default:
			// does not matter
			break;
		}
		argTreeNode.addToIndexes();
	}

	private static Set<Token> getCoveredToken(Annotation ann, Map<? extends Annotation, Set<Token>> constituentsToTokens ) {
		Set<Token> nodeTokens;
		if (ann instanceof Token){
			nodeTokens = new HashSet<Token>();
			nodeTokens.add((Token)ann);
		} else if (ann instanceof Constituent)
			nodeTokens = constituentsToTokens.get(ann);
		else 
			throw new UnsupportedOperationException();
		return nodeTokens;
	}

//	private void printPattern(Set<Token> arg1Tokens, Set<Token> arg2Tokens, Set<Token> dcTokens,
//			Annotation constituent, int level) {
//		List<Annotation> childeren = getChilderen().apply(constituent);
//		StringBuilder sb = new StringBuilder();
//		for (Annotation child: childeren){
//			String type = getConstituentType().apply(child);
//			Set<Token> nodeTokens = getCoveredToken(child);
//			NodeArgType label = extractLabel(arg1Tokens, arg2Tokens, dcTokens, nodeTokens);
//			if (label == NodeArgType.None_Mixed)
//				label = secondLevelAnalysis(arg1Tokens, arg2Tokens, dcTokens, constituent, nodeTokens);
//			
//			sb.append(" (" + type + ":" + label + ")");
//		}
//		if (sb.length() > 0)
//			output.println(level + "\t" + sb.toString());
//		
//		for (Annotation child: childeren){
//			printPattern(arg1Tokens, arg2Tokens, dcTokens, child, level + 1);
//		}
//	}

//	private NodeArgType extractLabel(Set<Token> arg1Tokens, Set<Token> arg2Tokens, Set<Token> dcTokens, Set<Token> noneTokens,
//			Set<Token> nodeTokens) {
//		NodeArgType res;
//		if (arg1Tokens.containsAll(nodeTokens))
//			res = NodeArgType.Arg1;
//		else if (arg2Tokens.containsAll(nodeTokens))
//			res = NodeArgType.Arg2;
//		else if (dcTokens.containsAll(nodeTokens)){
//			res = NodeArgType.DC;
//		} else if (noneTokens.containsAll(nodeTokens)){
//			res = NodeArgType.None;
//		} else
//			res = NodeArgType.Mixed;
//		return res;
//	}

}