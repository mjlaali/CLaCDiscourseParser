package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class LabelExtractor implements BiFunction<ArgumentInstance, DiscourseConnective, String>{
	

	@Override
	public String apply(ArgumentInstance instance, DiscourseConnective dc) {
		NodeArgType res;

		DiscourseRelation discourseRelation = dc.getDiscourseRelation();
		if (discourseRelation == null)
			return null;
		
		List<Token> arg1Tokens = TokenListTools.convertToTokens(discourseRelation.getArguments(0));
		List<Token> arg2Tokens = TokenListTools.convertToTokens(discourseRelation.getArguments(1));
		List<Token> dcTokens = TokenListTools.convertToTokens(dc);
		
		Annotation ann = instance.getInstance();
		List<Token> nodeTokens;
		if (ann instanceof Token){
			nodeTokens = Collections.singletonList((Token)ann);
		} else
			nodeTokens = JCasUtil.selectCovered(Token.class, ann);
		
		if (arg1Tokens.containsAll(nodeTokens))
			res = NodeArgType.Arg1;
		else if (arg2Tokens.containsAll(nodeTokens))
			res = NodeArgType.Arg2;
		else if (dcTokens.containsAll(nodeTokens)){
			res = NodeArgType.DC;
		} else 
			res = NodeArgType.Non;
		
		
		return res.toString();
	}
	
}