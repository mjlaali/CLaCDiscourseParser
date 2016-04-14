package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getChilderen;
import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class LabelExtractor implements BiFunction<DCTreeNodeArgInstance, DiscourseConnective, String>{
	private boolean errorAnalysis; 
	private Set<String> uniqWords = new HashSet<>();
	private Map<Constituent, List<Token>> constituentsToTokens;
	private PrintStream output = null;
	
	public LabelExtractor(boolean errorAnalysis, Map<Constituent, List<Token>> constituentsToTokens) {
		this.errorAnalysis = errorAnalysis;
		this.constituentsToTokens = constituentsToTokens;
		
	}

	@Override
	public String apply(DCTreeNodeArgInstance instance, DiscourseConnective dc) {
		try {
			output = new PrintStream(new FileOutputStream("outputs/patterns.txt", true));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		NodeArgType res;

		DiscourseRelation discourseRelation = dc.getDiscourseRelation();
		if (discourseRelation == null)
			return null;

		List<Token> arg1Tokens = TokenListTools.convertToTokens(discourseRelation.getArguments(0));
		List<Token> arg2Tokens = TokenListTools.convertToTokens(discourseRelation.getArguments(1));
		List<Token> dcTokens = TokenListTools.convertToTokens(dc);

		Annotation ann = instance.getNode();
		List<Token> nodeTokens = getCoveredToken(ann);

		res = extractLabel(arg1Tokens, arg2Tokens, dcTokens, nodeTokens);

		if (res == NodeArgType.None){
			if (errorAnalysis){
				res = secondLevelAnalysis(arg1Tokens, arg2Tokens, dcTokens, ann, nodeTokens);
				if (res != NodeArgType.None)
					printPattern(arg1Tokens, arg2Tokens, dcTokens, ann, 0);
			}
		}

		output.close();
		return res.toString();
	}

	private NodeArgType secondLevelAnalysis(List<Token> arg1Tokens, List<Token> arg2Tokens,
			List<Token> dcTokens, Annotation ann, List<Token> nodeTokens) {
		int cntArg1, cntArg2, cntDC;
		int size = nodeTokens.size();
		List<Token> nonNodes = new ArrayList<>(nodeTokens);
		nonNodes.removeAll(arg2Tokens);
		cntArg2 = size - nonNodes.size();
		nonNodes.removeAll(arg1Tokens);
		cntArg1 = size - nonNodes.size() - cntArg2;
		nonNodes.removeAll(dcTokens);
		cntDC = size - nonNodes.size() - cntArg2 - cntArg1;

		NodeArgType res = NodeArgType.None;
		int[] cnts = {cntArg1, cntArg2, cntDC};
		NodeArgType[] labels = {NodeArgType.Arg1, NodeArgType.Arg2, NodeArgType.DC};
		int max = 0;
		for (int i = 0; i < cnts.length; i++){
			if (cnts[i] > max){
				max = cnts[i];
				res = labels[i];
			}
		}
		
		
		if (errorAnalysis){
//					System.out.println(Arrays.toString(cnts));
			if (res != NodeArgType.None){
				List<String> words = nonNodes.stream().map(Token::getCoveredText).collect(Collectors.toList());
				uniqWords.addAll(words);
				String toStr = words.stream().collect(Collectors.joining(" "));
				System.out.printf("None tokens: %s\n", toStr);
				System.out.printf("Constituent Text: %s\n", ann.getCoveredText());
				System.out.println();
				
				switch (res) {
				case Arg1:
					return NodeArgType.None_Arg1;
				case Arg2:
					return NodeArgType.None_Arg2;
				case DC:
					return NodeArgType.None_DC;

				default:
				}
			}
		}
		return res;
	}

	private List<Token> getCoveredToken(Annotation ann) {
		List<Token> nodeTokens;
		if (ann instanceof Token){
			nodeTokens = Collections.singletonList((Token)ann);
		} else if (ann instanceof Constituent)
			nodeTokens = constituentsToTokens.get(ann);
		else 
			throw new UnsupportedOperationException();
		return nodeTokens;
	}

	private void printPattern(List<Token> arg1Tokens, List<Token> arg2Tokens, List<Token> dcTokens,
			Annotation constituent, int level) {
		List<Annotation> childeren = getChilderen().apply(constituent);
		StringBuilder sb = new StringBuilder();
		for (Annotation child: childeren){
			String type = getConstituentType().apply(child);
			List<Token> nodeTokens = getCoveredToken(child);
			NodeArgType label = extractLabel(arg1Tokens, arg2Tokens, dcTokens, nodeTokens);
			if (label == NodeArgType.None)
				label = secondLevelAnalysis(arg1Tokens, arg2Tokens, dcTokens, constituent, nodeTokens);
			
			sb.append(" (" + type + ":" + label + ")");
		}
		if (sb.length() > 0)
			output.println(level + "\t" + sb.toString());
		
		for (Annotation child: childeren){
			printPattern(arg1Tokens, arg2Tokens, dcTokens, child, level + 1);
		}
	}

	private NodeArgType extractLabel(List<Token> arg1Tokens, List<Token> arg2Tokens, List<Token> dcTokens,
			List<Token> nodeTokens) {
		NodeArgType res;
		if (arg1Tokens.containsAll(nodeTokens))
			res = NodeArgType.Arg1;
		else if (arg2Tokens.containsAll(nodeTokens))
			res = NodeArgType.Arg2;
		else if (dcTokens.containsAll(nodeTokens)){
			res = NodeArgType.DC;
		} else {
			res = NodeArgType.None;
		}
		return res;
	}

}