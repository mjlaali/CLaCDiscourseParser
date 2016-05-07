package org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler.nonNodes;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getTokenList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.discourse.parser.argument_labeler.argumentLabeler.NodeArgType;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PurifyDiscourseRelations implements SequenceClassifierConsumer<String, ArgumentTreeNode, Annotation> {
	private Map<Annotation, Set<Token>> constituentsToTokens = new HashMap<>();
	public PurifyDiscourseRelations(Map<Annotation, Set<Token>> constituentsToTokens) {
		this.constituentsToTokens = constituentsToTokens;
	}

	@Override
	public void accept(List<String> outcomes, ArgumentTreeNode aTreeNode, List<Annotation> instances) {
		Set<Token> noneTokens = new HashSet<>();
		for (int i = 0; i < outcomes.size(); i++){
			String outcome = outcomes.get(i);
			Annotation ann = instances.get(i);
			
			if (outcome.equals(NodeArgType.None.toString())){
				Set<Token> tokens = getTokenList(constituentsToTokens, Set.class).apply(ann);
				noneTokens.addAll(tokens);
			}
		}
		
		if (!noneTokens.isEmpty()){
			DiscourseArgument discourseArgument = aTreeNode.getDiscourseArgument();
			List<Token> tokens = TokenListTools.convertToTokens(discourseArgument);
			tokens.removeAll(noneTokens);
			TokenListTools.initTokenList(discourseArgument, tokens);
		}
	}

}
