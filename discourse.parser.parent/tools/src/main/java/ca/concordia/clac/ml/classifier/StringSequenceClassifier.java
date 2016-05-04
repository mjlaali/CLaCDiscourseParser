package ca.concordia.clac.ml.classifier;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkSequenceAnnotator;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DefaultSequenceDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.ml.viterbi.DefaultOutcomeFeatureExtractor;
import org.cleartk.ml.viterbi.ViterbiDataWriterFactory;

public class StringSequenceClassifier<SEQUENCE_TYPE, INSTANCE_TYPE> extends GenericSequenceClassifier<String, SEQUENCE_TYPE, INSTANCE_TYPE>{
	public static <T, U> AnalysisEngineDescription getViterbiWriterDescription(
			Class<? extends SequenceClassifierAlgorithmFactory<String, T, U>> classifierAlgorithmFactoryCls,
					Class<? extends DataWriter<String>> dataWriterCls, File outputDirectory, Object... otherParams) throws ResourceInitializationException{
		Object[] params = JCasUtils.addParams(otherParams, 
				CleartkSequenceAnnotator.PARAM_DATA_WRITER_FACTORY_CLASS_NAME, ViterbiDataWriterFactory.class.getName(), 
				ViterbiDataWriterFactory.PARAM_DELEGATED_DATA_WRITER_FACTORY_CLASS, DefaultDataWriterFactory.class.getName(), 
				ViterbiDataWriterFactory.PARAM_OUTCOME_FEATURE_EXTRACTOR_NAMES, new String[] { DefaultOutcomeFeatureExtractor.class.getName() } 
				);
		return getWriterDescription(classifierAlgorithmFactoryCls, dataWriterCls, outputDirectory,
				params);
	}

	
	public static <T, U> AnalysisEngineDescription getWriterDescription(
			Class<? extends SequenceClassifierAlgorithmFactory<String, T, U>> classifierAlgorithmFactoryCls,
			Class<?> dataWriterCls, File outputDirectory, Object... otherParams) throws ResourceInitializationException {
		Object[] params = JCasUtils.addParams(otherParams, 
				GenericSequenceClassifier.PARAM_ALGORITHM_FACTORY_CLASS_NAME,
				classifierAlgorithmFactoryCls.getName(),
				CleartkSequenceAnnotator.PARAM_IS_TRAINING,
		        true,
		        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY,
		        outputDirectory,
		        DefaultSequenceDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
		        dataWriterCls
				);
		
		return AnalysisEngineFactory.createEngineDescription(StringSequenceClassifier.class, params);
	}
	
	
	public static <T, U> AnalysisEngineDescription getClassifierDescription(String goldView, String systemView, String defaultOutcome,
			Class<? extends SequenceClassifierAlgorithmFactory<String, T, U>> classifierAlgorithmFactoryCls,
			String modelLocation, Object... otherParams
			) throws ResourceInitializationException{
		
		Object[] params = JCasUtils.addParams(otherParams,
		        StringSequenceClassifier.class,
		        GenericSequenceClassifier.PARAM_ALGORITHM_FACTORY_CLASS_NAME,
		        classifierAlgorithmFactoryCls,
		        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH,
		        modelLocation,
		        GenericClassifierLabeller.PARAM_GOLD_VIEW, goldView,
		        GenericClassifierLabeller.PARAM_SYSTEM_VIEW, systemView,
		        GenericClassifierLabeller.PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT, defaultOutcome
		        );
				
		
		return AnalysisEngineFactory.createEngineDescription(
		        StringSequenceClassifier.class,
		        params);
	}
}
