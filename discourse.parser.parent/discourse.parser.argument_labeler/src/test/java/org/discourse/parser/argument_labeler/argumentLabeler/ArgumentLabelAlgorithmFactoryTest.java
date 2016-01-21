package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.ml.Feature;
import org.junit.Test;

public class ArgumentLabelAlgorithmFactoryTest extends KongExampleTest{
	private ArgumentLabelerAlgorithmFactory algorithmFactory = new ArgumentLabelerAlgorithmFactory();
	
	@Test
	public void givenKongExampleWhenArgumentInstancesAreExtractedThreAreFiveArgumentInstances(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		List<String> nodes = instances.stream()
				.map(ArgumentInstance::getInstance)
				.map(getConstituentType()).collect(Collectors.toList());
		System.out.println(nodes.stream().collect(Collectors.joining("-")));
		assertThat(nodes).containsExactly("CC", "NP", "VP", "CC", "RB", "VP");
	}
	
	@Test
	public void givenKongExampleWhenLabelingInstancesThenTheyAre1112dc2(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		BiFunction<List<ArgumentInstance>, DiscourseConnective, List<String>> labelExtractor = algorithmFactory.getLabelExtractor(aJCas);
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
				"<CON-LStr:so>-<CON-POS:true>-<ChildPat:>-<NT-Ctx:CC-S-null-NP>-<CON-NT-Path:VP-S-null-CC>",
				"<CON-LStr:so>-<CON-POS:true>-<ChildPat:PRP-NNS>-<NT-Ctx:NP-S-CC-VP>-<CON-NT-Path:VP-S-null-NP>",
				"<CON-LStr:so>-<CON-POS:true>-<ChildPat:VBP-NP>-<NT-Ctx:VP-VP-null-CC>-<CON-NT-Path:VP-null-VP>",
				"<CON-LStr:so>-<CON-POS:true>-<ChildPat:>-<NT-Ctx:CC-VP-VP-RB>-<CON-NT-Path:VP-null-CC>",
				"<CON-LStr:so>-<CON-POS:true>-<ChildPat:>-<NT-Ctx:RB-VP-CC-VP>-<CON-NT-Path:VP-null-RB>",
				"<CON-LStr:so>-<CON-POS:true>-<ChildPat:VBP-VP>-<NT-Ctx:VP-VP-RB-null>-<CON-NT-Path:VP-null-VP>"
		};
				
		assertThat(strFeatures).containsExactly(goldFeatures);
	}
}
