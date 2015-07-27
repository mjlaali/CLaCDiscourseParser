package org.cleartk.corpus.conll2015;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.type.ConllToken;
import org.cleartk.token.type.Token;
import org.cleartk.util.treebank.TopTreebankNode;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.cleartk.util.treebank.TreebankNode;

public class SyntaxReader{
	public String initJCas(JCas jCas, String parseTree) {
		 String text = TreebankFormatParser.inferPlainText(parseTree);
		 String tokens = getTokens(text, parseTree);
		 initJCas(jCas, text, tokens, null, parseTree);
		 return tokens;
	}
	
	private String getTokens(String text, String parseTree) {
		List<TopTreebankNode> parseDocument = TreebankFormatParser.parseDocument(parseTree, 0, text);
		StringBuilder sb = new StringBuilder();
		for (TopTreebankNode aNode: parseDocument){
			addTokens(sb, aNode);
		}
		return sb.toString();
	}

	private void addTokens(StringBuilder sb, TreebankNode aNode) {
		if (aNode.isLeaf()){
			if (sb.length() != 0)
				sb.append(" ");
			sb.append(aNode.getText());
		} else
			for (TreebankNode node: aNode.getChildren()){
				addTokens(sb, node);
			}
	}

	public void initJCas(JCas jCas, String text, String tokens, String poses, String parseTree){
		jCas.setDocumentText(text);
		
		int offset = 0;
		
		List<Token> conllTokens = new ArrayList<Token>();
		int idx = 0;
		String[] split = tokens.split(" ");
		
		for (String tokenTxt: split){
			ConllToken token = new ConllToken(jCas, offset, offset + tokenTxt.length());
			token.setDocumentOffset(idx);
			token.addToIndexes();
			conllTokens.add(token);
			offset = offset + tokenTxt.length();
			while (offset < text.length() && text.charAt(offset) == ' ')
				offset++;
		}
		
		if (poses != null){
			addPoses(conllTokens, poses);
		}
		
		if (parseTree != null)
			addSyntacticConstituents(jCas, conllTokens, parseTree);

	}
	
	private void addPoses(List<Token> conllTokens, String poses) {
		String[] arPoses = poses.split(" ");
		if (conllTokens.size() != arPoses.length)
			throw new RuntimeException(conllTokens.size() + "<>" + poses.length());

		Pattern stanfordPattern = Pattern.compile("(\\w+/)(\\w)");

		for (int i = 0; i < conllTokens.size(); i++){
			String pos = arPoses[i];
			Matcher matcher = stanfordPattern.matcher(pos);
			if (matcher.matches()){
				pos = matcher.group(2);
				conllTokens.get(i).setPos(pos);
			}

		}
	
	}

	public void addSyntacticConstituents(JCas aJCas, List<Token> sentTokens, String parseTree){
		//create fragment parser if there is no parser. It makes the program simpler
		if (parseTree.trim().equals(ConllSyntaxGoldAnnotator.NO_PARSE)){
			StringBuilder buffer = new StringBuilder();
			if (sentTokens.size() != 0){
				buffer.append("(TOP");
				for (int i = 0; i < sentTokens.size(); i++){
					Token jsonWord = sentTokens.get(i);
					String coveredText = jsonWord.getCoveredText();
					if (coveredText.equals("(") || coveredText.equals(")"))
						coveredText = jsonWord.getPos();
					buffer.append(String.format(" (%s %s)", jsonWord.getPos(), coveredText));
				}
				buffer.append(")\n");
			}
			parseTree = buffer.toString();
			//otherwise ignore this sentence
		} 

		TopTreebankNode topNode = TreebankFormatParser.parse(parseTree);
		syncPosition(topNode, sentTokens.iterator());
		org.cleartk.corpus.penntreebank.TreebankNodeConverter.convert(
				topNode,
				aJCas,
				true);
	}

	private void syncPosition(TreebankNode node, Iterator<Token> tokensIter) {
		if (node.isLeaf()){
			Token token;
			try {
				token = tokensIter.next();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (!token.getCoveredText().equals(node.getText()))
				System.err.println("SyntaxReader.syncPosition():" + token.getCoveredText() + "<>" + node.getText());
			node.setTextBegin(token.getBegin());
			node.setTextEnd(token.getEnd());
			if (token.getPos() == null)
				token.setPos(node.getParent().getType());
		} else {
			int min = Integer.MAX_VALUE, max = -1;
			for (TreebankNode child: node.getChildren()){
				syncPosition(child, tokensIter);
				if (min > child.getTextBegin()){
					min = child.getTextBegin();
				}
				if (max < child.getTextEnd()){
					max = child.getTextEnd();
				}
			}
			node.setTextBegin(min);
			node.setTextEnd(max);

		}
	}
	
	
}