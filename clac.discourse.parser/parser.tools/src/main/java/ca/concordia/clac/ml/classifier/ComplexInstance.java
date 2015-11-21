package ca.concordia.clac.ml.classifier;

import java.util.List;

import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class ComplexInstance<CLASSIFIER_OUTPUT, INSTANCE_TYPE extends Annotation> {
	private CLASSIFIER_OUTPUT label;
	private INSTANCE_TYPE instance;
	private List<Feature> features;
	public ComplexInstance(INSTANCE_TYPE instance){
		this.instance = instance;
	}

	public INSTANCE_TYPE getInstance() {
		return instance;
	}
	
	public ComplexInstance<CLASSIFIER_OUTPUT, INSTANCE_TYPE> setFeatures(List<Feature> features) {
		this.features = features;
		return this;
	}
	
	public CLASSIFIER_OUTPUT getLabel() {
		return label;
	}
	
	public ComplexInstance<CLASSIFIER_OUTPUT, INSTANCE_TYPE> setLabel(CLASSIFIER_OUTPUT label) {
		this.label = label;
		return this;
	}
	
	public List<Feature> getFeatures() {
		return features;
	}

	public Instance<CLASSIFIER_OUTPUT> getClearTkInstance() {
		return new Instance<CLASSIFIER_OUTPUT>(label, features);
	}
}