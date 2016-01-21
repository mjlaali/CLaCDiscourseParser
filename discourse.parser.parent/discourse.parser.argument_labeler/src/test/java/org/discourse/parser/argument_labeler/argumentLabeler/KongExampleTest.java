package org.discourse.parser.argument_labeler.argumentLabeler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseRelation;
import org.junit.Before;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class KongExampleTest {
	protected String parseTree = "(S (CC But)(NP (PRP its) (NNS competitors))(VP(VP (VBP have)(NP (RB much) (JJR broader) (NN business) (NNS interests)))(CC and)(RB so)(VP (VBP are)(VP (ADVP (RBR better))(VBN cushioned)(PP (IN against)(NP (NN price) (NNS swings)))))))";
	protected JCas aJCas;
	protected DiscourseRelation discourseRelation;
	protected String arg1 = "But its competitors have much broader business interests";
	protected String arg2 = "and are better cushioned against price swings";
	protected String dc = "so"; 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		new SyntaxReader().initJCas(aJCas, parseTree);
		List<Token> tokens = new ArrayList<Token>(JCasUtil.select(aJCas, Token.class));
		List<Token> arg2Tokens = new ArrayList<Token>(tokens.subList(8, 9));	//and
		arg2Tokens.addAll(tokens.subList(10, 16));
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelation(aJCas, RelationType.Explicit, "Contrast", dc, 
				tokens.subList(9, 10), 
				tokens.subList(0, 8), 
				arg2Tokens);
		
		discourseRelation.getDiscourseConnective().addToIndexes();
		assertThat(JCasUtil.select(aJCas, Token.class)).hasSize(16);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(0))).isEqualTo(arg1);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getArguments(1))).isEqualTo(arg2);
		assertThat(TokenListTools.getTokenListText(discourseRelation.getDiscourseConnective())).isEqualTo(dc);
	}
	
}
