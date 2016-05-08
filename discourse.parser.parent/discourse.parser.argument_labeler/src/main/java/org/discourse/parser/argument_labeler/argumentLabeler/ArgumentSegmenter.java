package org.discourse.parser.argument_labeler.argumentLabeler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllDatasetPath;
import org.cleartk.corpus.conll2015.ConllDatasetPath.DatasetMode;
import org.cleartk.corpus.conll2015.ConllDatasetPathFactory;
import org.cleartk.corpus.conll2015.loader.ConllDataLoader;
import org.cleartk.corpus.conll2015.loader.ConllDataLoaderFactory;
import org.cleartk.ml.jar.Train;
import org.discourse.parser.argument_labeler.argumentLabeler.components.Arg1Classifier;
import org.discourse.parser.argument_labeler.argumentLabeler.components.Arg2Classifier;
import org.discourse.parser.argument_labeler.argumentLabeler.components.ConflictResolver;

public class ArgumentSegmenter {
	public static final String PACKAGE_DIR = "argumentSequenceLabeler/";
	public static URL DEFAULT_URL = ClassLoader.getSystemClassLoader().getResource("clacParser/model/" + PACKAGE_DIR);
	
	public static final String SEQUENCE_TAGGER = "sequenceTagger";
	public static final String NONE_NODE_TAGGER = "noneNodeTagger";
	public static final String ARG1_MODEL_LOCATION = "arg1";
	public static final String ARG2_MODEL_LOCATION = "arg2";
	public static final String NODE_REMOVER_MODEL_LOCATION= "nodeRemover";
	public static final String NODE_JUDGE_LOCATION = "nodeJudge";
	
	
	public static AnalysisEngineDescription getWriterDescription(File outputDirectory) throws ResourceInitializationException{
		AggregateBuilder aggregateBuilder = new AggregateBuilder();

		aggregateBuilder.add(Arg2Classifier.getWriterDescription(new File(outputDirectory, ARG2_MODEL_LOCATION)));
		aggregateBuilder.add(Arg1Classifier.getWriterDescription(new File(outputDirectory, ARG1_MODEL_LOCATION)));
		aggregateBuilder.add(ConflictResolver.getWriterDescription(new File(outputDirectory, NODE_JUDGE_LOCATION)));
//		aggregateBuilder.add(NodeRemover.getWriterDescription(new File(outputDirectory, NODE_REMOVER_MODEL_LOCATION)));
		
		return aggregateBuilder.createAggregateDescription();
	}

	public static AnalysisEngineDescription getClassifierDescription() throws ResourceInitializationException, MalformedURLException {
		return getClassifierDescription(DEFAULT_URL, null, null);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(String goldView, String systemView) throws ResourceInitializationException, MalformedURLException {
		return getClassifierDescription(DEFAULT_URL, goldView, systemView);
	}
	
	public static AnalysisEngineDescription getClassifierDescription(URL packageDir, String goldView, String systemView) throws ResourceInitializationException, MalformedURLException {
		
		AggregateBuilder aggregateBuilder = new AggregateBuilder();
		aggregateBuilder.add(Arg2Classifier.getClassifierDescription(new URL(packageDir, ARG2_MODEL_LOCATION + "/model.jar").toString(), goldView, systemView));
		aggregateBuilder.add(Arg1Classifier.getClassifierDescription(new URL(packageDir, ARG1_MODEL_LOCATION + "/model.jar").toString(), goldView, systemView));
		aggregateBuilder.add(ConflictResolver.getClassifierDescription(new URL(packageDir, NODE_JUDGE_LOCATION + "/model.jar").toString(), goldView, systemView));
//		aggregateBuilder.add(NodeRemover.getClassifierDescription(new URL(packageDir, NODE_REMOVER_MODEL_LOCATION + "/model.jar").toString(), goldView, systemView));
		
		return aggregateBuilder.createAggregateDescription();
	}
	
	
	public static void main(String[] args) throws Exception {
		new File("outputs/patterns.txt").delete();
		File dataFld = new File("../discourse.conll.dataset/data");
		DatasetMode mode = DatasetMode.trial;
		
//		File dataFld = new File("../discourse.parser.argument_labeler/outputs/data/test-data");
//		DatasetMode mode = DatasetMode.test;

		ConllDatasetPath dataset = new ConllDatasetPathFactory().makeADataset2016(dataFld, mode);

		ConllDataLoader dataLoader = ConllDataLoaderFactory.getInstance(dataset);
		
		File outputDirectory = new File(new File("outputs/resources/" + mode.toString()), PACKAGE_DIR);
		if (outputDirectory.exists())
			FileUtils.deleteDirectory(outputDirectory);
		SimplePipeline.runPipeline(dataLoader.getReader(),
				dataLoader.getAnnotator(false), 
				getWriterDescription(outputDirectory)
				);

		for (File aComponent: outputDirectory.listFiles()){
			System.out.println("ArgumentSequenceLabeler.main(): training " + aComponent.getName());
			Train.main(aComponent);
		}
	}

}
