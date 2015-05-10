package org.cleartk.discourse_parsing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllDataset;
import org.cleartk.corpus.conll2015.DatasetPath;
import org.cleartk.corpus.conll2015.Tools;
import org.cleartk.discourse_parsing.module.ParserComplement;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;
import org.junit.Ignore;
import org.junit.Test;

public class DiscourseParserComponentBaseTest {
	public static final String JSON_OUT_FILE = "outputs/test/perfect-pipe.json";

	protected CollectionReaderDescription reader;
	protected AggregateBuilder aggregateBuilder;
	
	public void setUpPipeline(DatasetPath dataSet) throws ResourceInitializationException{
		
		Collection<File> files = FileUtils.listFiles(new File(dataSet.getRawTextsFld()), null, false);

		AnalysisEngineDescription description;
		aggregateBuilder = new AggregateBuilder();

		// A collection reader that creates one CAS per file, containing the file's URI
		reader = UriCollectionReader.getDescriptionFromFiles(files);

		description = UriToDocumentTextAnnotator.getDescription();
		aggregateBuilder.add(description);
		description = ConllSyntaxGoldAnnotator.getDescription(dataSet.getSyntaxAnnotationFlie());
		aggregateBuilder.add(description);
	}
	
	public JCas run(DatasetPath dataSet, AnalysisEngineDescription description) throws ResourceInitializationException{
		if (aggregateBuilder == null)
			setUpPipeline(dataSet);
		if (description != null)
			aggregateBuilder.add(description);

		JCas res = null;
		for (JCas jCas : SimplePipeline.iteratePipeline(reader, aggregateBuilder.createAggregateDescription())) {
			res = jCas;
		}
		return res;
	}
	
	public JCas run(DatasetPath dataSet) throws ResourceInitializationException{
		return run(dataSet, null);
	}
	
	protected LinkedList<String> runAndComplete(DatasetPath dataSet)
			throws ResourceInitializationException, IOException {
		setUpPipeline(dataSet);
		aggregateBuilder.add(ParserComplement.getDescription(dataSet.getDiscourseGoldAnnotationFile(), false));
		aggregateBuilder.add(ConllJSONExporter.getDescription(JSON_OUT_FILE));
		run(dataSet);
		LinkedList<String> outputs = Tools.runScorer(dataSet.getDiscourseGoldAnnotationFile(), JSON_OUT_FILE);
		return outputs;
	}

	@Ignore
	@Test
	public void givenDevDataSetWhenNoImplicitRelationThenPromptThePerformance() throws ResourceInitializationException, IOException{
		LinkedList<String> outputs = runAndComplete(new ConllDataset("dev"));
		System.out.println(outputs.getLast());
	}
	
}
