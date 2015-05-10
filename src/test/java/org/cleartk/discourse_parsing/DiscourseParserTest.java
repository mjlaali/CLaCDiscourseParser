package org.cleartk.discourse_parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.junit.Before;
import org.junit.Test;

public class DiscourseParserTest {
	public static final String JSON_OUTPUT = "outputs/parser/system_%s.json";
	public static final String PERFECT_RESULT = "Precision 1.0 Recall 1.0 F1 1.0";
	public static final String ZERO_RESULT = "Precision 0.0 Recall 0.0 F1 0.0";

	@Before
	public void setUp(){
		new File(JSON_OUTPUT).getParentFile().mkdirs();
	}
	
	@Test
	public void whenRunningDiscourseParserOnTrialDataSetThePrecisionIsNietherZeroNorPerfect() throws UIMAException, IOException{
		DatasetPath dataSet = new ConllDataset();

		String systemOutputFile = runParserOnDataset(dataSet);
		String overallResult = Tools.runScorer(dataSet.getDiscourseGoldAnnotationFile(), systemOutputFile).getLast();
		assertThat(overallResult).contains("Precision");
		assertThat(overallResult).doesNotContain("Precision 0.0 ");
		assertThat(overallResult).isNotEqualTo(PERFECT_RESULT);
		System.out
				.println("DiscourseParserTest.whenRunningDiscourseParserOnTrialDataSetThePrecisionIsNietherZeroNorPerfect(): " + overallResult);

	}
	
	@Test
	public void whenRunningDiscourseParserOnDevDataSetThenPromptTheOutput() throws ResourceInitializationException, UIMAException, IOException{
		DatasetPath dataSet = new ConllDataset("dev");
		String systemOutputFile = runParserOnDataset(dataSet);
		String overallResult = Tools.runScorer(dataSet.getDiscourseGoldAnnotationFile(), systemOutputFile).getLast();
		System.out
				.println("DiscourseParserTest.whenRunningDiscourseParserOnDevDataSetThenPromptTheOutput(): " + overallResult);
	}

	private String runParserOnDataset(DatasetPath dataSet)
			throws ResourceInitializationException, UIMAException, IOException {
		Collection<File> files = FileUtils.listFiles(new File(dataSet.getRawTextsFld()), null, false);

		AnalysisEngineDescription discoursParser = new DiscourseParser(DiscourseParser.DEFAULT_OUT_DIR).getDescription();
		CollectionReaderDescription reader = UriCollectionReader.getDescriptionFromFiles(files);
		String systemOutputFile = String.format(JSON_OUTPUT, dataSet.getMode());
		
		AggregateBuilder builder = createParserPipeline(dataSet, systemOutputFile, discoursParser);

		SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
		return systemOutputFile;
	}

	public static AggregateBuilder createParserPipeline(DatasetPath dataSet,
			String systemOutputFile, AnalysisEngineDescription... discoursParserComponents)
			throws ResourceInitializationException {
		AggregateBuilder builder = new AggregateBuilder();
		// A collection reader that creates one CAS per file, containing the file's URI
		AnalysisEngineDescription textReader = UriToDocumentTextAnnotator.getDescription();
		builder.add(textReader);
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(dataSet.getSyntaxAnnotationFlie());
		builder.add(conllSyntaxJsonReader);
		for (AnalysisEngineDescription discoursParserComponent: discoursParserComponents)
			builder.add(discoursParserComponent);
		AnalysisEngineDescription conllJSONExporter = ConllJSONExporter.getDescription(systemOutputFile);
		builder.add(conllJSONExporter);
		return builder;
	}
}
