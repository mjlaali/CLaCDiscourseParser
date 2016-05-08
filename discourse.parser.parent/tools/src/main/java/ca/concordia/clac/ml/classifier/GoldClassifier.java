package ca.concordia.clac.ml.classifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GoldClassifier<CLASSIFIER_OUTPUT> implements Consumer<Instance<CLASSIFIER_OUTPUT>>, Function<List<Feature>, CLASSIFIER_OUTPUT> {
	private Map<List<Feature>, CLASSIFIER_OUTPUT> inventory;
	private CLASSIFIER_OUTPUT defaultValue;
	private String name;
	
	public GoldClassifier(CLASSIFIER_OUTPUT defaultValue, String name){
		this.defaultValue = defaultValue;
		inventory = new HashMap<>();
		this.name = name;
	}
	
	@Override
	public CLASSIFIER_OUTPUT apply(List<Feature> t) {
		CLASSIFIER_OUTPUT output = inventory.get(t);
		if (output == null)
			return defaultValue;
		return output;
	}

	@Override
	public void accept(Instance<CLASSIFIER_OUTPUT> t) {
		CLASSIFIER_OUTPUT prevOutcome = inventory.put(t.getFeatures(), t.getOutcome());
		if (prevOutcome != null && !prevOutcome.equals(t.getOutcome()))
			System.err.println("GoldClassifier.accept(): confilict for [" + name + "] with these features :" + t.getFeatures());
	}

	public void clear(){
		inventory.clear();
	}
}
