package org.discourse.parser.argument_labeler.argumentLabeler.components;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.cleartk.corpus.conll2015.DiscourseRelationFactory;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.discourse.parser.argument_labeler.argumentLabeler.KongExample;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class Arg2ClassifierTest {
	private Arg2Classifier classifier = new Arg2Classifier();
	private JCas aJCas;
	private DiscourseRelation discourseRelation;
	private KongExample example = new KongExample(); 
	
	@Before
	public void setup() throws UIMAException{
		aJCas = JCasFactory.createJCas();
		discourseRelation = new DiscourseRelationFactory().makeDiscourseRelationFrom(aJCas, example);
	}
	
	@Test
	public void createsOneSequence(){
		discourseRelation.getDiscourseConnective().addToIndexes();
		
		Collection<? extends DiscourseConnective> sequences = classifier.getSequenceExtractor(aJCas).apply(aJCas);
		assertThat(sequences).hasSize(1);
	}
	
	@Test
	public void createsTenInstancesAtTheTrain(){
		discourseRelation.getDiscourseConnective().addToIndexes();
		List<Constituent> instances = classifier.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		
		assertThat(instances).hasSize(11);
	}

	@Test
	public void onlyOneLabelIsTrueAndEqualToVP(){
		discourseRelation.addToIndexesRecursively();
		List<Constituent> instances = classifier.getInstanceExtractor(aJCas).apply(discourseRelation.getDiscourseConnective());
		List<String> labels = classifier.getLabelExtractor(aJCas).apply(instances, discourseRelation.getDiscourseConnective());
		
		boolean foundTrueLabel = false;
		
		for (int i = 0; i < labels.size(); i++){
			if (Boolean.valueOf(labels.get(i))){
				assertThat(foundTrueLabel).isFalse();
				foundTrueLabel = true;
				
				String nodeCoveredText = instances.get(i).getCoveredText();
				String expected = "have much broader business interests and so are better cushioned against price swings";
				assertThat(nodeCoveredText).isEqualTo(expected);
			}
		}
		
		assertThat(foundTrueLabel).isTrue();
	}
	
}
