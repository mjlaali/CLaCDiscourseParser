package org.cleartk.corpus.conll2015;

import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.treebank.TopTreebankNode;
import org.cleartk.util.treebank.TreebankFormatParser;
import org.cleartk.util.treebank.TreebankNode;

import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
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
	
	public void initJCas(JCas jCas, String[] parseTrees) throws AnalysisEngineProcessException{
		posMappingProvider.configure(jCas.getCas());
		constituentMappingProvider.configure(jCas.getCas());
		converter.setCreatePosTags(true);
		StringBuilder sb = new StringBuilder();
		
		for (String parseTree: parseTrees){
			PennTreeNode parsePennTree = PennTreeUtils.parsePennTree(parseTree);
			try {
				converter.convertPennTree(jCas, sb, parsePennTree);
			} catch (Exception e) {
				System.err.println("\n\n**************************");
				System.err.println(parseTree);
				e.printStackTrace();
			}
		}
		
		jCas.setDocumentText(sb.toString());
		Collection<ROOT> roots = JCasUtil.select(jCas, ROOT.class);
		roots.stream().map((r) -> new Sentence(jCas, r.getBegin(), r.getEnd())).forEach((s) -> s.addToIndexes());
	
	}
	
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
	
	public void addSyntaxInformation(JCas aJCas, String[] parseTrees) throws AnalysisEngineProcessException{
		int begin = 0;
		for (String parseTree: parseTrees){
			int end = tokenize(aJCas, begin, parseTree);
			Sentence aSentence = new Sentence(aJCas, begin, end);
			aSentence.addToIndexes();
			begin = end;
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
	}

	private int tokenize(JCas aJCas, int begin, String parseTree) {
		List<TopTreebankNode> parseDocument = TreebankFormatParser.parseDocument(parseTree, begin, aJCas.getDocumentText());
		int endOffset = 0;
		for (TopTreebankNode aNode: parseDocument){
			endOffset = addTokens(aJCas, aNode);
		}
		
		return endOffset;
	}

	private int addTokens(JCas aJCas, TreebankNode aNode) {
		if (aNode.isLeaf()){
			new Token(aJCas, aNode.getTextBegin(), aNode.getTextEnd()).addToIndexes();
			return aNode.getTextEnd();
		} else{
			int endOffset = -1;
			for (TreebankNode node: aNode.getChildren()){
				endOffset = addTokens(aJCas, node);
			}
			return endOffset;
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