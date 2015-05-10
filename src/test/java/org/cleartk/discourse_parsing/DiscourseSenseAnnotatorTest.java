package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse_parsing.module.DiscourseSenseAnnotator;
import org.junit.Test;

public class DiscourseSenseAnnotatorTest extends DiscourseArgumentLabelerTest{

	@Override
	public void setUpPipeline(DatasetPath dataSet)
			throws ResourceInitializationException {
		super.setUpPipeline(dataSet);
		aggregateBuilder.add(DiscourseSenseAnnotator.getDescription());
	}
	
	@Test
	public void giveTheTrialDatasetWhenRunningDiscourseSenseAnnotatorThenAllDiscourseSenseAreNotNull() throws ResourceInitializationException{
		JCas aJCas = run(new ConllDataset());
		
		Collection<DiscourseRelation> discourseRelations = JCasUtil.select(aJCas, DiscourseRelation.class);
		for (DiscourseRelation discourseRelation: discourseRelations){
			assertThat(discourseRelation.getDiscourseConnectiveText()).isNotEqualTo("that");
			assertThat(discourseRelation.getSense()).isNotNull();
		}
	}
}
