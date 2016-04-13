package org.discourse.parser.argument_labeler.argumentLabeler;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArgumentConstructor implements SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance>{
	private JCas aJCas;
	private DiscourseRelationFactory relationFactory = new DiscourseRelationFactory();
	public ArgumentConstructor(JCas aJCas) {
		this.aJCas = aJCas;
	}

	@Override
	public void accept(List<String> outcomes, DiscourseConnective dc, List<DCTreeNodeArgInstance> instances) {
		List<Token> arg1Tokens = new ArrayList<>();
		List<Token> arg2Tokens = new ArrayList<>();
		List<Token> dcTokens = new ArrayList<>();
		
		for (int i = 0; i < outcomes.size(); i++){
			NodeArgType nodeType = NodeArgType.valueOf(outcomes.get(i));
			Annotation ann = instances.get(i).getNode();
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
		
		DiscourseRelation relation = relationFactory.makeAnExplicitRelation(aJCas, dc.getSense(), dc, arg1Tokens, arg2Tokens);
		relation.getArguments(0).addToIndexes();
		relation.getArguments(1).addToIndexes();
		relation.addToIndexes();
		
	}
	
}