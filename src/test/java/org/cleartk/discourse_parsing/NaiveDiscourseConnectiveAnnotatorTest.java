package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.TokenListTools;
import org.cleartk.corpus.conll2015.statistics.DiscourseConnectivesList;
import org.cleartk.discourse.type.DiscourseConnective;
import org.cleartk.discourse_parsing.module.DiscourseConnectiveAnnotator;
import org.junit.Test;

public class NaiveDiscourseConnectiveAnnotatorTest extends DiscourseParserComponentBaseTest{
	@Override
	public void setUpPipeline(DatasetPath dataSet)
			throws ResourceInitializationException {
		super.setUpPipeline(dataSet);
		aggregateBuilder.add(DiscourseConnectiveAnnotator.getClassifierDescription(DiscourseParser.getDcAnnotatorTrainDir(new ConllDataset("train").getModelDir())));
	}
	
	@Test
	public void giveTheTrialDatasetWhenRunningDiscourseConnectiveAnnotatorThenAllDiscourseConnectivesAreAnnotated() throws UIMAException{
		JCas aJCas = run(new ConllDataset());		
		int dcCnt = JCasUtil.select(aJCas, DiscourseConnective.class).size();
		assertThat(dcCnt).isGreaterThan(13);
		System.out
				.println("DiscourseConnectiveAnnotatorTest.giveTheTrialDatasetWhenRunningDiscourseConnectiveAnnotatorThenAllDiscourseConnectivesAreAnnotated(): " + dcCnt);
		
	}
	
	@Test
	public void giveTheTrialDatasetWhenRunningDiscourseConnectiveAnnotatorThenAllDiscourseConnectivesContainTokens() throws ResourceInitializationException{
		JCas aJCas = run(new ConllDataset());
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			assertThat(discourseConnective.getTokens().size()).isGreaterThan(0);
		}
	}
	
	@Test
	public void thenAllConnectiveTextAreListedInTheFile() throws ResourceInitializationException, IOException{
		Set<String> dcList = new TreeSet<String>(FileUtils.readLines(new File(DiscourseConnectivesList.DISCOURSE_CONNECTIVES_LIST_FILE)));
		JCas aJCas = run(new ConllDataset());
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			assertThat(dcList).contains(TokenListTools.getTokenListText(discourseConnective).toLowerCase());
		}
	}

	@Test
	public void thenThatIsNotAnnotated() throws ResourceInitializationException, IOException{
		JCas aJCas = run(new ConllDataset());
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			assertThat(TokenListTools.getTokenListText(discourseConnective).toLowerCase()).isNotEqualTo("that");
		}
	}
	
	@Test
	public void thenUpperCaseDiscourseConnectiveTagged() throws ResourceInitializationException, IOException{
		JCas aJCas = run(new ConllDataset());
		boolean butTagged = false;
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			butTagged = butTagged || TokenListTools.getTokenListText(discourseConnective).equals("But");
		}
		assertThat(butTagged).isTrue();
	}
	
	@Test
	public void thenEachConnectiveAnnotatedOnlyOnce() throws ResourceInitializationException{
		JCas aJCas = run(new ConllDataset());
		Set<String> annotatedConnective = new TreeSet<String>();
		
		for (DiscourseConnective discourseConnective: JCasUtil.select(aJCas, DiscourseConnective.class)){
			String key = TokenListTools.getTokenListText(discourseConnective) + "-" + discourseConnective.getBegin();
			assertThat(annotatedConnective).doesNotContain(key);
			annotatedConnective.add(key);
		}
	}
	

	public void givenTrainingModeWhenExtractingDCThenAllThemExtracted(){
		
	}

}
