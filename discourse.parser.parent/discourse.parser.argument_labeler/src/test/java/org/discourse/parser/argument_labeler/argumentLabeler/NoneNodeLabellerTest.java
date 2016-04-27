package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
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
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseArgument;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.discourse.parser.argument_labeler.argumentLabeler.type.ArgumentTreeNode;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

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
		List<DCTreeNodeArgInstance> instances = argumentLabelerAlgorithmFactory.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		argumentLabelerAlgorithmFactory.getLabelExtractor(aJCas).apply(instances, discourseRelation.getDiscourseConnective());
		
		Collection<ArgumentTreeNode> sequences = JCasUtil.select(aJCas, ArgumentTreeNode.class);
		List<String> candidates= sequences.stream().map(ArgumentTreeNode::getTreeNode)
				.map(getConstituentType()).collect(Collectors.toList());
		//only nodes that is arg1 or arg2 will be analyzed.
		assertThat(candidates).containsExactly("S", "S");
	}


//	private void labelWithGoldData(List<DCTreeNodeArgInstance> instances) {
//		List<String> outcomes = Arrays.asList("None", "Arg1", "None", "DC", "Arg2", "None", "None", "None", "None", "None");
//		assertThat(outcomes.size()).isEqualTo(instances.size());
//		argumentLabelerAlgorithmFactory.getLabeller(aJCas).accept(outcomes, discourseRelation.getDiscourseConnective(), instances);;
//	}

	
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
//		System.out.println(labels);
//		System.out.println(instances.stream().map(getConstituentType()).collect(Collectors.toList()));
		
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
		
//		System.out.println(strFeatures.stream().collect(Collectors.joining("\n")));
		String[] goldFeatures = new String[]{
				"<nodeHead:impression>-<consType:NP>-<position:left>-<parentPattern:S->NP-VP>-<grandParentPattern:S->S-,-CC-S>-<argumentType:Arg1>-<leftSibling:null>-<rightSibling:VP>",
				 
				"<nodeHead:impression>-<consType:NP>-<position:left>-<parentPattern:NP->NP-SBAR>-<grandParentPattern:S->NP-VP>-<argumentType:Arg1>-<leftSibling:null>-<rightSibling:SBAR>",
				"<nodeHead:the>-<consType:DT>-<position:left>-<parentPattern:NP->DT-NN>-<grandParentPattern:NP->NP-SBAR>-<argumentType:Arg1>-<leftSibling:null>-<rightSibling:NN>",
				"<nodeHead:impression>-<consType:NN>-<position:middle>-<parentPattern:NP->DT-NN>-<grandParentPattern:NP->NP-SBAR>-<argumentType:Arg1>-<leftSibling:DT>-<rightSibling:null>",
				"<nodeHead:got>-<consType:SBAR>-<position:middle>-<parentPattern:NP->NP-SBAR>-<grandParentPattern:S->NP-VP>-<argumentType:Arg1>-<leftSibling:NP>-<rightSibling:null>",
				"<nodeHead:got>-<consType:S>-<position:middle>-<parentPattern:SBAR->S>-<grandParentPattern:NP->NP-SBAR>-<argumentType:Arg1>-<leftSibling:null>-<rightSibling:null>",
				"<nodeHead:i>-<consType:NP>-<position:middle>-<parentPattern:S->NP-VP>-<grandParentPattern:SBAR->S>-<argumentType:Arg1>-<leftSibling:null>-<rightSibling:VP>",
				"<nodeHead:i>-<consType:PRP>-<position:middle>-<parentPattern:NP->PRP>-<grandParentPattern:S->NP-VP>-<argumentType:Arg1>-<leftSibling:null>-<rightSibling:null>",
		};
				
		assertThat(strFeatures).containsExactly(goldFeatures);
	}
	
	@Test
	public void givenGoldLableWhenConstructingTheRelationThenItIsCorrect(){
		Function<ArgumentTreeNode, List<Annotation>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		Function<JCas, ? extends Collection<? extends ArgumentTreeNode>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends ArgumentTreeNode> sequences = sequenceExtractor.apply(aJCas);
		ArgumentTreeNode aSeqeunce = sequences.iterator().next();
		List<Annotation> instances = instanceExtractor.apply(aSeqeunce);
		
		SequenceClassifierConsumer<String, ArgumentTreeNode, Annotation> labeller = algorithmFactory.getLabeller(aJCas);
		DiscourseArgument arg1 = discourseRelation.getArguments(0);
		List<Token> tokens = JCasUtil.selectCovered(Token.class, aSeqeunce.getTreeNode());
		TokenListTools.initTokenList(arg1, tokens);
		discourseRelation.addToIndexes();
		
		List<String> goldLabels = new ArrayList<>();
		for (int i = 0; i < instances.size(); i++){
			if (i < 12 || i == 13)
				goldLabels.add("None");
			else
				goldLabels.add("Arg1");
		}
		labeller.accept(goldLabels, aSeqeunce, instances);
		
		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
		assertThat(relations).hasSize(1);
		DiscourseRelation relation = relations.iterator().next();
		
		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(example.getArg1());
		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(DiscourseRelationExample.toString(example.getArg2()));
		assertThat(TokenListTools.getTokenListText(relation.getDiscourseConnective())).isEqualTo(example.getDiscourseConnective());

	}
	
	
}
