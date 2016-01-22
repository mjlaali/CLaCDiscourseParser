package org.discourse.parser.argument_labeler.argumentLabeler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
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
	
	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		return ArgumentLabelerAlgorithmFactory.getWriterDescription(outputDirectory.getAbsolutePath());
	}

	public static AnalysisEngineDescription getClassifierDescription(URL packageDir) throws ResourceInitializationException, MalformedURLException {
		URL modelUrl = new URL(packageDir, "model.jar");
		System.out.println(modelUrl.toString());
		return ArgumentLabelerAlgorithmFactory.getClassifierDescription(modelUrl.toString());
	}
	
	
	public static void main(String[] args) throws Exception {
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getDataJSonFile());

		File outputDirectory = new File(new File("outputs/resources"), PACKAGE_DIR);
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(outputDirectory)
				);

		 Train.main(outputDirectory);
	}

}
