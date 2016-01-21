package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseConnective;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArgumentConstructor implements SequenceClassifierConsumer<String, DiscourseConnective, ArgumentInstance>{
	private JCas aJCas;
	private DiscourseRelationFactory relationFactory = new DiscourseRelationFactory();
	public ArgumentConstructor(JCas aJCas) {
		this.aJCas = aJCas;
	}

	@Override
	public void accept(List<String> outcomes, DiscourseConnective dc, List<ArgumentInstance> instances) {
		List<Token> arg1Tokens = new ArrayList<>();
		List<Token> arg2Tokens = new ArrayList<>();
		List<Token> dcTokens = new ArrayList<>();
		
		for (int i = 0; i < outcomes.size(); i++){
			NodeArgType nodeType = NodeArgType.valueOf(outcomes.get(i));
			Annotation ann = instances.get(i).getInstance();
			List<Token> tokens = null;
			if (ann instanceof Token){
				tokens = new ArrayList<>();
				tokens.add((Token)ann);
			} else
				tokens = JCasUtil.selectCovered(Token.class, ann);
			switch (nodeType) {
			case Arg1:
				arg1Tokens.addAll(tokens);
				break;
			case Arg2:
				arg2Tokens.addAll(tokens);
				break;
			case DC:
				dcTokens.addAll(tokens);
				break;
			default:
				break;
			}
		}
		
		relationFactory.makeAnExplicitRelation(aJCas, "", dc, arg1Tokens, arg2Tokens).addToIndexes();
	}
	
}