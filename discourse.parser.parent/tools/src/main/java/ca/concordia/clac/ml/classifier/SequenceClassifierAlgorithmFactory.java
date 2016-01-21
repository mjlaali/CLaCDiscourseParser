package ca.concordia.clac.ml.classifier;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;

public interface SequenceClassifierAlgorithmFactory<CLASSIFIER_OUTPUT, SEQUENCE_TYPE, INSTANCE_TYPE> {
	public Function<JCas, ? extends Collection<? extends SEQUENCE_TYPE>> getSequenceExtractor(JCas jCas);
	public BiFunction<List<INSTANCE_TYPE>, SEQUENCE_TYPE, List<List<Feature>>> getFeatureExtractor(JCas jCas);
	public BiFunction<List<INSTANCE_TYPE>, SEQUENCE_TYPE, List<CLASSIFIER_OUTPUT>> getLabelExtractor(JCas jCas);
	public SequenceClassifierConsumer<CLASSIFIER_OUTPUT, SEQUENCE_TYPE, INSTANCE_TYPE> getLabeller(JCas jCas);
	public Function<SEQUENCE_TYPE, List<INSTANCE_TYPE>> getInstanceExtractor(JCas aJCas);
}
