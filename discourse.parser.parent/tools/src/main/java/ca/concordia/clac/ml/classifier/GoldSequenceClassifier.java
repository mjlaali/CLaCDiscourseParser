package ca.concordia.clac.ml.classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GoldSequenceClassifier<CLASSIFIER_OUTPUT> implements Consumer<List<Instance<CLASSIFIER_OUTPUT>>>, Function<List<List<Feature>>, List<CLASSIFIER_OUTPUT>> {
	private Map<List<Feature>, Map<CLASSIFIER_OUTPUT, CLASSIFIER_OUTPUT>> inventory = new HashMap<>();
	private CLASSIFIER_OUTPUT defaultOutput;
	
	public GoldSequenceClassifier(CLASSIFIER_OUTPUT defaultOutput) {
		this.defaultOutput = defaultOutput;
	}

	@Override
	public List<CLASSIFIER_OUTPUT> apply(List<List<Feature>> aSequence) {
		List<CLASSIFIER_OUTPUT> outcomes = new ArrayList<>();
		
		CLASSIFIER_OUTPUT prevOutcome = defaultOutput;
		for (List<Feature> features: aSequence){
			Map<CLASSIFIER_OUTPUT, CLASSIFIER_OUTPUT> potentialOutcomes = inventory.get(features);
			CLASSIFIER_OUTPUT outcome = defaultOutput;
			
			if (potentialOutcomes != null){
				CLASSIFIER_OUTPUT label = potentialOutcomes.get(prevOutcome);
				if (label == null)
					outcome = potentialOutcomes.entrySet().iterator().next().getValue(); //pick randomly
				else 
					outcome = label;
			}
			
			outcomes.add(outcome);
			prevOutcome = outcome;
		}
		return outcomes;
	}

	@Override
	public void accept(List<Instance<CLASSIFIER_OUTPUT>> instances) {
		CLASSIFIER_OUTPUT prevLabel = defaultOutput;
		for (Instance<CLASSIFIER_OUTPUT> instance: instances){
			Map<CLASSIFIER_OUTPUT, CLASSIFIER_OUTPUT> potentialLabels = inventory.get(instance.getFeatures());
			if (potentialLabels == null){
				potentialLabels = new HashMap<>();
				inventory.put(instance.getFeatures(), potentialLabels);
			}
			potentialLabels.put(prevLabel, instance.getOutcome());
			prevLabel = instance.getOutcome();
		}
	}

	public void clear() {
		inventory.clear();
	}

}
