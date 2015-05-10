package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.junit.Ignore;
import org.junit.Test;

public class ConllJSONExporterTest {
	public static final String JSON_OUTPUT = "outputs/test/exporter.json";
	public static final String PERFECT_RESULT = "Precision 1.0 Recall 1.0 F1 1.0";
	@SuppressWarnings("unused")
	private JCas jCas;

	public void setUp(DatasetPath dataSet) throws UIMAException, IOException{
		new File(JSON_OUTPUT).getParentFile().mkdirs();
		Collection<File> files = FileUtils.listFiles(new File(dataSet.getRawTextsFld()), null, false);

		// A collection reader that creates one CAS per file, containing the file's URI
		CollectionReaderDescription reader = UriCollectionReader.getDescriptionFromFiles(files);

		AnalysisEngineDescription textReader = UriToDocumentTextAnnotator.getDescription();
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(dataSet.getSyntaxAnnotationFlie());
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(dataSet.getDiscourseGoldAnnotationFile(), false);
		AnalysisEngineDescription conllJSONExporter = ConllJSONExporter.getDescription(JSON_OUTPUT);
//		AnalysisEngineDescription syntaxParseTreeReader = AnalysisEngineFactory.createEngineDescription(TreebankGoldAnnotator.class);
		
		for (JCas jCas : SimplePipeline.iteratePipeline(reader, textReader, conllSyntaxJsonReader, conllDiscourseJsonReader, conllJSONExporter)) {
			this.jCas = jCas;
		}
	}
	
	@Test
	public void givenTrialDataSetWhenWritingTheSameOutputThenGetPerfectResults() throws IOException, UIMAException{
		DatasetPath dataSet = new ConllDataset();
		setUp(dataSet);
		String overallResult = Tools.runScorer(ConllJSON.TRIAL_DISCOURSE_FILE, JSON_OUTPUT).getLast();
		
		assertThat(overallResult).isEqualTo(PERFECT_RESULT);
	}
	
	@Ignore
	@Test
	public void givenDevDataSetWhenWritingTheSameOutputThenGetPerfectResults() throws IOException, UIMAException{
		DatasetPath dataSet = new ConllDataset("dev");
		setUp(dataSet);
		String overallResult = Tools.runScorer(dataSet.getDiscourseGoldAnnotationFile(), JSON_OUTPUT).getLast();
		
		assertThat(overallResult).isEqualTo(PERFECT_RESULT);
	}
}
