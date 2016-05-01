package org.discourse.parser.implicit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllJSonGoldExporter;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.discourse.parser.implicit.NoRelationAnnotator;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class NoRelationAnnotatorTest {
	static final File JSON_OUTPUT = new File("outputs/export.json");

	@Test
	public void whenGeneratingAFileForTrialDatasetThenNoRelationsAreIncludedInTheOutput() throws UIMAException, IOException{
		File dataFld = new File("../discourse.conll.dataset/data/");
		ConllDatasetPath datasetPath = new ConllDatasetPathFactory().makeADataset2016(dataFld, DatasetMode.trial);
		
		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, datasetPath.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		
		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(datasetPath.getParsesJSonFile());
		AnalysisEngineDescription conllDiscourseJsonReader = ConllDiscourseGoldAnnotator.getDescription(datasetPath.getRelationsJSonFile(), false);
		AnalysisEngineDescription noRelationAnnotator = NoRelationAnnotator.getDescription();
		AnalysisEngineDescription conllGoldJSONExporter = ConllJSonGoldExporter.getDescription(JSON_OUTPUT);
		
		SimplePipeline.runPipeline(reader, conllSyntaxJsonReader, conllDiscourseJsonReader, noRelationAnnotator, conllGoldJSONExporter);
		
		System.out.println(FileUtils.readFileToString(JSON_OUTPUT));
	}
}
