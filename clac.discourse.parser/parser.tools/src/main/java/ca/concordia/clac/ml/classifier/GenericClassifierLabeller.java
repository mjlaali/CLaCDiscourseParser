package ca.concordia.clac.ml.classifier;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.CleartkAnnotator;
import org.cleartk.ml.CleartkProcessingException;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GenericClassifierLabeller<CLASSIFIER_OUTPUT, INSTANCE_TYPE extends Annotation> 
extends CleartkAnnotator<CLASSIFIER_OUTPUT>{
	public static final String PARAM_PARALLEL_CLASSIFICATION = "parallelClassification";
	public static final String PARAM_LABELER_CLS_NAME = "labellerClsName";

	@ConfigurationParameter(name = PARAM_LABELER_CLS_NAME)
	private String labellerClsName;
	
	@ConfigurationParameter(name = PARAM_PARALLEL_CLASSIFICATION, defaultValue="false")
	private Boolean parallelClassification;

	protected ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory;
	private InstanceExtractor<INSTANCE_TYPE> extractor;
	private Function<INSTANCE_TYPE, List<Feature>> featureExtractor;
	private Function<INSTANCE_TYPE, CLASSIFIER_OUTPUT> labelExtractor;
	private BiConsumer<CLASSIFIER_OUTPUT, INSTANCE_TYPE> labeller;
	
	protected Consumer<Instance<CLASSIFIER_OUTPUT>> writer;
	protected Function<List<Feature>, CLASSIFIER_OUTPUT> classify;
	
	protected JCas aJCas;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		algorithmFactory = InitializableFactory.create(context, labellerClsName, ClassifierAlgorithmFactory.class);
		
		extractor = algorithmFactory.getExtractor();
		featureExtractor = algorithmFactory.getFeatureExtractor();
		labelExtractor = algorithmFactory.getLabelExtractor();
		labeller = algorithmFactory.getLabeller();
		
		writer = (instance) -> {
			try {
				dataWriter.write(instance);
			} catch (CleartkProcessingException e) {
				throw new RuntimeException(e);
			}
		};
		
		classify = (features) -> {
			try {
				return classifier.classify(features);
			} catch (CleartkProcessingException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		this.aJCas = aJCas;
		Collection<INSTANCE_TYPE> instances = extractor.getInstances(aJCas);
		
		Stream<INSTANCE_TYPE> stream;
		if (parallelClassification)
			stream = instances.parallelStream();
		else
			stream = instances.stream();
		
		Stream<ComplexInstance<CLASSIFIER_OUTPUT, INSTANCE_TYPE>> instancesWithFeatures = stream
			.map(i -> new ComplexInstance<CLASSIFIER_OUTPUT, INSTANCE_TYPE>(i))
			.map(i -> i.setFeatures(featureExtractor.apply(i.getInstance())));

		if (isTraining()){
			instancesWithFeatures
				.map(i -> i.setLabel(labelExtractor.apply(i.getInstance())))
				.map(i -> i.getClearTkInstance())
				.forEach(writer);
		} else {
			instancesWithFeatures
				.map(i -> i.setLabel(classify.apply(i.getFeatures())))
				.forEach(i -> labeller.accept(i.getLabel(), i.getInstance()));
		}
	}

}
