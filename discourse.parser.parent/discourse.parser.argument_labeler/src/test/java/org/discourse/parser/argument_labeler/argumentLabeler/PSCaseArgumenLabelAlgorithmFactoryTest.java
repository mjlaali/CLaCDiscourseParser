package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

public class PSCaseArgumenLabelAlgorithmFactoryTest {
	private ArgumentLabelerAlgorithmFactory algorithmFactory = new ArgumentLabelerAlgorithmFactory();
	private DiscourseRelatoinExample additionallyExample;
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	
	@Before
	public void setup() throws UIMAException{
		additionallyExample = new AdditionallyExample();
		aJCas = additionallyExample.getJCas();
		discourseRelation = additionallyExample.getRelation();
	}

	@Test
	public void whenExtractingInstancesThenTheyAreSix(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		List<String> nodes = instances.stream()
				.map(ArgumentInstance::getInstance)
				.map(getConstituentType()).collect(Collectors.toList());
		assertThat(nodes).containsExactly("ROOT", "RB", ",", "NP", "VP", ".");
	}
	
	@Test
	public void whenExtractingFeaturesThenTheyAreCorrect(){
		Function<DiscourseConnective, List<ArgumentInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<ArgumentInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());

		BiFunction<List<ArgumentInstance>, DiscourseConnective, List<List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
		List<List<Feature>> features = featureExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		List<String> strFeatures = features.stream()
			.map((fs) -> fs.stream().map(
					(f) -> "<" + f.getName() + ":" + f.getValue().toString() + ">")
					.collect(Collectors.joining("-")))
			.collect(Collectors.toList());
		
		assertThat(strFeatures.get(0)).isEqualTo("<CON-LStr:additionally>-<CON-POS:false>-<CON-NT-Position:true>-<ChildPat:S>-<NT-Ctx:ROOT-null-null-null>-<CON-NT-Path:>-<CON-NT-Path-Size:0>");
	}
}
