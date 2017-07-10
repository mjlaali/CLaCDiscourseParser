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

import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.Option;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;

public class ArgumentSequenceLabeler {
	public static final String PACKAGE_DIR = "argumentSequenceLabeler/";
	public static URL DEFAULT_URL = ClassLoader.getSystemClassLoader().getResource("clacParser/model/" + PACKAGE_DIR);
	
	public static final String SEQUENCE_TAGGER = "sequenceTagger";
	public static final String NONE_NODE_TAGGER = "noneNodeTagger";
	
	public static AnalysisEngineDescription getWriterDescription(File outputDirectory, boolean malletForArgs, boolean malletForNonNode) throws ResourceInitializationException{
		AggregateBuilder aggregateBuilder = new AggregateBuilder();
		
		aggregateBuilder.add(ArgumentLabelerAlgorithmFactory.getWriterDescription(
				new File(outputDirectory, SEQUENCE_TAGGER).getAbsolutePath(), malletForArgs));

		aggregateBuilder.add(NoneNodeLabeller.getWriterDescription(
				new File(outputDirectory, NONE_NODE_TAGGER).getAbsolutePath(), malletForNonNode));
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
	
	
	public interface Options{
		@Option(
				shortName = "a",
				longName = "malletArgument", 
				description = "Specify if the model use mallet")
		public Boolean isMalletForArgs();

		@Option(
				shortName = "n",
				longName = "malletNoneNode", 
				description = "Specify if the model use mallet")
		public Boolean isMalletForNone();

		@Option(
				shortName = "o",
				longName = "outputDir",
				description = "Specify the output directory to stores extracted texts")
		public String getOutputDir();

		@Option(
				defaultToNull = true,
				shortName = "c",
				longName = "The configuration for the classifier",
				description = "Specify the configuration for the classifier (e.g. Weka Classifier)")
		public String getConfig();
	}
	
	public static void main(String[] args) throws Exception {
		Options options = CliFactory.parseArguments(Options.class, args);
		
//		new File("outputs/patterns.txt").delete();
		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(new File("../discourse.conll.dataset/data"), DatasetMode.train);

		CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(TextReader.class, 
				TextReader.PARAM_SOURCE_LOCATION, dataset.getRawDirectory(), 
				TextReader.PARAM_LANGUAGE, "en",
				TextReader.PARAM_PATTERNS, "wsj_*");
		AnalysisEngineDescription conllSyntaxJsonReader = 
				ConllSyntaxGoldAnnotator.getDescription(dataset.getParsesJSonFile());

		AnalysisEngineDescription conllGoldJsonReader = 
				ConllDiscourseGoldAnnotator.getDescription(dataset.getRelationsJSonFile());

		File outputDirectory = new File(new File(options.getOutputDir()), PACKAGE_DIR);
		getWriterDescription(outputDirectory, options.isMalletForArgs(), options.isMalletForNone());
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(reader,
				conllSyntaxJsonReader, 
				conllGoldJsonReader, 
				getWriterDescription(outputDirectory, options.isMalletForArgs(), options.isMalletForNone())
				);

		for (File aComponent: outputDirectory.listFiles()){
			
			System.out.println("ArgumentSequenceLabeler.main(): training " + aComponent.getName());
			String[] configs = new String[]{};
			switch (aComponent.getName()) {
			case SEQUENCE_TAGGER:
				if (!options.isMalletForArgs())
					configs = new String[]{options.getConfig()};
				break;

			case NONE_NODE_TAGGER:
				if (!options.isMalletForNone())
					configs = new String[]{options.getConfig()};
				break;
			default:
				throw new RuntimeException("This directory was not configured, you need to update this part of the code");
			}
			Train.main(aComponent, configs);
		}
	}

}
