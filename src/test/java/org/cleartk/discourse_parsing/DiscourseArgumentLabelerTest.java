package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.RelationType;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.discourse_parsing.module.argumentLabeler.DiscourseArgumentLabeler;
import org.junit.Ignore;
import org.junit.Test;

public class DiscourseArgumentLabelerTest extends NaiveDiscourseConnectiveAnnotatorTest{
	@Override
	public void setUpPipeline(DatasetPath dataSet)
			throws ResourceInitializationException {
		super.setUpPipeline(dataSet);
		aggregateBuilder.add(DiscourseArgumentLabeler.getDescription());
	}

	@Test
	public void giveTheTrialDatasetWhenRunningDiscourseArgumentLabelerThenDiscourseRelationIsAnnotated() throws UIMAException{
		JCas aJCas = run(new ConllDataset());
		assertThat(JCasUtil.select(aJCas, DiscourseRelation.class)).hasSize(JCasUtil.select(aJCas, DiscourseConnective.class).size());
	}
	
	@Test
	public void giveTheTrialDatasetWhenRunningDiscourseArgumentLabelerThenAllExplicitRelationsHaveDCAndDCText() throws ResourceInitializationException{
		JCas aJCas = run(new ConllDataset());
		for (DiscourseRelation discourseRelation: JCasUtil.select(aJCas, DiscourseRelation.class)){
			if (discourseRelation.getRelationType().equals(RelationType.Explicit.toString())){
				assertThat(discourseRelation.getDiscourseConnective()).isNotNull();
				assertThat(discourseRelation.getDiscourseConnectiveText()).isNotNull();
				assertThat(discourseRelation.getDiscourseConnectiveText()).isNotEmpty();
				assertThat(discourseRelation.getDiscourseConnectiveText()).isNotEqualTo("that");
			}
		}
	}
	
	@Ignore
	@Test
	public void givenPerfectPipelineWhenAddingDiscourseArgumentsThenPromptThePerformance() throws ResourceInitializationException, IOException{
		DatasetPath dataSet = new ConllDataset();
		LinkedList<String> outputs = runAndComplete(dataSet);
		for (String line: outputs)
			System.out.println(line);
//		System.out
//				.println("DiscourseConnectiveAnnotatorTest.givenPerfectPipelineWhenAddingDiscourseArgumentsThenPromptThePerformance()" +
//						outputs.getLast());
	}

}
