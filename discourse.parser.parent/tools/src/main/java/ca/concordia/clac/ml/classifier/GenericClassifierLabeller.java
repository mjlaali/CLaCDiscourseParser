package ca.concordia.clac.ml.classifier;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.DataWriter;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;

public class GenericClassifierLabeller<CLASSIFIER_OUTPUT, INSTANCE_TYPE>
		extends CleartkAnnotator<CLASSIFIER_OUTPUT> {
	public static final String PARAM_PARALLEL_CLASSIFICATION = "parallelClassification";
	public static final String PARAM_LABELER_CLS_NAME = "labellerClsName";

	@ConfigurationParameter(name = PARAM_LABELER_CLS_NAME)
	private String labellerClsName;

	@ConfigurationParameter(name = PARAM_PARALLEL_CLASSIFICATION, defaultValue = "false")
	private Boolean parallelClassification;

	private GenericClassifier<CLASSIFIER_OUTPUT, INSTANCE_TYPE> genericClassifier;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory = InitializableFactory.create(context, labellerClsName, ClassifierAlgorithmFactory.class);


		Consumer<Instance<CLASSIFIER_OUTPUT>> writerFunc = (instance) -> {
			try {
				dataWriter.write(instance) ;
			} catch (CleartkProcessingException e) {
				throw new RuntimeException(e);
			}
		};

		Function<List<Feature>, CLASSIFIER_OUTPUT> classifierFunc = (features) -> {
			try {
				return classifier.classify(features);
			} catch (CleartkProcessingException e) {
				throw new RuntimeException(e);
			}
		};
		this.genericClassifier = new GenericClassifier<>(algorithmFactory, writerFunc, 
				classifierFunc, parallelClassification, isTraining());
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		genericClassifier.process(aJCas);
	}
	
	public static <CLASSIFIER_OUTPUT, INSTANCE_TYPE> AnalysisEngineDescription getWriterDescription(
			Class<? extends ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE>> classifierAlgorithmFactoryCls,
					@SuppressWarnings("rawtypes") Class<? extends DataWriter> dataWriterCls, File outputDirectory, Object... otherParams) throws ResourceInitializationException{
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

	public static <CLASSIFIER_OUTPUT, INSTANCE_TYPE> AnalysisEngineDescription getClassifierDescription(
			Class<? extends ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE>> classifierAlgorithmFactoryCls, URL modelUrl, Object... otherParams) throws ResourceInitializationException{
		List<Object> params = new ArrayList<>();
		params.addAll(Arrays.asList(
				GenericClassifierLabeller.PARAM_LABELER_CLS_NAME, classifierAlgorithmFactoryCls.getName(), 
				CleartkAnnotator.PARAM_IS_TRAINING, false, 
				GenericClassifierLabeller.PARAM_PARALLEL_CLASSIFICATION, false,
				GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelUrl)
				);
		params.addAll(Arrays.asList(otherParams));

		return AnalysisEngineFactory.createEngineDescription(StringClassifierLabeller.class, 
				params.toArray(new Object[params.size()])
				);
	}
}
