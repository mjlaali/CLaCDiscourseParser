package org.cleartk.discourse_parsing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.corpus.conll2015.ConllJSONExporter;
import org.cleartk.corpus.conll2015.ConllSyntaxGoldAnnotator;
import org.cleartk.discourse_parsing.module.DiscourseConnectiveAnnotator;
import org.cleartk.discourse_parsing.module.DiscourseSenseAnnotator;
import org.cleartk.discourse_parsing.module.argumentLabeler.Arg1Labeler;
import org.cleartk.discourse_parsing.module.argumentLabeler.Arg2Labeler;
import org.cleartk.discourse_parsing.module.argumentLabeler.DCScopDetector;
import org.cleartk.discourse_parsing.module.argumentLabeler.KongEtAl2014ArgumentLabeler;
import org.cleartk.ml.jar.JarClassifierBuilder;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

public class DiscourseParser{
	public static final String DEFAULT_OUT_DIR = "outputs/parser/model_train";
	private String modelDir;
	
	public DiscourseParser(String modelDir) {
		this.modelDir = modelDir;
	}
	
	public static String getArg2LabelerTrainDir(String modelDir){
		return new File(new File(modelDir), "arg2Labeler").getAbsolutePath();
	}

	public static String getArg1LabelerTrainDir(String modelDir){
		return new File(new File(modelDir), "arg1Labeler").getAbsolutePath();
	}

	public static String getKongEtAlLabelerTrainDir(String modelDir){
		return new File(new File(modelDir), "kongEtAl").getAbsolutePath();
	}
	
	public static String getDcAnnotatorTrainDir(String modelDir) {
		return new File(new File(modelDir), "dcAnnotator").getAbsolutePath();
	}

	public static String getScopeDetectorTrainDir(String modelDir) {
		return new File(new File(modelDir), "scopDetector").getAbsolutePath();
	}

	public List<AnalysisEngineDescription> getModules() throws ResourceInitializationException{
		List<AnalysisEngineDescription> modules = new ArrayList<AnalysisEngineDescription>();
		modules.add(DiscourseConnectiveAnnotator.getClassifierDescription(getDcAnnotatorTrainDir(modelDir)));
		
//		AggregateBuilder builder = new AggregateBuilder();
//		builder.add(Arg2Labeler.getClassifierDescription(getArg2LabelerTrainDir(modelDir), Arg2Labeler.DEFAULT_PATTERN_FILE));
//		builder.add(Arg1Labeler.getClassifierDescription(getArg1LabelerTrainDir(modelDir)));
//		modules.add(builder.createAggregateDescription());
		
		modules.add(KongEtAl2014ArgumentLabeler.getClassifierDescription(getKongEtAlLabelerTrainDir(modelDir)));
//		modules.add(DiscourseArgumentLabeler.getDescription());
		
		modules.add(DiscourseSenseAnnotator.getDescription());
		
//		modules.add(ImplicitRelationAnnotator.getDescription());
		
		return modules;
	}

	public AnalysisEngineDescription getDescription() throws ResourceInitializationException {
		AggregateBuilder builder = new AggregateBuilder();
		for (AnalysisEngineDescription description: getModules()){
			builder.add(description);
		}
		
		return builder.createAggregateDescription();
	}

	public AnalysisEngineDescription getWriterDescription() throws ResourceInitializationException {
		AggregateBuilder builder = new AggregateBuilder();
		builder.add(DiscourseConnectiveAnnotator.getWriterDescription(getDcAnnotatorTrainDir(modelDir)));
		builder.add(Arg2Labeler.getWriterDescription(getArg2LabelerTrainDir(modelDir)));
		builder.add(Arg1Labeler.getWriterDescription(getArg1LabelerTrainDir(modelDir)));
		builder.add(KongEtAl2014ArgumentLabeler.getWriterDescription(getKongEtAlLabelerTrainDir(modelDir)));
		builder.add(DCScopDetector.getWriterDescription(getScopeDetectorTrainDir(modelDir)));
//		builder.add(DiscourseSenseAnnotator.getDescription());
		
		return builder.createAggregateDescription();
	}


	public void trainAndPackage(String wekaOptions) throws Exception{
		JarClassifierBuilder.trainAndPackage(new File(getKongEtAlLabelerTrainDir(modelDir)), wekaOptions);
		JarClassifierBuilder.trainAndPackage(new File(getArg2LabelerTrainDir(modelDir)), wekaOptions);
		JarClassifierBuilder.trainAndPackage(new File(getArg1LabelerTrainDir(modelDir)), wekaOptions);
		JarClassifierBuilder.trainAndPackage(new File(getDcAnnotatorTrainDir(modelDir)), wekaOptions);
		JarClassifierBuilder.trainAndPackage(new File(getScopeDetectorTrainDir(modelDir)), wekaOptions);
	}
	
	
	public static void main(String[] args) throws ResourceInitializationException, UIMAException, IOException {
		
		String inputDataset = args[0];
		
		String modelDir = args[1];
		String rawTextFolder = inputDataset + "/raw";
		String syntaxJsonFile = inputDataset + "/pdtb-parses.json";
		String outputJsonFile = args[2] + "/output.json";
		
		File[] files = new File(rawTextFolder).listFiles();
		Arrays.sort(files);
		CollectionReaderDescription reader = UriCollectionReader.getDescriptionFromFiles(Arrays.asList(files));

		AggregateBuilder builder = new AggregateBuilder();

		// A collection reader that creates one CAS per file, containing the file's URI
		AnalysisEngineDescription textReader = UriToDocumentTextAnnotator.getDescription();
		builder.add(textReader);

		AnalysisEngineDescription conllSyntaxJsonReader = ConllSyntaxGoldAnnotator.getDescription(syntaxJsonFile);
		builder.add(conllSyntaxJsonReader);
		
		DiscourseParser discourseParser = new DiscourseParser(modelDir);
		builder.add(discourseParser.getDescription());
		
		builder.add(ConllJSONExporter.getDescription(outputJsonFile));
		SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
	}

}
