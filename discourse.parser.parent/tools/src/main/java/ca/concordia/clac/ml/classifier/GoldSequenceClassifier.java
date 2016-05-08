package ca.concordia.clac.ml.classifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.cleartk.ml.Feature;
import org.cleartk.ml.Instance;

public class GoldSequenceClassifier<CLASSIFIER_OUTPUT> implements Consumer<List<Instance<CLASSIFIER_OUTPUT>>>, Function<List<List<Feature>>, List<CLASSIFIER_OUTPUT>> {
	private Map<List<Feature>, Map<CLASSIFIER_OUTPUT, CLASSIFIER_OUTPUT>> inventory = new HashMap<>();
	private CLASSIFIER_OUTPUT defaultOutput;
	private String name;
	private static PrintStream output;
	
	public GoldSequenceClassifier(CLASSIFIER_OUTPUT defaultOutput, String name) {
		if (output == null)
			try {
				output = new PrintStream(new File("outputs/features.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		this.defaultOutput = defaultOutput;
		this.name = name;
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
				if (label == null){
					outcome = potentialOutcomes.entrySet().iterator().next().getValue(); //pick randomly
					System.err.println("GoldSequenceClassifier.apply(): picked randomley for " + name + "->" + features.stream().map(Feature::toString).collect(Collectors.joining(", ")));
				} else
					outcome = label;
					
			} else
				System.err.println("GoldSequenceClassifier.apply(): chose default value for " + name + "->" + features.stream().map(Feature::toString).collect(Collectors.joining(", ")));
			
			outcomes.add(outcome);
			prevOutcome = outcome;
		}
		return outcomes;
	}

	@Override
	public void accept(List<Instance<CLASSIFIER_OUTPUT>> instances) {
		output.println(instances.stream().map(Instance::toString).collect(Collectors.joining(",")));
		output.flush();
		CLASSIFIER_OUTPUT prevLabel = defaultOutput;
		for (Instance<CLASSIFIER_OUTPUT> instance: instances){
			Map<CLASSIFIER_OUTPUT, CLASSIFIER_OUTPUT> potentialLabels = inventory.get(instance.getFeatures());
			if (potentialLabels == null){
				potentialLabels = new HashMap<>();
				inventory.put(instance.getFeatures(), potentialLabels);
			}
			CLASSIFIER_OUTPUT prevValue = potentialLabels.put(prevLabel, instance.getOutcome());
			if (prevValue != null && !prevValue.equals(instance.getOutcome()))
				System.err.println("Confilict for [" + name + "] for these features :" + instance.getFeatures());
			prevLabel = instance.getOutcome();
		}
	}

	public void clear() {
		inventory.clear();
	}

}
