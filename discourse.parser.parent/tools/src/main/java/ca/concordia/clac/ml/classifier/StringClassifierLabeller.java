package ca.concordia.clac.ml.classifier;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

public class StringClassifierLabeller<INSTANCE_TYPE> extends GenericClassifierLabeller<String, INSTANCE_TYPE>{

	public static <T> AnalysisEngineDescription getWriterDescription(
			Class<? extends ClassifierAlgorithmFactory<String, T>> classifierAlgorithmFactoryCls,
					Class<? extends DataWriter<String>> dataWriterCls, File outputDirectory, Object... otherParams) throws ResourceInitializationException{
		List<Object> params = new ArrayList<>();
		params.addAll(Arrays.asList(
				GenericClassifierLabeller.PARAM_LABELER_CLS_NAME, classifierAlgorithmFactoryCls.getName(), 
				CleartkAnnotator.PARAM_IS_TRAINING, true, 
				DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME, dataWriterCls.getName(),
				DefaultDataWriterFactory.PARAM_OUTPUT_DIRECTORY, outputDirectory) 
				);
		params.addAll(Arrays.asList(otherParams));

		return AnalysisEngineFactory.createEngineDescription(StringClassifierLabeller.class,
				params.toArray(new Object[params.size()])
				);
	}

	public static <T> AnalysisEngineDescription getClassifierDescription(
			Class<? extends ClassifierAlgorithmFactory<String, T>> classifierAlgorithmFactoryCls, String modelUrl, Object... otherParams) throws ResourceInitializationException{
		return getClassifierDescription(null, null, null, classifierAlgorithmFactoryCls, modelUrl, otherParams);
	}

	public static <T> AnalysisEngineDescription getClassifierDescription(String goldView, String systemView, String defaultValue,
			Class<? extends ClassifierAlgorithmFactory<String, T>> classifierAlgorithmFactoryCls, String modelLocation, Object... otherParams) throws ResourceInitializationException{
		Object[] params = JCasUtils.addParams(otherParams, 
				GenericClassifierLabeller.PARAM_LABELER_CLS_NAME, classifierAlgorithmFactoryCls.getName(), 
				CleartkAnnotator.PARAM_IS_TRAINING, false, 
				GenericClassifierLabeller.PARAM_PARALLEL_CLASSIFICATION, false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelLocation, 
				GenericClassifierLabeller.PARAM_GOLD_VIEW, goldView,
				GenericClassifierLabeller.PARAM_SYSTEM_VIEW, systemView,
				GenericClassifierLabeller.PARAM_DEFAULT_GOLD_CLASSIFIER_OUTPUT, defaultValue
				);

		return AnalysisEngineFactory.createEngineDescription(StringClassifierLabeller.class, params);
	}
}
