package ca.concordia.clac.ml.classifier;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.component.initialize.ConfigurationParameterInitializer;
import org.apache.uima.fit.component.initialize.ExternalResourceInitializer;
import org.apache.uima.fit.factory.initializable.Initializable;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.ml.Feature;

public interface ClassifierAlgorithmFactory <CLASSIFIER_OUTPUT, INSTANCE_TYPE> extends Initializable{
	@Override
	default void initialize(UimaContext context) throws ResourceInitializationException {
	    ConfigurationParameterInitializer.initialize(this, context);
	    ExternalResourceInitializer.initialize(this, context);
	}
	public InstanceExtractor<INSTANCE_TYPE> getExtractor(JCas jCas);
	public List<Function<INSTANCE_TYPE, List<Feature>>> getFeatureExtractor(JCas jCas);
	public Function<INSTANCE_TYPE, CLASSIFIER_OUTPUT> getLabelExtractor(JCas jCas);
	public BiConsumer<CLASSIFIER_OUTPUT, INSTANCE_TYPE> getLabeller(JCas jCas);
	
}
