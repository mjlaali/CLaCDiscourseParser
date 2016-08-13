package ca.concordia.clac.ml.classifier;

import java.util.List;

import org.cleartk.ml.Feature;

public class ComplexInstance<INSTANCE_TYPE> {
	private INSTANCE_TYPE instance;
	private List<Feature> features;
	public ComplexInstance(INSTANCE_TYPE instance){
		this.instance = instance;
	}

	public INSTANCE_TYPE getInstance() {
		return instance;
	}
	
	public ComplexInstance<INSTANCE_TYPE> setFeatures(List<Feature> features) {
		this.features = features;
		return this;
	}
	
	public List<Feature> getFeatures() {
		return features;
	}

}