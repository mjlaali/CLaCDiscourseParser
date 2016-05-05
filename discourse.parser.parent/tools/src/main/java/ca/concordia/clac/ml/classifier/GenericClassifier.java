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

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GenericClassifier <CLASSIFIER_OUTPUT, INSTANCE_TYPE>{

	protected final ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory;
	protected final Consumer<Instance<CLASSIFIER_OUTPUT>> writer;
	protected final Function<List<Feature>, CLASSIFIER_OUTPUT> classify;
	
	private final boolean parallelClassification;
	private boolean training;
	
	public GenericClassifier(ClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, INSTANCE_TYPE> algorithmFactory,
			Consumer<Instance<CLASSIFIER_OUTPUT>> writer,
			Function<List<Feature>, CLASSIFIER_OUTPUT> classify, 
			boolean parallelClassification,
			boolean training) {
		
		this.algorithmFactory = algorithmFactory;
		this.writer = writer;
		this.classify = classify;
		this.parallelClassification = parallelClassification;
		this.training = training;
	}
	
	public void setTraining(boolean training) {
		this.training = training;
	}
	
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
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
		if (training) {
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
	

	public static <INSTANCE_TYPE> Map<INSTANCE_TYPE, List<Feature>> calcFeatures(
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
