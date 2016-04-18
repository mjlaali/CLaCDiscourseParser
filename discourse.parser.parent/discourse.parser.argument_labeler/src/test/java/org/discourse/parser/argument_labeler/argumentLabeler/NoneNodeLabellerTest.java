package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;
import org.junit.Before;
import org.junit.Test;

public class NoneNodeLabellerTest{
	private NoneNodeLabeller algorithmFactory = new NoneNodeLabeller();
	
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private ArgumentLabelerAlgorithmFactory argumentLabelerAlgorithmFactory = new ArgumentLabelerAlgorithmFactory();
	private DiscourseRelationExample example = new NoneNodeExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
//		discourseRelation.addToIndexes();
		List<DCTreeNodeArgInstance> instances = argumentLabelerAlgorithmFactory.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		argumentLabelerAlgorithmFactory.getLabelExtractor(aJCas).apply(instances, discourseRelation.getDiscourseConnective());
		
		Collection<ArgumentTreeNode> sequences = JCasUtil.select(aJCas, ArgumentTreeNode.class);
		List<String> candidates= sequences.stream().map(ArgumentTreeNode::getTreeNode)
				.map(getConstituentType()).collect(Collectors.toList());
		//only nodes that is arg1 or arg2 will be analyzed.
		assertThat(candidates).containsExactly("S", "S");
	}

	
	@Test
	public void whenExtractingSequencesThenOnlyTwoSequencesAreExtracted(){
		Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends ArgumentTreeNode> sequences = sequenceExtractor.apply(aJCas);
		List<String> candidates= sequences.stream().map(ArgumentTreeNode::getTreeNode)
				.map(getConstituentType()).collect(Collectors.toList());
		assertThat(candidates).containsExactly("S", "S");
	}
	
	@Test
	public void whenExtractingInstanceForTheFirstSequenceThen39InstanceAreExtracted(){
		Function<ArgumentTreeNode, List<Annotation>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends ArgumentTreeNode> sequences = sequenceExtractor.apply(aJCas);
		List<Annotation> instances = instanceExtractor.apply(sequences.iterator().next());
		
		assertThat(instances).hasSize(39);
	}

	
	@Test
	public void whenLabelingInstancesThenThereAre13NoneLabels(){
		Function<ArgumentTreeNode, List<Annotation>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends ArgumentTreeNode> sequences = sequenceExtractor.apply(aJCas);
		List<Annotation> instances = instanceExtractor.apply(sequences.iterator().next());
		BiFunction<List<Annotation>, ArgumentTreeNode, List<String>> labelExtractor = 
				algorithmFactory.getLabelExtractor(aJCas);
		
		List<String> labels = labelExtractor.apply(instances, sequences.iterator().next());
		
		long count = labels.stream().filter((l) -> l.equals(NodeArgType.None.toString())).count();
		assertThat(count).isEqualTo(13);
	}
	
	@Test
	public void whenExtractingFeaturesThenTheyAreCorrect(){
		Function<ArgumentTreeNode, List<Annotation>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends ArgumentTreeNode> sequences = sequenceExtractor.apply(aJCas);
		
		List<Annotation> instances = instanceExtractor.apply(sequences.iterator().next());
		List<List<Feature>> features = algorithmFactory.getFeatureExtractor(aJCas).apply(instances, sequences.iterator().next())
				.subList(0, 8);
		
		List<String> strFeatures = features.stream()
			.map((fs) -> fs.stream().map(
					(f) -> "<" + f.getName() + ":" + f.getValue().toString() + ">")
					.collect(Collectors.joining("-")))
			.collect(Collectors.toList());
		
		System.out.println(strFeatures);
		String[] goldFeatures = new String[]{
				"<nodeHead:so>-<consType:NP>",
				"<nodeHead:so>-<consType:NP>",
				"<nodeHead:so>-<consType:DT>",
				"<nodeHead:so>-<consType:NN>",
				"<nodeHead:so>-<consType:SBAR>",
				"<nodeHead:so>-<consType:S>",
				"<nodeHead:so>-<consType:NP>",
				"<nodeHead:so>-<consType:PRP>",
		};
				
		assertThat(strFeatures).containsExactly(goldFeatures);
	}
	
//	@Test
//	public void givenGoldLableWhenConstructingTheRelationThenItIsCorrect(){
//		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
//		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
//		List<String> goldLabels = Arrays.asList("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
//		
//		SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance> labeller = algorithmFactory.getLabeller(aJCas);
//		
//		labeller.accept(goldLabels, discourseRelation.getDiscourseConnective(), instances);
//		
//		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
//		assertThat(relations).hasSize(1);
//		DiscourseRelation relation = relations.iterator().next();
//		
//		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(arg1);
//		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(arg2);
//		assertThat(TokenListTools.getTokenListText(relation.getDiscourseConnective())).isEqualTo(dc);
//
//	}
//	
	
}
