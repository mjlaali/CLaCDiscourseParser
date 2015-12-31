package ca.concordia.clac.uima.engines.stat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("serial")
public class StatisticResult implements Serializable{
	private final Map<String, Map<String, Integer>> counts;
	private final Map<String, LabeledEnumeratedDistribution> distributions;
	
	public StatisticResult(Map<String, Map<String, Integer>> counts) {
		this.counts = counts;
		distributions = new HashMap<>();
		for (Entry<String, Map<String, Integer>> catFreqs: counts.entrySet()){
			Map<String, Integer> freqs = catFreqs.getValue();
			List<String> labels = new ArrayList<String>(freqs.keySet());
			Collections.sort(labels);
			
			int sum = 0;
			for (Integer freq: freqs.values()){
				sum += freq;
			}
			
			double[] probabilities = new double[labels.size()];
			for (int i = 0; i < labels.size(); i++){
				probabilities[i] = (double) freqs.get(labels.get(i)) / sum;
			}
			distributions.put(catFreqs.getKey(), new LabeledEnumeratedDistribution(
					labels.toArray(new String[labels.size()]), probabilities));
		}
	}

	public LabeledEnumeratedDistribution getDistribution(String name) {
		return distributions.get(name);
	}

	public Map<String, Map<String, Integer>> getCounts() {
		return counts;
	}
}
