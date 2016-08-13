package org.discourse.parser.argument_labeler.argumentLabeler;

import static ca.concordia.clac.ml.feature.TreeFeatureExtractor.getConstituentType;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

import ca.concordia.clac.ml.classifier.SequenceClassifierConsumer;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class PSCaseArgumenLabelAlgorithmFactoryTest {
	private ArgumentLabelerAlgorithmFactory algorithmFactory = new ArgumentLabelerAlgorithmFactory();
	private DiscourseRelationExample additionallyExample;
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	
	@Before
	public void setup() throws UIMAException{
		additionallyExample = new AdditionallyExample();
		DiscourseRelationFactory factory = new DiscourseRelationFactory();
		aJCas = JCasFactory.createJCas();
		
		discourseRelation = factory.makeDiscourseRelationFrom(aJCas, additionallyExample);
//		discourseRelation.addToIndexes();
	}

	@Test
	public void whenExtractingInstancesThenTheyAreSix(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		List<String> nodes = instances.stream()
				.map(DCTreeNodeArgInstance::getNode)
				.map(getConstituentType()).collect(Collectors.toList());
		assertThat(nodes).containsExactly("ROOT", "RB", ",", "NP", "VP", ".");
		Collection<Sentence> sentences = JCasUtil.select(aJCas, Sentence.class);
		assertThat(instances.get(0).getNode().getCoveredText()).isEqualTo(sentences.iterator().next().getCoveredText());
	}
	
	@Test
	public void givenKongExampleWhenLabelingInstancesThenTheyAre1112dc2(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		
		BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<String>> labelExtractor = 
				algorithmFactory.getLabelExtractor(aJCas);
		List<String> labels = labelExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		assertThat(labels).containsExactly("Arg1", "DC", "Arg2", "Arg2", "Arg2", "Arg2");
	}
	
	@Test
	public void whenExtractingFeaturesThenTheyAreCorrect(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());

		BiFunction<List<DCTreeNodeArgInstance>, DiscourseConnective, List<List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
		List<List<Feature>> features = featureExtractor.apply(instances, discourseRelation.getDiscourseConnective());
		
		List<String> strFeatures = features.stream()
			.map((fs) -> fs.stream().map(
					(f) -> "<" + f.getName() + ":" + f.getValue().toString() + ">")
					.collect(Collectors.joining("-")))
			.collect(Collectors.toList());
		
		assertThat(strFeatures.get(0)).isEqualTo(
				"<CON-LStr:additionally>-<CON-POS:false>-<leftPOS:.>-<leftText:.>-<rightPOS:,>-<rightText:,>"
				+ "-<CON-NT-Position:true>-<ChildPat:S>-<NT-Ctx:ROOT-null-null-null>-<CON-NT-Path:>-<CON-NT-Path-Size:0>"
				+ "-<firstToken:a>-<lastToken:.>-<tokenBeforeFirst:null>-<tokenAfterLast:additionally>-<mainVerb:is>"
				);
	}
	
	@Test
	public void givenGoldLableWhenConstructingTheRelationThenItIsCorrect(){
		Function<DiscourseConnective, List<DCTreeNodeArgInstance>> instanceExtractor = algorithmFactory.getInstanceExtractor(aJCas);
		List<DCTreeNodeArgInstance> instances = instanceExtractor.apply(discourseRelation.getDiscourseConnective());
		List<String> goldLabels = Arrays.asList("Arg1", "DC", "Arg2", "Arg2", "Arg2", "Arg2");
		
		SequenceClassifierConsumer<String, DiscourseConnective, DCTreeNodeArgInstance> labeller = algorithmFactory.getLabeller(aJCas);
		
		labeller.accept(goldLabels, discourseRelation.getDiscourseConnective(), instances);
		
		Collection<DiscourseRelation> relations = JCasUtil.select(aJCas, DiscourseRelation.class);
		assertThat(relations).hasSize(1);
		DiscourseRelation relation = relations.iterator().next();
		
		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(additionallyExample.getArg1());
		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(DiscourseRelationExample.toString(additionallyExample.getArg2()));
		assertThat(TokenListTools.getTokenListText(relation.getDiscourseConnective())).isEqualTo(additionallyExample.getDiscourseConnective());

	}
	
	@Test
	public void runRealTest() throws ResourceInitializationException, MalformedURLException, AnalysisEngineProcessException{
		AnalysisEngineDescription argumentLabeler = ArgumentSequenceLabeler.getClassifierDescription();
		discourseRelation.getDiscourseConnective().addToIndexes();
		SimplePipeline.runPipeline(aJCas, argumentLabeler);
	}
}
