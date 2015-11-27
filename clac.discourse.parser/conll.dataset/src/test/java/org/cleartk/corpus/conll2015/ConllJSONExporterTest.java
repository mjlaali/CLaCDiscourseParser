package org.cleartk.corpus.conll2015;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ConllJSONExporterTest {
	public static final String JSON_OUTPUT = "outputs/test/exporter.json";
	public static final String PERFECT_RESULT = "Precision 1.0000 Recall 1.0000 F1 1.0000";
	@SuppressWarnings("unused")
	private JCas jCas;

	public void setUp(ConllDatasetPath dataSet) throws UIMAException, IOException{
		new File(JSON_OUTPUT).getParentFile().mkdirs();

		// A collection reader that creates one CAS per file, containing the file's URI
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, new File(ConllJSON.TRIAL_RAW_TEXT_LD), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(dataSet.getParsesJSonFile());
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(dataSet.getDataJSonFile(), false);
		AnalysisEngineDescription conllJSONExporter = ConllJSONExporter.getDescription(JSON_OUTPUT);
//		AnalysisEngineDescription syntaxParseTreeReader = AnalysisEngineFactory.createEngineDescription(TreebankGoldAnnotator.class);
		
		for (JCas jCas : SimplePipeline.iteratePipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader, conllJSONExporter)) {
			this.jCas = jCas;
		}
	}
	
	@Test
	public void givenTrialDataSetWhenWritingTheSameOutputThenGetPerfectResults() throws IOException, UIMAException{
		ConllDatasetPath dataSet = new ConllDatasetPathFactory().makeADataset(ConllDatasetPath.DatasetMode.trial);
		setUp(dataSet);
		String overallResult = Tools.runScorer(ConllJSON.TRIAL_DISCOURSE_FILE, JSON_OUTPUT).getLast();
		
		assertThat(overallResult).isEqualTo(PERFECT_RESULT);
	}
	
	@Ignore
	@Test
	public void givenDevDataSetWhenWritingTheSameOutputThenGetPerfectResults() throws IOException, UIMAException{
		ConllDatasetPath dataSet = new ConllDatasetPathFactory().makeADataset(ConllDatasetPath.DatasetMode.dev);
		setUp(dataSet);
		String overallResult = Tools.runScorer(dataSet.getDataJSonFile().getAbsolutePath(), JSON_OUTPUT).getLast();
		
		assertThat(overallResult).isEqualTo(PERFECT_RESULT);
	}
}
