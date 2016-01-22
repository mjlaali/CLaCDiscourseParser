package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;

public class ArgumentLabelAlgorithmFactoryTest extends KongExampleTest{
	private ArgumentLabelerAlgorithmFactory algorithmFactory = new ArgumentLabelerAlgorithmFactory();
	
	@Test
	public void whenExtractingSequencesThenOnlyOneSequenceIsExtracted(){
		discourseRelation.addToIndexes();
		Function<JCas, ? extends Collection<? extends DiscourseConnective>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends DiscourseConnective> sequences = sequenceExtractor.apply(aJCas);
		assertThat(sequences).hasSize(1);
		DiscourseConnective dc = sequences.iterator().next();
		assertThat(dc).isEqualTo(discourseRelation.getDiscourseConnective());
	}
	
	@Test
	public void givenKongExampleWhenArgumentInstancesAreExtractedThreAreFiveArgumentInstances(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		List<String> nodes = instances.stream()
				.map(ArgumentInstance::getInstance)
				.map(getConstituentType()).collect(Collectors.toList());
		assertThat(nodes).containsExactly("CC", "NP", "VP", "CC", "RB", "VP");
	}
	
	@Test
	public void givenKongExampleWhenLabelingInstancesThenTheyAre1112dc2(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		BiFunction<List<ArgumentInstance>, DiscourseConnective, List<String>> labelExtractor = 
				algorithmFactory.getLabelExtractor(aJCas);
		List<String> labels = labelExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		assertThat(labels).containsExactly("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
	}
	
	@Test
	public void givenKongExampleWhenExtractingFeaturesThenTheyAreCorrect(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());

		BiFunction<List<ArgumentInstance>, DiscourseConnective, List<List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
		List<List<Feature>> features = featureExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		List<String> strFeatures = features.stream()
			.map((fs) -> fs.stream().map(
					(f) -> "<" + f.getName() + ":" + f.getValue().toString() + ">")
					.collect(Collectors.joining("-")))
			.collect(Collectors.toList());
		
		String[] goldFeatures = new String[]{
				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:>-<NT-Ctx:CC-S-null-NP>-<CON-NT-Path:VP-S-null-CC>-<CON-NT-Path-Size:4>",
				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:PRP-NNS>-<NT-Ctx:NP-S-CC-VP>-<CON-NT-Path:VP-S-null-NP>-<CON-NT-Path-Size:4>",
				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:VBP-NP>-<NT-Ctx:VP-VP-null-CC>-<CON-NT-Path:VP-null-VP>-<CON-NT-Path-Size:3>",
				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:true>-<ChildPat:>-<NT-Ctx:CC-VP-VP-RB>-<CON-NT-Path:VP-null-CC>-<CON-NT-Path-Size:3>",
				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:false>-<ChildPat:>-<NT-Ctx:RB-VP-CC-VP>-<CON-NT-Path:VP-null-RB>-<CON-NT-Path-Size:3>",
				"<CON-LStr:so>-<CON-POS:true>-<CON-NT-Position:false>-<ChildPat:VBP-VP>-<NT-Ctx:VP-VP-RB-null>-<CON-NT-Path:VP-null-VP>-<CON-NT-Path-Size:3>"
		};
				
		assertThat(strFeatures).containsExactly(goldFeatures);
	}
	
	@Test
	public void givenGoldLableWhenConstructingTheRelationThenItIsCorrect(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		List<String> goldLabels = Arrays.asList("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
		
		SequenceClassifierConsumer<String, DiscourseConnective, ArgumentInstance> labeller = algorithmFactory.getLabeller(aJCas);
		
		labeller.accept(goldLabels, discourseRelation.getDiscourseConnective(), instances);
		
		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
		assertThat(relations).hasSize(1);
		DiscourseRelation relation = relations.iterator().next();
		
		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(arg1);
		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(arg2);
		assertThat(TokenListTools.getTokenListText(relation.getDiscourseConnective())).isEqualTo(dc);

	}
}
