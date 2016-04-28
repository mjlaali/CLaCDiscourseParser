package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseRelation;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;
import org.junit.Before;

public class SentArg1ExampleTest {
	private NoneNodeLabeller algorithmFactory = new NoneNodeLabeller();
	
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private ArgumentLabelerAlgorithmFactory argumentLabelerAlgorithmFactory = new ArgumentLabelerAlgorithmFactory();
	private DiscourseRelationExample example = new SentArg1Example();
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
		List<DCTreeNodeArgInstance> instances = argumentLabelerAlgorithmFactory.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		argumentLabelerAlgorithmFactory.getLabelExtractor(aJCas).apply(instances, discourseRelation.getDiscourseConnective());
		
		Collection<ArgumentTreeNode> sequences = JCasUtil.select(aJCas, ArgumentTreeNode.class);
		List<String> candidates= sequences.stream().map(ArgumentTreeNode::getTreeNode)
				.map(getConstituentType()).collect(Collectors.toList());
		//only nodes that is arg1 or arg2 will be analyzed.
		assertThat(candidates).containsExactly("S", "S");
	}


}
