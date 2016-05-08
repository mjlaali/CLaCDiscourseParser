package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.ml.Feature;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class ComponentPipelineTest2 {
	private Arg1Classifier arg1Classifier = new Arg1Classifier();
	private Arg2Classifier arg2Classifier = new Arg2Classifier();
	private ConflictResolver conflictResolver = new ConflictResolver();
	private NodeRemover nodeRemover = new NodeRemover();
	private JCas goldView;
	private JCas testView;
	private DiscourseRelation discourseRelation;
	private DiscourseRelationExample example = new TokenConfilictExample(); 
	
	@Before
	public void setup() throws UIMAException{
		goldView = JCasFactory.createJCas();
		testView = JCasFactory.createJCas();
		new DiscourseRelationFactory().makeDiscourseRelationFrom(goldView, example).addToIndexesRecursively();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(testView, example);
		discourseRelation.getDiscourseConnective().addToIndexes();
		discourseRelation.getDiscourseConnective().setDiscourseRelation(null);
	}

	@Test
	public void testArg2Classifier(){
		JCas view = goldView;
		DiscourseConnective dc = null;
		Collection<? extends DiscourseConnective> goldSequence = arg2Classifier.getSequenceExtractor(view).apply(view);
		dc = goldSequence.iterator().next();
		List<Constituent> goldInstances = arg2Classifier.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> goldFeatures = arg2Classifier.getFeatureExtractor(view).apply(goldInstances, dc);
		List<String> goldLabels = arg2Classifier.getLabelExtractor(view).apply(goldInstances, dc);
		
		view = testView;
		Collection<? extends DiscourseConnective> testSequence = arg2Classifier.getSequenceExtractor(view).apply(view);
		dc = testSequence.iterator().next();
		List<Constituent> testInstances = arg2Classifier.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> testFeatures = arg2Classifier.getFeatureExtractor(view).apply(testInstances, dc);
		arg2Classifier.getLabeller(view).accept(goldLabels, dc, testInstances);
		
		assertThat(goldSequence).hasSize(1);
		assertThat(testSequence).hasSize(goldSequence.size());
		
		assertThat(testInstances).hasSize(goldInstances.size());

		assertThat(testFeatures).isEqualTo(goldFeatures);
		DiscourseRelation relation = JCasUtil.select(testView, DiscourseRelation.class).iterator().next();
		assertThat(relation.getArguments(1).getCoveredText()).isEqualTo(example.getArg2()[0]);
	}
	
	@Test
	public void testArg1Classifier(){
		testArg2Classifier();
		
		JCas view = goldView;
		DiscourseConnective dc = null;
		Collection<? extends DiscourseConnective> goldSequence = arg1Classifier.getSequenceExtractor(view).apply(view);
		dc = goldSequence.iterator().next();
		List<Constituent> goldInstances = arg1Classifier.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> goldFeatures = arg1Classifier.getFeatureExtractor(view).apply(goldInstances, dc);
		List<String> goldLabels = arg1Classifier.getLabelExtractor(view).apply(goldInstances, dc);
		
		view = testView;
		Collection<? extends DiscourseConnective> testSequence = arg1Classifier.getSequenceExtractor(view).apply(view);
		dc = testSequence.iterator().next();
		List<Constituent> testInstances = arg1Classifier.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> testFeatures = arg1Classifier.getFeatureExtractor(view).apply(testInstances, dc);
		arg1Classifier.getLabeller(view).accept(goldLabels, dc, testInstances);
		
		assertThat(goldSequence).hasSize(1);
		assertThat(testSequence).hasSize(goldSequence.size());
		
		assertThat(testInstances).hasSize(goldInstances.size());

		assertThat(testFeatures).isEqualTo(goldFeatures);
		DiscourseRelation relation = JCasUtil.select(testView, DiscourseRelation.class).iterator().next();
		assertThat(relation.getArguments(0).getCoveredText()).isEqualTo("We would have to wait until we have collected on those assets before we can move forward");
	}
	
	@Test
	public void testConflictResolver(){
		testArg1Classifier();
		
		JCas view = goldView;
		DiscourseConnective dc = null;
		Collection<? extends DiscourseConnective> goldSequence = conflictResolver.getSequenceExtractor(view).apply(view);
		dc = goldSequence.iterator().next();
		List<Constituent> goldInstances = conflictResolver.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> goldFeatures = conflictResolver.getFeatureExtractor(view).apply(goldInstances, dc);
		List<String> goldLabels = conflictResolver.getLabelExtractor(view).apply(goldInstances, dc);
		assertThat(goldSequence).hasSize(1);
		
		view = testView;
		Collection<? extends DiscourseConnective> testSequence = conflictResolver.getSequenceExtractor(view).apply(view);
		dc = testSequence.iterator().next();
		assertThat(testSequence).hasSize(goldSequence.size());
		
		List<Constituent> testInstances = conflictResolver.getInstanceExtractor(view).apply(dc);
		assertThat(testInstances).hasSize(goldInstances.size());
		
		List<List<Feature>> testFeatures = conflictResolver.getFeatureExtractor(view).apply(testInstances, dc);
		conflictResolver.getLabeller(view).accept(goldLabels, dc, testInstances);
		
		

		assertThat(testFeatures).isEqualTo(goldFeatures);
		DiscourseRelation relation = JCasUtil.select(testView, DiscourseRelation.class).iterator().next();
		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(example.getArg1() + " before we can move forward");
		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(example.getArg2()[0]);
	}
	
	@Test
	public void testNodeRemover(){
		testConflictResolver();
		
		JCas view = goldView;
		DiscourseConnective dc = null;
		Collection<? extends DiscourseConnective> goldSequence = nodeRemover.getSequenceExtractor(view).apply(view);
		dc = goldSequence.iterator().next();
		List<Annotation> goldInstances = nodeRemover.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> goldFeatures = nodeRemover.getFeatureExtractor(view).apply(goldInstances, dc);
		List<String> goldLabels = nodeRemover.getLabelExtractor(view).apply(goldInstances, dc);
		
		view = testView;
		Collection<? extends DiscourseConnective> testSequence = nodeRemover.getSequenceExtractor(view).apply(view);
		dc = testSequence.iterator().next();
		List<Annotation> testInstances = nodeRemover.getInstanceExtractor(view).apply(dc);
		List<List<Feature>> testFeatures = nodeRemover.getFeatureExtractor(view).apply(testInstances, dc);
		nodeRemover.getLabeller(view).accept(goldLabels, dc, testInstances);
		
		assertThat(goldSequence).hasSize(1);
		assertThat(testSequence).hasSize(goldSequence.size());
		
		assertThat(testInstances).hasSize(goldInstances.size());

		assertThat(testFeatures).isEqualTo(goldFeatures);
		DiscourseRelation relation = JCasUtil.select(testView, DiscourseRelation.class).iterator().next();
		assertThat(TokenListTools.getTokenListText(relation.getArguments(0))).isEqualTo(example.getArg1());
		assertThat(TokenListTools.getTokenListText(relation.getArguments(1))).isEqualTo(example.getArg2()[0]);

	}
}
