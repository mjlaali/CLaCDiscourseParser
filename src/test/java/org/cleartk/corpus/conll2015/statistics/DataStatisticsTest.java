package org.cleartk.corpus.conll2015.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.discourse.type.DiscourseRelation;
import org.cleartk.syntax.constituent.type.TreebankNode;
import org.junit.Before;
import org.junit.Test;

public class DataStatisticsTest {
	File xmiDir = new File("outputs/test/temp-xmi");


	public static class Tester extends JCasAnnotator_ImplBase{

		@Override
		public void process(JCas aJCas) throws AnalysisEngineProcessException {
			try {
				JCas defView = aJCas.getView(CAS.NAME_DEFAULT_SOFA);
				JCas goldView = aJCas.getView(ConllDiscourseGoldAnnotator.GOLD_DISCOURSE_VIEW);
				
				assertThat(defView).isNotNull();
				assertThat(goldView).isNotNull();
				assertThat(defView.getDocumentText().length()).isGreaterThan(0);
				assertThat(defView.getDocumentText()).isEqualTo(goldView.getDocumentText());
				
				assertThat(JCasUtil.select(defView, TreebankNode.class).size()).isEqualTo(
						JCasUtil.select(goldView, TreebankNode.class).size());
				
				assertThat(JCasUtil.select(defView, DiscourseRelation.class)).isEmpty();
				assertThat(JCasUtil.select(goldView, DiscourseRelation.class).size()).isGreaterThan(0);
			} catch (CASException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	@Before
	public void setUp() throws IOException{
		if (xmiDir.exists())
			FileUtils.deleteDirectory(xmiDir);
		xmiDir.mkdirs();
	}
	
	@Test
	public void whenReadDataThenDefaultViewHasSyntaxInfoAndGoldViewHasBothSyntaxAndDiscourse() throws ResourceInitializationException, UIMAException, IOException{
		DatasetPath dataSet = new ConllDataset();
		DatasetStatistics datasetStatistics = new DatasetStatistics(dataSet, xmiDir.getAbsolutePath());
		datasetStatistics.readDataset();
		datasetStatistics.getStatistics(AnalysisEngineFactory.createEngineDescription(Tester.class));
	}
}
