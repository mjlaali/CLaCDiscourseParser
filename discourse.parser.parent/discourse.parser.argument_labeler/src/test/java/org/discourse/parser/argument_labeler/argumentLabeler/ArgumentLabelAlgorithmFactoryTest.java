package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler.ArgumentSequenceClassifier;
import org.discourse.parser.argument_labeler.argumentLabeler.sequenceLabeler.copy.DCTreeNodeArgInstance;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;

public class ArgumentLabelAlgorithmFactoryTest{
	private ArgumentSequenceClassifier algorithmFactory = new ArgumentSequenceClassifier();
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private KongExample example = new KongExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
	}
	
	@Test
	public void whenExtractingSequencesThenOnlyOneSequenceIsExtracted(){
		discourseRelation.addToIndexesRecursively();
		Function<JCas, ? extends Collection<? extends DiscourseConnective>> sequenceExtractor = algorithmFactory.getSequenceExtractor(aJCas);
		Collection<? extends DiscourseConnective> sequences = sequenceExtractor.apply(aJCas);
		assertThat(sequences).hasSize(1);
		DiscourseConnective dc = sequences.iterator().next();
		assertThat(dc).isEqualTo(discourseRelation.getDiscourseConnective());
	}
	
	@Test
	public void givenKongExampleWhenArgumentInstancesAreExtractedThreAreFiveArgumentInstances(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		List<String> nodes = instances.stream()
				.map(DCTreeNodeArgInstance::getNode)
				.map(getConstituentType()).collect(Collectors.toList());
		assertThat(nodes).containsExactly("CC", "NP", "VP", "CC", "RB", "VP");
	}
	
	@Test
	public void givenKongExampleWhenLabelingInstancesThenTheyAre1112dc2(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<String>> labelExtractor = 
				algorithmFactory.getLabelExtractor(aJCas);
		List<String> labels = labelExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		assertThat(labels).containsExactly("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
	}
	
	@Test
	public void givenKongExampleWhenExtractingFeaturesThenTheyAreCorrect(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());

		BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
		List<List<Feature>> features = featureExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		List<String> strFeatures = features.stream()
			.map((fs) -> fs.stream().map(
					(f) -> "<" + f.getName() + ":" + f.getValue().toString() + ">")
					.collect(Collectors.joining("-")))
			.collect(Collectors.toList());
		
		String[] goldFeatures = new String[]{
				"<CON-LStr:so>-<CON-POS:true>-<leftPOS:CC>-<leftText:and>-<rightPOS:VBP>-<rightText:are>-<CON-NT-Position:true>-<ChildPat:>-<NT-Ctx:CC-S-null-NP>-<CON-NT-Path:VP-S-null-CC>-<CON-NT-Path-Size:4>-<firstToken:but>-<lastToken:but>-<tokenBeforeFirst:null>-<tokenAfterLast:its>-<mainVerb:null>",
				"<CON-LStr:so>-<CON-POS:true>-<leftPOS:CC>-<leftText:and>-<rightPOS:VBP>-<rightText:are>-<CON-NT-Position:true>-<ChildPat:PRP-NNS>-<NT-Ctx:NP-S-CC-VP>-<CON-NT-Path:VP-S-null-NP>-<CON-NT-Path-Size:4>-<firstToken:its>-<lastToken:competitors>-<tokenBeforeFirst:but>-<tokenAfterLast:have>-<mainVerb:null>",
				"<CON-LStr:so>-<CON-POS:true>-<leftPOS:CC>-<leftText:and>-<rightPOS:VBP>-<rightText:are>-<CON-NT-Position:true>-<ChildPat:VBP-NP>-<NT-Ctx:VP-VP-null-CC>-<CON-NT-Path:VP-null-VP>-<CON-NT-Path-Size:3>-<firstToken:have>-<lastToken:interests>-<tokenBeforeFirst:competitors>-<tokenAfterLast:and>-<mainVerb:have>",
				"<CON-LStr:so>-<CON-POS:true>-<leftPOS:CC>-<leftText:and>-<rightPOS:VBP>-<rightText:are>-<CON-NT-Position:true>-<ChildPat:>-<NT-Ctx:CC-VP-VP-RB>-<CON-NT-Path:VP-null-CC>-<CON-NT-Path-Size:3>-<firstToken:and>-<lastToken:and>-<tokenBeforeFirst:interests>-<tokenAfterLast:so>-<mainVerb:null>",
				"<CON-LStr:so>-<CON-POS:true>-<leftPOS:CC>-<leftText:and>-<rightPOS:VBP>-<rightText:are>-<CON-NT-Position:false>-<ChildPat:>-<NT-Ctx:RB-VP-CC-VP>-<CON-NT-Path:VP-null-RB>-<CON-NT-Path-Size:3>-<firstToken:so>-<lastToken:so>-<tokenBeforeFirst:and>-<tokenAfterLast:are>-<mainVerb:null>",
				"<CON-LStr:so>-<CON-POS:true>-<leftPOS:CC>-<leftText:and>-<rightPOS:VBP>-<rightText:are>-<CON-NT-Position:false>-<ChildPat:VBP-VP>-<NT-Ctx:VP-VP-RB-null>-<CON-NT-Path:VP-null-VP>-<CON-NT-Path-Size:3>-<firstToken:are>-<lastToken:swings>-<tokenBeforeFirst:so>-<tokenAfterLast:null>-<mainVerb:cushioned>"
		};
				
		assertThat(strFeatures).containsExactly(goldFeatures);
	}
	
	@Test
	public void givenGoldLableWhenConstructingTheRelationThenItIsCorrect(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		List<String> goldLabels = Arrays.asList("Arg1", "Arg1", "Arg1", "Arg2", "DC", "Arg2");
		
		SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance> labeller = algorithmFactory.getLabeller(aJCas);
		
		labeller.accept(goldLabels, discourseRelation.getDiscourseConnective(), instances);
		
		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
		assertThat(relations).hasSize(1);
		DiscourseRelation relation = relations.iterator().next();
		
		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(example.getArg1());
		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(DiscourseRelationExample.toString(example.getArg2()));
		assertThat(TokenListTools.getTokenListText(relation.getDiscourseConnective())).isEqualTo(example.getDiscourseConnective());

	}
}
