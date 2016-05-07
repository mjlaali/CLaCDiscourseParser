package org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getTokenList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.discourse.parser.argument_labeler.argumentLabeler.LabelExtractor;
import org.discourse.parser.argument_labeler.argumentLabeler.NodeArgType;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class ArgumentConstructor implements SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance>{
	private JCas aJCas;
	private DiscourseRelationFactory relationFactory = new DiscourseRelationFactory();
	private Map<Constituent, ? extends Collection<Token>> constituentToTokens;
	
	
	public ArgumentConstructor(JCas aJCas, Map<Constituent, ? extends Collection<Token>> constituentToTokens) {
		this.aJCas = aJCas;
		this.constituentToTokens = constituentToTokens;
	}

	@Override
	public void accept(List<String> outcomes, DiscourseConnective dc, List<DCTreeNodeArgInstance> instances) {
		if (!new HashSet<>(outcomes).contains(NodeArgType.Arg1.toString())){
			outcomes.remove(0);
			outcomes.add(0, NodeArgType.Arg1.toString());
		}
		
		List<Token> arg1Tokens = new ArrayList<>();
		List<Token> arg2Tokens = new ArrayList<>();
		List<Token> dcTokens = new ArrayList<>();
		
		List<DCTreeNodeArgInstance> nodesToBeAdded = new ArrayList<>();
		List<NodeArgType> types = new ArrayList<>();
		
		for (int i = 0; i < outcomes.size(); i++){
			NodeArgType nodeType = NodeArgType.valueOf(outcomes.get(i));
			DCTreeNodeArgInstance instance = instances.get(i);
			Annotation ann = instance.getNode();
			Collection<Token> tokens = getTokenList(constituentToTokens, List.class).apply(ann);
			switch (nodeType) {
			case Arg1:
				arg1Tokens.addAll(tokens);
				nodesToBeAdded.add(instance);
				types.add(NodeArgType.Arg1);
				break;
			case Arg2:
				arg2Tokens.addAll(tokens);
				nodesToBeAdded.add(instance);
				types.add(NodeArgType.Arg2);
				break;
			case DC:
				dcTokens.addAll(tokens);
				break;
			default:
				break;
			}
		}
		
		
		DiscourseRelation relation = relationFactory.makeAnExplicitRelation(aJCas, dc.getSense(), dc, arg1Tokens, arg2Tokens);
		relation.addToIndexes();
		relation.getArguments(0).addToIndexes();
		relation.getArguments(1).addToIndexes();
		

		for (int i = 0; i < nodesToBeAdded.size(); i++){
			LabelExtractor.createArgTreeNode(nodesToBeAdded.get(i), dc, types.get(i));
		}
		
	}
	
}