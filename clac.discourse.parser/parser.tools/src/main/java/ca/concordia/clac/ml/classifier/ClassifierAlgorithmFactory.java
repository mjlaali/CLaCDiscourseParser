package ca.concordia.clac.ml.classifier;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;

public interface ClassifierAlgorithmFactory <CLASSIFIER_OUTPUT, INSTANCE_TYPE extends Annotation> extends Initializable{
	public InstanceExtractor<INSTANCE_TYPE> getExtractor();
	public Function<INSTANCE_TYPE, List<Feature>> getFeatureExtractor();
	public Function<INSTANCE_TYPE, CLASSIFIER_OUTPUT> getLabelExtractor();
	public BiConsumer<CLASSIFIER_OUTPUT, INSTANCE_TYPE> getLabeller();
	
	
}
