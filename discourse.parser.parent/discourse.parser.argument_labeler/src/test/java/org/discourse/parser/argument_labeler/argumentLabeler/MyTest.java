package org.discourse.parser.argument_labeler.argumentLabeler;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class MyTest {

	@Test
	public void test() throws UIMAException{
		JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText("it is a test");
		
		Token token = new Token(jcas, 0, 2);
		token.addToIndexes();
		token.removeFromIndexes();
		token.addToIndexes();
		
		assertThat(JCasUtil.select(jcas, Token.class)).hasSize(1);
	}
}
