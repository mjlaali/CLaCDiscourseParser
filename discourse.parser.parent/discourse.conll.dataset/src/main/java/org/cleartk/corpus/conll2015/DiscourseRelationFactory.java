package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DiscourseRelationFactory {
	
	public DiscourseRelation makeDiscourseRelationFrom(JCas aJCas, DiscourseRelationExample anExample) throws AnalysisEngineProcessException{
		aJCas.setDocumentText(anExample.getText());
		String text = aJCas.getDocumentText(); 
		new SyntaxReader().addSyntaxInformation(aJCas,anExample.getParseTree());;
		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
		List<String> strTokens = tokens.stream().map(Token::getCoveredText).collect(Collectors.toList());
		int dcIndex = strTokens.indexOf(anExample.getDiscourseConnective());
		int begin;
		
		begin = text.indexOf(anExample.getArg1());
		if (begin == -1)
			throw new RuntimeException(String.format("<%s> cannot be found in <%s>.", anExample.getArg1(), anExample.getText()));
		List<Token> arg1Tokens = JCasUtil.selectCovered(aJCas, Token.class, begin, begin + anExample.getArg1().length());

		List<Token> arg2Tokens = new ArrayList<>();
		for (String arg2Part: anExample.getArg2()){
			begin = text.indexOf(arg2Part);
			if (begin == -1)
				throw new RuntimeException(String.format("<%s> cannot be found in <%s>.", anExample.getArg2(), anExample.getText()));
			arg2Tokens.addAll(JCasUtil.selectCovered(aJCas, Token.class, begin, begin + arg2Part.length()));
		}
		
		DiscourseRelation discourseRelation = new DiscourseRelationFactory().makeDiscourseRelation(aJCas, 
				RelationType.Explicit, anExample.getSense(), anExample.getDiscourseConnective(), 
				Collections.singletonList(tokens.get(dcIndex)), 
				arg1Tokens, 
				arg2Tokens);
		
		discourseRelation.getDiscourseConnective();
		
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(0))).isEqualTo(anExample.getArg1());
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(1))).isEqualTo(
				DiscourseRelationExample.toString(anExample.getArg2()));
		assertThat(TokenListTools.getTokenListText(discourseRelation.getDiscourseConnective())).isEqualTo(anExample.getDiscourseConnective());
		return discourseRelation;
	}
	
	public DiscourseRelation makeSimpleRelation(JCas aJCas, String arg1, String arg2, String dc){
		String text = aJCas.getDocumentText();
		int idxArg1 = text.indexOf(arg1);
		int idxArg2 = text.indexOf(arg2);
		if (idxArg1 == -1 || idxArg2 == -1)
			throw new RuntimeException();
		List<Token> arg1Tokens = JCasUtil.selectCovered(Token.class, new Annotation(aJCas, idxArg1, idxArg1 + arg1.length()));
		List<Token> arg2Tokens = new ArrayList<Token>(JCasUtil.selectCovered(Token.class, new Annotation(aJCas, idxArg2, idxArg2 + arg2.length())));

		int idxDc = text.indexOf(dc);
		String dcTrimed = dc.trim();
		idxDc = idxDc + dc.indexOf(dcTrimed);
		List<Token> dcTokens = JCasUtil.selectCovered(Token.class, new Annotation(aJCas, idxDc, idxDc + dcTrimed.length()));
		arg2Tokens.removeAll(dcTokens);
		return makeDiscourseRelation(aJCas, RelationType.Explicit, null, dc, dcTokens, arg1Tokens, arg2Tokens);
	}

	public DiscourseRelation makeAnExplicitRelation(JCas aJCas, String sense, DiscourseConnective discourseConnective, 
			List<Token> arg1, List<Token> arg2) {
		
		List<Token> tokens = new ArrayList<Token>();
		
		DiscourseRelation discourseRelation = new DiscourseRelation(aJCas);
		discourseRelation.setRelationType(RelationType.Explicit.toString());
		discourseRelation.setSense(sense);

		discourseRelation.setDiscourseConnectiveText(TokenListTools.getTokenListText(discourseConnective));
		discourseRelation.setDiscourseConnective(discourseConnective);
		discourseConnective.setDiscourseRelation(discourseRelation);
		tokens.addAll(TokenListTools.convertToTokens(discourseConnective));
		
		initArguments(aJCas, arg1, arg2, tokens, discourseRelation);
		TokenListTools.initTokenList(aJCas, discourseRelation, tokens);

		return discourseRelation;
	}

	public DiscourseRelation makeAnImplicitRelation(JCas aJCas, String sense, List<Token> arg1, List<Token> arg2){
		RelationType type = RelationType.Implicit;
		
		return makeDiscourseRelation(aJCas, type, sense, null, null, arg1, arg2);
	}
	
	public DiscourseRelation makeDiscourseRelation(JCas aJCas,
			RelationType type, String sense, String discourseConnectiveText, 
			List<Token> discourseConnectiveTokens, 
			List<Token> arg1, List<Token> arg2) {
		List<Token> tokens = new ArrayList<Token>();
		
		DiscourseRelation discourseRelation = new DiscourseRelation(aJCas);
		discourseRelation.setRelationType(type.toString());
		discourseRelation.setSense(sense);
		
		discourseRelation.setDiscourseConnectiveText(discourseConnectiveText);
		if (type == RelationType.Explicit){
			DiscourseConnective discourseConnective = new DiscourseConnective(aJCas);
			TokenListTools.initTokenList(aJCas, discourseConnective, discourseConnectiveTokens);
			discourseRelation.setDiscourseConnective(discourseConnective);
			discourseConnective.setDiscourseRelation(discourseRelation);
			discourseConnective.setSense(sense);
			tokens.addAll(discourseConnectiveTokens);
		}
		
		initArguments(aJCas, arg1, arg2, tokens, discourseRelation);
		
		TokenListTools.initTokenList(aJCas, discourseRelation, tokens);
		return discourseRelation;
	}

	public void initArguments(JCas aJCas, List<Token> arg1, List<Token> arg2,
			List<Token> tokens, DiscourseRelation discourseRelation) {
		List<DiscourseArgument> arguments = new ArrayList<>();
		List<List<Token>> args = new ArrayList<List<Token>>();
		args.add(arg1); tokens.addAll(arg1);
		args.add(arg2); tokens.addAll(arg2);
		int idx = 0;
		
		for (ArgType argType: ArgType.values()){
			DiscourseArgument discourseArgument = new DiscourseArgument(aJCas);
			discourseArgument.setArgumentType(argType.toString());
			TokenListTools.initTokenList(aJCas, discourseArgument, args.get(idx++));
			discourseArgument.setDiscouresRelation(discourseRelation);
			arguments.add(discourseArgument);
		}
		
		discourseRelation.setArguments(new FSArray(aJCas, arguments.size()));
		FSCollectionFactory.fillArrayFS(discourseRelation.getArguments(), arguments);
	}
}
