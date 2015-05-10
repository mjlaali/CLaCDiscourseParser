package org.cleartk.discourse_parsing.module.argumentLabeler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;

public class DiscourseArgumentLabeler extends JCasAnnotator_ImplBase{
	private DiscourseRelationFactory discourseRelationFactory = new DiscourseRelationFactory();
	private Set<String> potentialNodeTypes = new TreeSet<String>(Arrays.asList(new String[]{"S", "SBAR"}));

	public static AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		return AnalysisEngineFactory.createEngineDescription(DiscourseArgumentLabeler.class);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		List<DiscourseConnective> dcs = new ArrayList<DiscourseConnective>(JCasUtil.select(aJCas, DiscourseConnective.class));
		for (DiscourseConnective dc: dcs){
			List<TreebankNode> coveringNodes = new ArrayList<TreebankNode>(JCasUtil.selectCovering(TreebankNode.class, dc));
			Collections.reverse(coveringNodes);
			
			Annotation[] argsNode = new Annotation[2];
			int idx = 1;
			for (TreebankNode treebankNode: coveringNodes){
				if (potentialNodeTypes.contains(treebankNode.getNodeType())){
					argsNode[idx--] = treebankNode;
					if (idx == -1)
						break;
				}
			}
			
			if (idx == 1){
				argsNode[idx--] = JCasUtil.selectCovering(Sentence.class, dc).iterator().next();
			}
			if (idx == 0){
				List<Sentence> selectPreceding = JCasUtil.selectPreceding(Sentence.class, dc, 1);
				if (selectPreceding.size() == 0){
					System.err.println("DiscourseArgumentLabeler.process()");
					continue;
				}
				argsNode[idx] = selectPreceding.get(0);
			}
			
			List<Token> arg1Tokens = new ArrayList<Token>(JCasUtil.selectCovered(Token.class, argsNode[0]));
			List<Token> arg2Tokens = new ArrayList<Token>(JCasUtil.selectCovered(Token.class, argsNode[1]));
			if (TokenListTools.getTokenListText(dc).equals("when"))
				System.out.println("DiscourseArgumentLabeler.process()");
			List<Token> dcTokens = JCasUtil.selectCovered(Token.class, dc);
			
			arg1Tokens.removeAll(arg2Tokens);
			arg1Tokens.removeAll(dcTokens);
			
			arg2Tokens.removeAll(dcTokens);
			
			discourseRelationFactory.makeDiscourseRelation(aJCas, RelationType.Explicit, null, 
					TokenListTools.getTokenListText(dc), dcTokens, arg1Tokens, arg2Tokens).addToIndexes();
		}
	}

}
