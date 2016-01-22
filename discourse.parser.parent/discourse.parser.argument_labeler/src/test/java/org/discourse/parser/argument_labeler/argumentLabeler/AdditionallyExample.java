package org.discourse.parser.argument_labeler.argumentLabeler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseRelation;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class AdditionallyExample implements DiscourseRelatoinExample {
	//A new contract is in place. Additionally, staff will be offered a bonus scheme.
	protected String[] parseTrees = new String[]{"(ROOT (S (NP (DT A) (JJ new) (NN contract)) (VP (VBZ is) (PP (IN in) (NP (NN place)))) (. .)))", "(ROOT (S (ADVP (RB Additionally)) (, ,) (NP (NN staff)) (VP (MD will) (VP (VB be) (VP (VBN offered) (NP (DT a) (NN bonus) (NN scheme))))) (. .)))"};
	protected JCas aJCas;
	protected DiscourseRelation discourseRelation;
	protected String arg1 = "A new contract is in place .";
	protected String arg2 = ", staff will be offered a bonus scheme .";
	protected String dc = "Additionally"; 
	
	public AdditionallyExample() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		new SyntaxReader().initJCas(aJCas, parseTrees);
		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
		List<String> strTokens = tokens.stream().map(Token::getCoveredText).collect(Collectors.toList());
		int dcIndex = strTokens.indexOf("Additionally");
		List<Token> arg2Tokens = new ArrayList<Token>(tokens.subList(8, 9));	//and
		arg2Tokens.addAll(tokens.subList(10, 16));
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelation(aJCas, RelationType.Explicit, "Contrast", dc, 
				Collections.singletonList(tokens.get(dcIndex)), 
				tokens.subList(0, dcIndex), 
				tokens.subList(dcIndex + 1, tokens.size()));
		
		discourseRelation.getDiscourseConnective().addToIndexes();
	}
	
	@Override
	public JCas getJCas() {
		return aJCas;
	}
	
	@Override
	public DiscourseRelation getRelation() {
		return discourseRelation;
	}
	
	@Override
	public String getArg1() {
		return arg1;
	}
	
	@Override
	public String getArg2() {
		return arg2;
	}
	
	@Override
	public String getDc() {
		return dc;
	}
	
	@Test
	public void test(){
		assertThat(JCasUtil.select(aJCas, Token.class)).hasSize(17);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(0))).isEqualTo(arg1);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(1))).isEqualTo(arg2);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getDiscourseConnective())).isEqualTo(dc);
	}
}
