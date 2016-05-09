package org.discourse.parser.argument_labeler.argumentLabeler.implicit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.corpus.conll2015.DiscourseRelationExample;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseRelation;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class ImplicitArgumentLabelerTest {
	private ImplicitArgumentLabeler classifier = new ImplicitArgumentLabeler();
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private DiscourseRelationExample example = new ImplicitExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
	}
	
	@Test
	public void extractOneSequence(){
		Collection<? extends Sentence> sequences = classifier.getSequenceExtractor(aJCas).apply(aJCas);
		assertThat(sequences).hasSize(1);
	}
	

	@Test
	public void extractInstances(){
		Sentence sequence = classifier.getSequenceExtractor(aJCas).apply(aJCas).iterator().next();
		
		List<Annotation> instances = classifier.getInstanceExtractor(aJCas).apply(sequence);
		assertThat(instances.get(0).getCoveredText()).isEqualTo("\"");
		String lastSent = "\"We would stop index arbitrage when the market is under stress, and we have recently,\" he said, citing Oct. 13 and earlier this week.";
		assertThat(instances.get(instances.size() - 1).getCoveredText()).isEqualTo(lastSent);
		
		assertThat(instances).hasSize(97);
	}
	
	@Test
	public void getLabels(){
		discourseRelation.addToIndexesRecursively();
		
		Sentence sequence = classifier.getSequenceExtractor(aJCas).apply(aJCas).iterator().next();
		
		List<Annotation> instances = classifier.getInstanceExtractor(aJCas).apply(sequence);
		List<String> labels = classifier.getLabelExtractor(aJCas).apply(instances, sequence);
		
//		for (int i = 0; i < labels.size(); i++){
//			if (Boolean.valueOf(labels.get(i))){
//				System.out.println("" + i + ": " + instances.get(i).getCoveredText());
//			}
//		}
		System.out.println(labels);
	}

}
