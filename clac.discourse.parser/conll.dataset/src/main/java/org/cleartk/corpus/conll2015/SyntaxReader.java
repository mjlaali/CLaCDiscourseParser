package org.cleartk.corpus.conll2015;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeNode;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeToJCasConverter;
import de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils;

public class SyntaxReader{
	private PennTreeToJCasConverter converter;
	private MappingProvider posMappingProvider = MappingProviderFactory.createPosMappingProvider(null, null, (String)null);
	private MappingProvider constituentMappingProvider = MappingProviderFactory.createConstituentMappingProvider(null, null, (String)null);
	
	public SyntaxReader() {
		converter = new PennTreeToJCasConverter(
				posMappingProvider, 
				constituentMappingProvider);
	}
	
//	public String initJCas(JCas jCas, String parseTree) {
//		 String text = TreebankFormatParser.inferPlainText(parseTree);
//		 String tokens = getTokens(text, parseTree);
//		 initJCas(jCas, text, tokens, null, parseTree);
//		 return tokens;
//	}
//	
//	private String getTokens(String text, String parseTree) {
//		List<TopTreebankNode> parseDocument = TreebankFormatParser.parseDocument(parseTree, 0, text);
//		StringBuilder sb = new StringBuilder();
//		for (TopTreebankNode aNode: parseDocument){
//			addTokens(sb, aNode);
//		}
//		return sb.toString();
//	}
//
//	private void addTokens(StringBuilder sb, TreebankNode aNode) {
//		if (aNode.isLeaf()){
//			if (sb.length() != 0)
//				sb.append(" ");
//			sb.append(aNode.getText());
//		} else
//			for (TreebankNode node: aNode.getChildren()){
//				addTokens(sb, node);
//			}
//	}
//
//	public void initJCas(JCas jCas, String text, String tokens, String poses, String parseTree){
//		jCas.setDocumentText(text);
//		
//		int offset = 0;
//		
//		List<Token> conllTokens = new ArrayList<Token>();
//		int idx = 0;
//		String[] split = tokens.split(" ");
//		
//		for (String tokenTxt: split){
//			ConllToken token = new ConllToken(jCas, offset, offset + tokenTxt.length());
//			token.setDocumentOffset(idx);
//			token.addToIndexes();
//			conllTokens.add(token);
//			offset = offset + tokenTxt.length();
//			while (offset < text.length() && text.charAt(offset) == ' ')
//				offset++;
//		}
//		
//		if (poses != null){
//			addPoses(conllTokens, poses);
//		}
//		
//		if (parseTree != null)
//			addSyntacticConstituents(jCas, conllTokens, parseTree);
//
//	}
//	
//	private void addPoses(List<Token> conllTokens, String poses) {
//		String[] arPoses = poses.split(" ");
//		if (conllTokens.size() != arPoses.length)
//			throw new RuntimeException(conllTokens.size() + "<>" + poses.length());
//
//		Pattern stanfordPattern = Pattern.compile("(\\w+/)(\\w)");
//
//		for (int i = 0; i < conllTokens.size(); i++){
//			String pos = arPoses[i];
//			Matcher matcher = stanfordPattern.matcher(pos);
//			if (matcher.matches()){
//				pos = matcher.group(2);
//				conllTokens.get(i).setPos(pos);
//			}
//
//		}
//	
//	}
//
	
	
	public void addSyntacticConstituents(Sentence aSentence, String parseTree) throws AnalysisEngineProcessException{
		//create fragment parser if there is no parser. It makes the program simpler
		List<Token> sentTokens = JCasUtil.selectCovering(Token.class, aSentence);
		if (parseTree.trim().equals(ConllSyntaxGoldAnnotator.NO_PARSE)){
			StringBuilder buffer = new StringBuilder();
			if (sentTokens.size() != 0){
				buffer.append("(TOP");
				for (int i = 0; i < sentTokens.size(); i++){
					Token jsonWord = sentTokens.get(i);
					String coveredText = jsonWord.getCoveredText();
					if (coveredText.equals("(") || coveredText.equals(")"))
						coveredText = jsonWord.getPos().getPosValue();
					buffer.append(String.format(" (%s %s)", jsonWord.getPos(), coveredText));
				}
				buffer.append(")\n");
			}
			parseTree = buffer.toString();
			//otherwise ignore this sentence
		} 

		posMappingProvider.configure(aSentence.getCAS());
		constituentMappingProvider.configure(aSentence.getCAS());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		converter.convertPennTree(aSentence, parsePennTree);
	}
//
//	private void syncPosition(TreebankNode node, Iterator<Token> tokensIter) {
//		if (node.isLeaf()){
//			Token token;
//			try {
//				token = tokensIter.next();
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//			if (!token.getCoveredText().equals(node.getText()))
//				System.err.println("SyntaxReader.syncPosition():" + token.getCoveredText() + "<>" + node.getText());
//			node.setTextBegin(token.getBegin());
//			node.setTextEnd(token.getEnd());
//			if (token.getPos() == null)
//				token.setPos(node.getParent().getType());
//		} else {
//			int min = Integer.MAX_VALUE, max = -1;
//			for (TreebankNode child: node.getChildren()){
//				syncPosition(child, tokensIter);
//				if (min > child.getTextBegin()){
//					min = child.getTextBegin();
//				}
//				if (max < child.getTextEnd()){
//					max = child.getTextEnd();
//				}
//			}
//			node.setTextBegin(min);
//			node.setTextEnd(max);
//
//		}
//	}
//	
//	
}