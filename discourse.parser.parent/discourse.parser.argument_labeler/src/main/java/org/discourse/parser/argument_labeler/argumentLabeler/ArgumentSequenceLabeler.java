package org.discourse.parser.argument_labeler.argumentLabeler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.ConllDiscourseGoldAnnotator;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.ml.jar.Train;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ArgumentSequenceLabeler {
	public static final String PACKAGE_DIR = "argumentSequenceLabeler/";
	public static URL DEFAULT_URL = ClassLoader.getSystemClassLoader().getResource("clacParser/model/" + PACKAGE_DIR);
	
	public static final String SEQUENCE_TAGGER = "sequenceTagger";
	public static final String NONE_NODE_TAGGER = "noneNodeTagger";
	
	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		AggregateBuilder aggregateBuilder = new AggregateBuilder();
		boolean usingMallet;
		usingMallet = true;
		aggregateBuilder.add(ArgumentLabelerAlgorithmFactory.getWriterDescription(
				new File(outputDirectory, SEQUENCE_TAGGER).getAbsolutePath(), usingMallet));
		usingMallet = false;
		aggregateBuilder.add(NoneNodeLabeller.getWriterDescription(
				new File(outputDirectory, NONE_NODE_TAGGER).getAbsolutePath(), usingMallet));
		return aggregateBuilder.createAggregateDescription();
	}

	public static AnalysisEngineDescription getClassifierDescription() throws ResourceInitializationException, MalformedURLException {
		return getClassifierDescription(DEFAULT_URL);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(URL packageDir) throws ResourceInitializationException, MalformedURLException {
		URL sequenceTaggerModel = new URL(packageDir, SEQUENCE_TAGGER + "/model.jar");
		URL noneNodeTaggerModel = new URL(packageDir, NONE_NODE_TAGGER + "/model.jar");
		
		AggregateBuilder aggregateBuilder = new AggregateBuilder();
		aggregateBuilder.add(ArgumentLabelerAlgorithmFactory.getClassifierDescription(sequenceTaggerModel.toString()));
		aggregateBuilder.add(NoneNodeLabeller.getClassifierDescription(noneNodeTaggerModel.toString()));
		
		return aggregateBuilder.createAggregateDescription();
	}
	
	
	public static void main(String[] args) throws Exception {
		new File("outputs/patterns.txt").delete();
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());

		File outputDirectory = new File(new File("outputs/resources"), PACKAGE_DIR);
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(outputDirectory)
				);

		for (File aComponent: outputDirectory.listFiles()){
			System.out.println("ArgumentSequenceLabeler.main(): training " + aComponent.getName());
			Train.main(aComponent);
		}
	}

}
