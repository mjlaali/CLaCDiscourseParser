package org.cleartk.ml.feature.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.cleartk.corpus.conll2015.SyntaxReader;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.TokenList;
import org.cleartk.ml.Feature;
import org.cleartk.test.util.DefaultTestBase;
import org.cleartk.token.type.Token;
import org.json.JSONException;
import org.junit.Test;

public class TokenListContextTest extends DefaultTestBase{
	private CleartkExtractor<TokenList, Token> extractor;

	@Test
	public void givenTwoSeparatedTokenInTokenListWhenGetCoverTextThenCoverTextDoesNotIncludeTokensInBetween() throws UIMAException, JSONException{
		extractor = new CleartkExtractor<TokenList, Token>(
	            Token.class,
	            new CoveredTextExtractor<Token>(),
	    		new TokenListContext());

	    new SyntaxReader().initJCas(
	            this.jCas,
	            "The quick brown fox jumped over the lazy dog.",
	            "The quick brown fox jumped over the lazy dog .",
	            "DT JJ JJ NN VBD IN DT JJ NN .", null);

	    List<Token> tokens = new ArrayList<Token>();
	    List<Token> sentTokens = new ArrayList<Token>(JCasUtil.select(jCas, Token.class));
	    
	    tokens.add(sentTokens.get(5));
	    tokens.add(sentTokens.get(1));

	    TokenList tokenList = new TokenList(jCas);
	    TokenListTools.initTokenList(jCas, tokenList, tokens);
	    
	    assertThat(TokenListTools.getTokenListText(tokenList)).isEqualTo("quick over");
	    
	    List<Feature> features = extractor.extract(jCas, tokenList);
	    
	    assertThat(features).hasSize(1);
	    assertThat(features.get(0).getValue()).isEqualTo("quick_over");
	    assertThat(features.get(0).getName()).isEqualTo("TokenListContext");

	}

}
