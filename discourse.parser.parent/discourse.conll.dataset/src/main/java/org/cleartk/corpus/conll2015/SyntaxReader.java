package org.cleartk.corpus.conll2015;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

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
	public void initJCas(JCas jCas, String parseTree) throws AnalysisEngineProcessException{
		posMappingProvider.configure(jCas.getCas());
		constituentMappingProvider.configure(jCas.getCas());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		converter.setCreatePosTags(true);
		try {
			StringBuilder sb = new StringBuilder();
			converter.convertPennTree(jCas, sb, parsePennTree);
			jCas.setDocumentText(sb.toString());
		} catch (Exception e) {
			System.err.println("\n\n**************************");
			System.err.println(parseTree);
			e.printStackTrace();
		}
	
	}

	public void addSyntacticConstituents(Sentence aSentence, String parseTree) throws AnalysisEngineProcessException{
		//create fragment parser if there is no parser. It makes the program simpler
		List<Token> sentTokens = JCasUtil.selectCovering(Token.class, aSentence);
		parseTree = parseTree.trim();
		if (parseTree.equals(ConllSyntaxGoldAnnotator.NO_PARSE) || parseTree.isEmpty()){
			StringBuilder buffer = new StringBuilder();
			if (sentTokens.size() != 0){
				buffer.append("(ROOT");
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

		if (parseTree.isEmpty())
			return;
		posMappingProvider.configure(aSentence.getCAS());
		constituentMappingProvider.configure(aSentence.getCAS());
		PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
		converter.setCreatePosTags(true);
		try {
			converter.convertPennTree(aSentence, parsePennTree);
		} catch (Exception e) {
			System.err.println("\n\n**************************");
			System.err.println(parseTree);
			e.printStackTrace();
		}
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