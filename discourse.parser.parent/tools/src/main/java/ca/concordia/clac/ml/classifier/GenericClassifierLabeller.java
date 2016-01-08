package ca.concordia.clac.ml.classifier;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
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
		extends CleartkAnnotator<CLASSIFIER_OUTPUT> {
	public static final String PARAM_PARALLEL_CLASSIFICATION = "parallelClassification";
	public static final String PARAM_LABELER_CLS_NAME = "labellerClsName";

	@ConfigurationParameter(name = PARAM_LABELER_CLS_NAME)
	private String labellerClsName;

	@ConfigurationParameter(name = PARAM_PARALLEL_CLASSIFICATION, defaultValue = "false")
	private Boolean parallelClassification;

	protected ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory;

	protected Consumer<Instance<CLASSIFIER_OUTPUT>> writer;
	protected Function<List<Feature>, CLASSIFIER_OUTPUT> classify;

	protected JCas aJCas;

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		algorithmFactory = InitializableFactory.create(context, labellerClsName, ClassifierAlgorithmFactory.class);


		writer = (instance) -> {
			try {
				dataWriter.write(instance) ;
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
		InstanceExtractor<INSTANCE_TYPE> extractor = algorithmFactory.getExtractor(aJCas);
		List<Function<INSTANCE_TYPE, List<Feature>>> featureExtractor = algorithmFactory.getFeatureExtractor(aJCas);
		Function<INSTANCE_TYPE, CLASSIFIER_OUTPUT> labelExtractor = algorithmFactory.getLabelExtractor(aJCas);
		BiConsumer<CLASSIFIER_OUTPUT, INSTANCE_TYPE> labeller = algorithmFactory.getLabeller(aJCas);

		Collection<INSTANCE_TYPE> instances = extractor.getInstances(aJCas);

		Stream<INSTANCE_TYPE> stream;
		if (parallelClassification)
			stream = instances.parallelStream();
		else
			stream = instances.stream();

		Map<INSTANCE_TYPE, List<Feature>> allFeatures = calcFeatures(stream, featureExtractor);
		BiConsumer<? super INSTANCE_TYPE, ? super List<Feature>> action;
		if (isTraining()) {
			action = (ins, features) -> {
				CLASSIFIER_OUTPUT label = labelExtractor.apply(ins);
				writer.accept(new Instance<CLASSIFIER_OUTPUT>(label, features));
			};
		} else {
			action = (ins, features) -> {
				CLASSIFIER_OUTPUT label = classify.apply(features);
				labeller.accept(label, ins);
			};

		}
		allFeatures.forEach(action);
	}

	public static <INSTANCE_TYPE extends Annotation> Map<INSTANCE_TYPE, List<Feature>> calcFeatures(
			Stream<INSTANCE_TYPE> stream, List<Function<INSTANCE_TYPE, List<Feature>>> featureExtractor) {
		
		Map<INSTANCE_TYPE, List<Feature>> allFeatures = stream.flatMap(ann -> featureExtractor.stream().map(f -> {
			ComplexInstance<INSTANCE_TYPE> res = new ComplexInstance<>(ann);
			res.setFeatures(f.apply(ann));
			return res;
		})).filter((ci) -> !ci.getFeatures().isEmpty())
			.collect(Collectors.toMap(ci -> ci.getInstance(), ci -> ci.getFeatures(), (list1, list2) -> {
			List<Feature> res = new LinkedList<>(list1);
			res.addAll(list2);
			return res;
		}));
		return allFeatures;
	}
}
