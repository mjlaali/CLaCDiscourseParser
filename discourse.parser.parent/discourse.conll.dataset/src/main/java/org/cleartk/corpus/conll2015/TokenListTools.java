package org.cleartk.corpus.conll2015;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.jcas.cas.FSArray;
import org.cleartk.discourse.type.TokenList;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenListTools {
	public static Function<Character, Boolean> isWhiteSpace = Character::isWhitespace;

	public static TokenList initTokenList(TokenList tokenList, List<Token> tokens) {
		int begin = Integer.MAX_VALUE; 
		int end = -1;
		for (Token token: tokens){
			if (begin > token.getBegin())
				begin = token.getBegin();
			if (end < token.getEnd())
				end = token.getEnd();
		}
		
		if (tokens.size() == 0){	//just create annotation.
			begin = 0; end = 0;
		}
		
	    try {
			tokenList.setTokens(new FSArray(tokenList.getCAS().getJCas(), tokens.size()));
		} catch (CASException e) {
			throw new RuntimeException(e);
		}
	    FSCollectionFactory.fillArrayFS(tokenList.getTokens(), tokens);
	    tokenList.setBegin(begin);
	    tokenList.setEnd(end);
	    return tokenList;
	}

	public static String getTokenListText(TokenList tokenList){
		return getTokenListText(tokenList, isWhiteSpace);
	}
	
	public static String getTokenListText(TokenList tokenList, Function<Character, Boolean> isWhiteSpace){
		List<Token> tokens = TokenListTools.convertToTokens(tokenList);
		String text;
		try {
			text = tokenList.getCAS().getJCas().getDocumentText();
		} catch (CASException e) {
			throw new RuntimeException(e);
		}
		
		StringBuilder sb = new StringBuilder();
		for (Token token: tokens){
			if (token.getBegin() > 0 && sb.length() > 0 && 
					isWhiteSpace.apply(text.charAt(token.getBegin() - 1)))
				sb.append(" ");
			sb.append(token.getCoveredText());
		}
		return sb.toString();
	}

	public static boolean isEqualTokenList(TokenList firstTokenList,
			TokenList secondTokenList) {
		return isEqualTokenList(convertToTokens(firstTokenList), convertToTokens(secondTokenList));
	}
	
	public static boolean isEqualTokenList(List<Token> firstTokenList,
			List<Token> secondTokenList) {
		if ((firstTokenList == null && secondTokenList != null) 
				|| (firstTokenList != null && secondTokenList == null))
			return false;
		
		if (firstTokenList.size() != secondTokenList.size())
			return false;
		
		for (int i = 0; i < firstTokenList.size(); i++){
			if (!firstTokenList.get(i).equals(secondTokenList.get(i)))
				return false;
		}
		return true;
	}

	public static List<Token> convertToTokens(TokenList aTokenList) {
		FSArray tokens = aTokenList.getTokens();
		List<Token> tokenList = new ArrayList<Token>();
		for (int i = 0; i < tokens.size(); i++){
			tokenList.add((Token) tokens.get(i));
		}
		final AnnotationIndex<AnnotationFS> annotationIndex = tokens.getCAS().getAnnotationIndex();
		Collections.sort(tokenList, new Comparator<Token>() {
	
			@Override
			public int compare(Token o1, Token o2) {
				return annotationIndex.compare(o1, o2);
			}
		});
	
		return tokenList;

	}

}
